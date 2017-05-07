/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package robot;

import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import obstacles.types.ObstacleRobot;
import pathfinding.astar.arcs.ClothoidesComputer;
import pathfinding.chemin.CheminPathfinding;
import serie.BufferOutgoingOrder;
import serie.SerialProtocol;
import serie.SerialProtocol.InOrder;
import serie.SerialProtocol.State;
import serie.Ticket;
import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.CoreClass;
import exceptions.ActionneurException;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;
import graphic.Fenetre;
import graphic.PrintBufferInterface;
import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import graphic.printable.Segment;

/**
 * Effectue le lien entre le code et la réalité (permet de parler à la carte bas niveau, d'interroger les capteurs, etc.)
 * @author pf
 *
 */

public class RobotReal extends Robot implements Service, Printable, CoreClass
{
	protected volatile boolean matchDemarre = false;
    protected volatile long dateDebutMatch;
    private boolean debugpf, simuleSerie, debugAct;
    private int demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant;
	private boolean print, printTrace;
	private PrintBufferInterface buffer;
	private BufferOutgoingOrder out;
	private CheminPathfinding chemin;
	private double courbureMax;
    private boolean cinematiqueInitialised = false;
    private CinematiqueObs[] pointsAvancer = new CinematiqueObs[256];

	// Constructeur
	public RobotReal(Log log, BufferOutgoingOrder out, PrintBufferInterface buffer, CheminPathfinding chemin, Config config)
 	{
		super(log);
		this.buffer = buffer;
		this.out = out;
		this.chemin = chemin;
		
		// c'est le LL qui fournira la position
		cinematique = new Cinematique(0, 300, 0, true, 3);
		print = config.getBoolean(ConfigInfo.GRAPHIC_ROBOT_AND_SENSORS);
		demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		printTrace = config.getBoolean(ConfigInfo.GRAPHIC_TRACE_ROBOT);
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);
		debugpf = config.getBoolean(ConfigInfo.DEBUG_PF);
		simuleSerie = config.getBoolean(ConfigInfo.SIMULE_SERIE);
		debugAct = config.getBoolean(ConfigInfo.DEBUG_ACTIONNEURS);
		
		if(print)
			buffer.add(this);
		
		for(int i = 0; i < pointsAvancer.length; i++)
			pointsAvancer[i] = new CinematiqueObs(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */

	@Override
	public synchronized void updateConfig(Config config)
	{
		super.updateConfig(config);
		dateDebutMatch = config.getLong(ConfigInfo.DATE_DEBUT_MATCH);
		matchDemarre = config.getBoolean(ConfigInfo.MATCH_DEMARRE);
	}
			
	public void setEnMarcheAvance(boolean enMarcheAvant)
	{
		cinematique.enMarcheAvant = enMarcheAvant;
	}
	
	@Override
    public long getTempsDepuisDebutMatch()
    {
		if(!matchDemarre)
			return 0;
		return System.currentTimeMillis() - dateDebutMatch;
    }

	public boolean isCinematiqueInitialised()
	{
		return cinematiqueInitialised;
	}
	
	@Override
	public synchronized void setCinematique(Cinematique cinematique)
	{
		Vec2RO old = this.cinematique.getPosition().clone();
		super.setCinematique(cinematique);
		/*
		 * On vient juste de récupérer la position initiale
		 */
		if(!cinematiqueInitialised)
		{
			cinematiqueInitialised = true;
			notifyAll();
		}
		synchronized(buffer)
		{
			// affichage
			if(printTrace && old.distanceFast(cinematique.getPosition()) < 100)
				buffer.addSupprimable(new Segment(old, cinematique.getPosition().clone(), Layer.FOREGROUND, Couleur.ROUGE.couleur));
			else if(print)
				buffer.notify();
		}
	}

	/**
	 * N'est utilisé que pour l'affichage
	 * @return
	 */
	public Cinematique getCinematique()
	{
		return cinematique;
	}

	@Override
	public void print(Graphics g, Fenetre f, RobotReal robot)
	{
		ObstacleRobot o = new ObstacleRobot(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);
		o.update(cinematique.getPosition(), cinematique.orientationReelle);
		o.print(g, f, robot);
	}

	@Override
	public Layer getLayer()
	{
		return Layer.FOREGROUND;
	}

	public int getDemieLargeurGauche()
	{
		return demieLargeurNonDeploye;
	}

	public int getDemieLargeurDroite()
	{
		return demieLargeurNonDeploye;
	}

	public int getDemieLongueurAvant()
	{
		return demieLongueurAvant;
	}

	public int getDemieLongueurArriere()
	{
		return demieLongueurArriere;
	}
	
	/*
	 * DÉPLACEMENTS
	 */
	
	
	/**
	 * Se déplace en visant la direction d'un point
	 * @param distance
	 * @param speed
	 * @param centre
	 * @throws UnableToMoveException
	 * @throws InterruptedException
	 */
	@Override
	public void avanceVersCentre(Speed speed, Vec2RO centre, double rayon) throws UnableToMoveException, InterruptedException
	{
		double orientationReelleDesiree = Math.atan2(centre.getY()-cinematique.position.getY(), centre.getX()-cinematique.position.getX());
		double deltaO = (orientationReelleDesiree - cinematique.orientationReelle) % (2*Math.PI);
		if(deltaO > Math.PI)
			deltaO -= 2*Math.PI;

//		log.debug("deltaO = "+deltaO);
		boolean enAvant = Math.abs(deltaO) < Math.PI/2;
//		log.debug("enAvant = "+enAvant);
		
		// on regarde maintenant modulo PI pour savoir si on est aligné
		deltaO = deltaO % Math.PI;
		if(deltaO > Math.PI/2)
			deltaO -= Math.PI;
		
		if(Math.abs(deltaO) < 0.001) // on est presque aligné
		{
//			log.debug("Presque aligné : "+deltaO);
			double distance = cinematique.position.distanceFast(centre) - rayon;
			if(!enAvant)
				distance = -distance;
			avanceVersCentreLineaire(distance, speed, centre);
			return;
		}
			
		double cos = Math.cos(cinematique.orientationReelle);
		double sin = Math.sin(cinematique.orientationReelle);
		if(!enAvant)
		{
			cos = -cos;
			sin = -sin;
		}
		
		Vec2RO a = cinematique.position, bp = centre;		
		// le symétrique du centre bp
		Vec2RO ap = new Vec2RO(cinematique.position.getX()-rayon*cos, cinematique.position.getY()-rayon*sin);
		Vec2RO d = bp.plusNewVector(ap).scalar(0.5); // le milieu entre ap et bp, sur l'axe de symétrie
		Vec2RW u = bp.minusNewVector(ap);
		double n = u.norm();
		double ux = -u.getY() / n;
		double uy = u.getX() / n;
		double vx = -sin;
		double vy = cos;
		
//		log.debug(ap+" "+a+" "+d+" "+bp);
		
		double alpha = (uy * (d.getX() - a.getX()) + ux * (a.getY() - d.getY())) / (vx * uy - vy * ux);
		Vec2RO c = new Vec2RO(a.getX() + alpha * vx, a.getY() + alpha * vy);
		if(alpha > 0)
		{
			ux = -ux;
			uy = -uy;
			vx = -vx;
			vy = -vy;
		}

//		log.debug("C : "+c);
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		double rayonTraj = c.distance(a);
		double courbure = 1000. / rayonTraj; // la courbure est en m^-1
		
		if(courbure > courbureMax)
		{
			double distance = cinematique.position.distanceFast(centre) - rayon;
			if(!enAvant)
				distance = -distance;
			avanceVersCentreLineaire(distance, speed, centre);
			return;
		}
		
		Vec2RW delta = a.minusNewVector(c);
					
		double angle = (2*(new Vec2RO(ux, uy).getFastArgument() - new Vec2RO(vx, vy).getFastArgument())) % (2*Math.PI);
		if(angle > Math.PI)
			angle -= 2*Math.PI;
//			double angle = 2*Math.acos(ux * vx + uy * vy); // angle total
		double longueur = angle * rayonTraj;
//			log.debug("Angle : "+angle);

		int nbPoints = (int) Math.round(Math.abs(longueur) / ClothoidesComputer.PRECISION_TRACE_MM);
		
		cos = Math.cos(angle);
		sin = Math.sin(angle);
					
		delta.rotate(Math.cos(angle), Math.sin(angle)); // le tout dernier point, B
		
//			log.debug("B : "+delta.plusNewVector(c));
		
		double anglePas = -angle/nbPoints;

		if(angle < 0)
			courbure = -courbure;
		
		cos = Math.cos(anglePas);
		sin = Math.sin(anglePas);
		
//			log.debug("nbPoints = "+nbPoints);
		
		for(int i = nbPoints - 1; i >= 0; i--)
		{
			double orientation = cinematique.orientationReelle;
			if(!enAvant)
				orientation += Math.PI; // l'orientation géométrique
			orientation -= (i+1)*anglePas;
			pointsAvancer[i].update(delta.getX() + c.getX(), delta.getY() + c.getY(), orientation, enAvant, courbure);
			delta.rotate(cos, sin);
		}
		for(int i = 0; i < nbPoints; i++)
			out.add(pointsAvancer[i]);

		try {
			Ticket[] t = chemin.addToEnd(out);
			for(Ticket ticket : t)
				ticket.attendStatus();
		} catch (PathfindingException e) {
			// Ceci ne devrait pas arriver, ou alors en demandant d'avancer de 5m
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
		followTrajectory(speed);
		
	}
	
	/**
	 * Ancienne version de avanceVersCentre basée sur une simple droite. Mauvais résultats.
	 * @param distance
	 * @param speed
	 * @param centre
	 * @param rayon
	 * @throws UnableToMoveException
	 * @throws InterruptedException
	 */
	public void avanceVersCentreLineaire(double distance, Speed speed, Vec2RO centre) throws UnableToMoveException, InterruptedException
	{
		double orientationReelleDesiree = Math.atan2(centre.getY()-cinematique.position.getY(), centre.getX()-cinematique.position.getX());
		double deltaO = (orientationReelleDesiree - cinematique.orientationReelle) % (2*Math.PI);
		if(deltaO > Math.PI)
			deltaO -= 2*Math.PI;
		if(Math.abs(deltaO) > Math.PI/2)
			orientationReelleDesiree += Math.PI;
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		double cos = Math.cos(orientationReelleDesiree);
		double sin = Math.sin(orientationReelleDesiree);
		int nbPoint = (int) Math.round(Math.abs(distance) / ClothoidesComputer.PRECISION_TRACE_MM);
		double xFinal = cinematique.position.getX()+distance*cos;
		double yFinal = cinematique.position.getY()+distance*sin;
		boolean marcheAvant = distance > 0;
		double orientationGeometrique = marcheAvant ? orientationReelleDesiree : -orientationReelleDesiree;
		if(nbPoint == 0)
		{
			// Le point est vraiment tout proche
			pointsAvancer[0].update(xFinal, yFinal, orientationGeometrique, marcheAvant, 0);
			out.add(pointsAvancer[0]);
		}
		else
		{
			double deltaX = ClothoidesComputer.PRECISION_TRACE_MM * cos;
			double deltaY = ClothoidesComputer.PRECISION_TRACE_MM * sin;
			if(distance < 0)
			{
				deltaX = -deltaX;
				deltaY = -deltaY;
			}
			for(int i = 0; i < nbPoint; i++)
				pointsAvancer[nbPoint-i-1].update(xFinal - i * deltaX, yFinal - i * deltaY, orientationGeometrique, marcheAvant, 0);
			for(int i = 0; i < nbPoint; i++)
				out.add(pointsAvancer[i]);
		}

		try {
			Ticket[] t = chemin.addToEnd(out);
			for(Ticket ticket : t)
				ticket.attendStatus();
		} catch (PathfindingException e) {
			// Ceci ne devrait pas arriver, ou alors en demandant d'avancer de 5m
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
		followTrajectory(speed);
	}
	
	@Override
	public void avance(double distance, Speed speed) throws UnableToMoveException, InterruptedException
	{
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		double cos = Math.cos(cinematique.orientationReelle);
		double sin = Math.sin(cinematique.orientationReelle);
		int nbPoint = (int) Math.round(Math.abs(distance) / ClothoidesComputer.PRECISION_TRACE_MM);
		double xFinal = cinematique.position.getX()+distance*cos;
		double yFinal = cinematique.position.getY()+distance*sin;
		boolean marcheAvant = distance > 0;
		double orientationGeometrique = marcheAvant ? cinematique.orientationReelle : -cinematique.orientationReelle;
		if(nbPoint == 0)
		{
			// Le point est vraiment tout proche
			pointsAvancer[0].update(xFinal, yFinal, orientationGeometrique, marcheAvant, 0);
			out.add(pointsAvancer[0]);
		}
		else
		{
			double deltaX = ClothoidesComputer.PRECISION_TRACE_MM * cos;
			double deltaY = ClothoidesComputer.PRECISION_TRACE_MM * sin;
			if(distance < 0)
			{
				deltaX = -deltaX;
				deltaY = -deltaY;
			}
			for(int i = 0; i < nbPoint; i++)
				pointsAvancer[nbPoint-i-1].update(xFinal - i * deltaX, yFinal - i * deltaY, orientationGeometrique, marcheAvant, 0);
			for(int i = 0; i < nbPoint; i++)
				out.add(pointsAvancer[i]);
		}

		try {
			Ticket[] t = chemin.addToEnd(out);
			for(Ticket ticket : t)
				ticket.attendStatus();
		} catch (PathfindingException e) {
			// Ceci ne devrait pas arriver, ou alors en demandant d'avancer de 5m
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
		followTrajectory(speed);
	}
	
	/*
	 * ACTIONNEURS
	 */
	
	/**
	 * Rend bloquant l'appel d'une méthode
	 * @param m
	 * @throws InterruptedException
	 * @throws ActionneurException 
	 */
	@Override
	protected void bloque(String nom, Object... param) throws InterruptedException, ActionneurException
	{
		if(debugAct)
		{
			if(param == null || param.length == 0)
				log.debug("Appel à "+nom);
			else
			{
				String s = "";
				for(Object o : param)
				{
					if(s != "")
						s += ", ";
					s += o;
				}
				log.debug("Appel à "+nom+" (param = "+s+")");
			}
		}
		
		if(simuleSerie)
			return;
		
		SerialProtocol.State etat;
		Ticket t = null;
		Class<?>[] paramClasses = null;
		if(param.length > 0)
		{
			paramClasses = new Class[param.length];
			for(int i = 0; i < param.length; i++)
				paramClasses[i] = param[i].getClass();
		}
		long avant = System.currentTimeMillis();
		try {
			t = (Ticket) BufferOutgoingOrder.class.getMethod(nom, paramClasses).invoke(out, param.length == 0 ? null : param);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
		etat = t.attendStatus().etat;
		if(etat == SerialProtocol.State.KO)
			throw new ActionneurException("Problème pour l'actionneur "+nom);

		if(debugAct)
			log.debug("Temps d'exécution de "+nom+" : "+(System.currentTimeMillis()-avant));
	}

	/**
	 * Initialise les actionneurs pour le début du match
	 * @throws InterruptedException 
	 */
	public void initActionneurs() throws InterruptedException
	{
		try {
			leveFilet();
			verrouilleFilet();
			rearme();
			rearmeAutreCote();
		} catch (ActionneurException e) {
			log.critical(e);
		}
	}

	/**
	 * Méthode bloquante qui suit une trajectoire précédemment envoyée
	 * @throws InterruptedException
	 * @throws UnableToMoveException 
	 */
	@Override
	public void followTrajectory(Speed vitesse) throws InterruptedException, UnableToMoveException
	{
		if(chemin.isEmpty())
			log.warning("Trajectoire vide !");
		else
			while(!chemin.isEmpty())
			{
				Ticket t = out.followTrajectory(vitesse, chemin.getNextMarcheAvant());
				InOrder i = t.attendStatus();
				if(i.etat == State.KO)
				{
					log.critical("Erreur : "+i);
					chemin.clear();
					throw new UnableToMoveException("Erreur : "+i);
				}
				if(debugpf)
					log.debug("Le trajet s'est bien terminé ("+i+")");
				Thread.sleep(50); // on attend un peu que l'indice du chemin soit mis à jour avant de vérifier s'il est vide
			}
		chemin.clear(); // dans tous les cas, il faut nettoyer le chemin
	}

}

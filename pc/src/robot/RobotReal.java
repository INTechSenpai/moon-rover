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
import serie.Ticket;
import config.Config;
import config.ConfigInfo;
import container.Service;
import container.dependances.CoreClass;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import utils.Log;
import utils.Vec2RO;
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
    private int demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant;
    private int nbRetente;
	private boolean print, printTrace;
	private PrintBufferInterface buffer;
	private BufferOutgoingOrder out;
	private CheminPathfinding chemin;
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
		cinematique = new Cinematique(0, 300, 0, true, 3, Speed.STANDARD.translationalSpeed);
		print = config.getBoolean(ConfigInfo.GRAPHIC_ROBOT_AND_SENSORS);
		demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);
		nbRetente = config.getInt(ConfigInfo.NB_TENTATIVES_ACTIONNEURS);
		printTrace = config.getBoolean(ConfigInfo.GRAPHIC_TRACE_ROBOT);
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
	
	@Override
	public void avance(double distance, Speed speed) throws UnableToMoveException, InterruptedException
	{
		// TODO revoir
		LinkedList<CinematiqueObs> out = new LinkedList<CinematiqueObs>();
		double cos = Math.cos(cinematique.orientationReelle);
		double sin = Math.sin(cinematique.orientationReelle);
		int nbPoint = (int) Math.round(Math.abs(distance) / ClothoidesComputer.PRECISION_TRACE_MM);
		double xFinal = cinematique.position.getX()+distance*cos;
		double yFinal = cinematique.position.getY()+distance*sin;
		boolean marcheAvant = distance > 0;
		double orientationGeometrique = marcheAvant ? cinematique.orientationReelle : -cinematique.orientationReelle;
		double vitesse = marcheAvant ? Speed.STANDARD.translationalSpeed : Speed.MARCHE_ARRIERE.translationalSpeed;
		if(nbPoint == 0)
		{
			// Le point est vraiment tout proche
			pointsAvancer[0].update(xFinal, yFinal, orientationGeometrique, marcheAvant, 0, vitesse);
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
				pointsAvancer[nbPoint-i-1].update(xFinal - i * deltaX, yFinal - i * deltaY, orientationGeometrique, marcheAvant, 0, vitesse);
			for(int i = 0; i < nbPoint; i++)
				out.add(pointsAvancer[i]);
		}

		try {
			Ticket[] t = chemin.add(out);
			for(Ticket ticket : t)
				ticket.attendStatus();
		} catch (PathfindingException e) {
			// Ceci ne devrait pas arriver, ou alors en demandant d'avancer de 5m
			e.printStackTrace();
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
	 */
	@Override
	protected void bloque(String nom, Object... param) throws InterruptedException
	{
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
		int nbEssai = nbRetente;
		do {
			try {
				t = (Ticket) BufferOutgoingOrder.class.getMethod(nom, paramClasses).invoke(out, param.length == 0 ? null : param);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			etat = t.attendStatus().etat;
			nbEssai--;
			if(etat == SerialProtocol.State.KO)
				log.warning("Problème pour l'actionneur "+nom+" : on "+(nbEssai >= 0 ? "retente." : "abandonne."));
		} while(nbEssai >= 0 && etat == SerialProtocol.State.KO);
		log.debug("Temps d'exécution de "+nom+" : "+(System.currentTimeMillis()-avant));
	}

	/**
	 * Initialise les actionneurs pour le début du match
	 * @throws InterruptedException 
	 */
	public void initActionneurs() throws InterruptedException
	{
		leveFilet();
	}

	/**
	 * Méthode bloquante qui suit une trajectoire précédemment envoyée
	 * @throws InterruptedException
	 * @throws UnableToMoveException 
	 */
	@Override
	public void followTrajectory(Speed vitesse) throws InterruptedException, UnableToMoveException
	{
		followTrajectory(chemin.getIndexLast(), vitesse);
	}
	
	/**
	 * Méthode bloquante qui suit une trajectoire précédemment envoyée.
	 * S'arrête à l'index spécifié
	 * @throws InterruptedException
	 * @throws UnableToMoveException 
	 */
	public void followTrajectory(int indexTraj, Speed vitesse) throws InterruptedException, UnableToMoveException
	{
		if(chemin.isEmpty())
			log.debug("Trajectoire vide !");
		else
			while(chemin.getCurrentIndex() != indexTraj)
			{
				Ticket t = out.followTrajectory(vitesse, chemin.getNextMarcheAvant());
				InOrder i = t.attendStatus();
				if(i == InOrder.ROBOT_BLOCAGE_EXTERIEUR || i == InOrder.ROBOT_BLOCAGE_INTERIEUR)
				{
					log.critical("Erreur : "+i);
					throw new UnableToMoveException();
				}
			}
	}

}

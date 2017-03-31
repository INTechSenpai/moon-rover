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

package tests.lowlevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import config.ConfigInfo;
import graphic.PrintBuffer;
import obstacles.types.ObstacleCircular;
import pathfinding.RealGameState;
import pathfinding.astar.AStarCourbe;
import pathfinding.astar.arcs.ArcCourbeStatique;
import pathfinding.astar.arcs.ClothoidesComputer;
import pathfinding.astar.arcs.vitesses.VitesseClotho;
import pathfinding.chemin.CheminPathfinding;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotReal;
import robot.Speed;
import serie.BufferOutgoingOrder;
import tests.JUnit_Test;

/**
 * Tests unitaires des actionneurs
 * @author pf
 *
 */

public class JUnit_Robot extends JUnit_Test {

	private RobotReal robot;
	private AStarCourbe astar;
	private CheminPathfinding chemin;
	private RealGameState state;
	private BufferOutgoingOrder data;
	private Cinematique c;
	private boolean simuleSerie;
	private Speed v;
	private double last;
	
	/**
	 * Génère un fichier qui présente les tests
	 * @param args
	 */
	public static void main(String[] args)
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("liste-tests.txt"));
			Method[] methodes = JUnit_Robot.class.getDeclaredMethods();
			for(Method m : methodes)
				if(m.isAnnotationPresent(Test.class))
					writer.write("./run_junit.sh tests.lowlevel.JUnit_Robot#"+m.getName()+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		} 
		finally
		{
			if(writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			System.out.println("Génération de la liste des tests terminée.");
		}
	}
	
	/**
	 * Pas un test
	 */
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
		state = container.getService(RealGameState.class);
		robot = container.getService(RobotReal.class);
		chemin = container.getService(CheminPathfinding.class);
		astar = container.getService(AStarCourbe.class);
		data = container.getService(BufferOutgoingOrder.class);
		simuleSerie = config.getBoolean(ConfigInfo.SIMULE_SERIE);
		data.startStream();
		this.v = null;
		double v = config.getInt(ConfigInfo.DEBUG_VITESSE_ROBOT) / 1000.;
		for(Speed s : Speed.values())
			if(Math.abs(s.translationalSpeed - v) < 0.001)
				this.v = s;
		if(this.v == null)
		{
			log.warning("Vitesse inconnue ! "+v*1000.+". Vitesse utilisée : "+Speed.TEST1.translationalSpeed);
			this.v = Speed.TEST1;
		}
		else
			log.debug("Vitesse du robot : "+v*1000.+" ("+this.v+")");
	}
	
	/**
	 * Pas un test
	 */
	@Override
	@After
	public void tearDown() throws Exception {
		data.stopStream();
		if(!simuleSerie)
		{
			log.debug("Position robot : "+robot.getCinematique().getPosition());
			log.debug("Position voulue : "+c.getPosition());
			log.debug("Erreur position : "+robot.getCinematique().getPosition().distance(c.getPosition()));
			log.debug("Orientation robot : "+robot.getCinematique().orientationReelle);
			log.debug("Orientation voulue : "+last);
			double deltaO = robot.getCinematique().orientationReelle - last;
			if(deltaO > Math.PI)
				deltaO -= 2*Math.PI;
			else if(deltaO < -Math.PI)
				deltaO += 2*Math.PI;
			log.debug("Erreur orientation : "+deltaO);
		}

		super.tearDown();
	}
	
	/**
	 * Test AX-12 filet
	 * @throws Exception
	 */
	@Test
	public void test_ax12() throws Exception
	{
		robot.baisseFilet();
		robot.leveFilet();
		robot.bougeFiletMiChemin();
	}

	/**
	 * Test de l'attrape-rêve
	 * @throws Exception
	 */
	@Test
	public void test_actionneurs() throws Exception
	{
		robot.ejecteBalles();
		robot.rearme();
		robot.ouvreFilet();
		robot.fermeFilet();
		robot.traverseBascule();
		robot.funnyAction();
	}
	
	/**
	 * TODO
	 * Trajectoire qui alterne marche avant / marche arrière (créneau)
	 * @throws Exception
	 */
	@Test
    public void creneau() throws Exception
    {
		Cinematique depart = new Cinematique(-900, 650, -Math.PI/6, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(-400, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin);
		last = chemin.getLastOrientation();
		if(!simuleSerie)
			robot.followTrajectory(v);
    }

	/**
	 * Trajectoire longue vers la gauche
	 */
	@Test
    public void courbe_loin() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(1000, 700, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		last = chemin.getLastOrientation();
		if(!simuleSerie)
			robot.followTrajectory(v);
    }

	/**
	 * Cette trajectoire procède en deux temps : le rover recule puis avance
	 * @throws Exception
	 */
	@Test
    public void arriere_loin() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1700, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(1000, 700, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		last = chemin.getLastOrientation();
		if(!simuleSerie)
			robot.followTrajectory(v);
    }

	
	/**
	 * Petite trajectoire en marche avant vers la gauche
	 * @throws Exception
	 */
	@Test
    public void gauche() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(300, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		last = chemin.getLastOrientation();
		if(!simuleSerie)
			robot.followTrajectory(v);
    }
	
	/**
	 * Trajectoire de longueur moyenne en marche avant vers la gauche
	 * @throws Exception
	 */
	@Test
    public void gauche2() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(700, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		last = chemin.getLastOrientation();
		if(!simuleSerie)
			robot.followTrajectory(v);
    }

	/**
	 * Trajectoire longue (environ 2m) en marche avant
	 * @throws Exception
	 */
	@Test
    public void courbe_longue() throws Exception
    {
		Cinematique depart = new Cinematique(-1000, 700, 0, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(1000, 700, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		last = chemin.getLastOrientation();
		if(!simuleSerie)
			robot.followTrajectory(v);
    }
	
	/**
	 * Trajectoire courte vers la droite. Courbure assez grande (-3.4)
	 * @throws Exception
	 */
	@Test
    public void droite() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(-300, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		last = chemin.getLastOrientation();
		if(!simuleSerie)
			robot.followTrajectory(v);
    }
	
	@Test
    public void droite2() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(-700, 1100, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		last = chemin.getLastOrientation();
		if(!simuleSerie)
			robot.followTrajectory(v);
    }
	
	/**
	 * Le robot recule de 20cm
	 * @throws Exception
	 */
	@Test
    public void recule() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1600, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(500);
		if(!simuleSerie)
			robot.avance(-200, v);
    }
	
	/**
	 * Le robot avance de 20cm
	 * @throws Exception
	 */
	@Test
    public void avance() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(500);
		if(!simuleSerie)
			robot.avance(200, v);
    }
	
	/**
	 * Un test qui sert plus à grand chose
	 * @throws Exception
	 */
	@Test
    public void test_clotho() throws Exception
    {
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		int nbArc = 4;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(1000);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.GAUCHE_3, Speed.STANDARD, arc[0]);
		
		clotho.getTrajectoire(arc[0], VitesseClotho.DROITE_1, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.GAUCHE_2, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[3]);

		ArrayList<CinematiqueObs> path = new ArrayList<CinematiqueObs>();
		
		for(int i = 0; i < nbArc; i++)
			for(int j = 0; j < arc[i].getNbPoints(); j++)
			{
				path.add(arc[i].getPoint(j));
				buffer.addSupprimable(new ObstacleCircular(arc[i].getPoint(j).getPosition(), 4));
			}

		data.envoieArcCourbe(path, 0);
		if(!simuleSerie)
			robot.followTrajectory(v);
    }


	/**
	 * Fais un cercle avec un rayon de courbure de 20cm
	 * @throws Exception
	 */
	@Test
    public void test_cercle() throws Exception
    {
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		int nbArc = 50;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, true, 5, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(1000);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		
		for(int i = 1; i < nbArc; i++)
			clotho.getTrajectoire(arc[i-1], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[i]);

		ArrayList<CinematiqueObs> path = new ArrayList<CinematiqueObs>();
		
		for(int i = 0; i < nbArc; i++)
			for(int j = 0; j < arc[i].getNbPoints(); j++)
			{
				path.add(arc[i].getPoint(j));
				buffer.addSupprimable(new ObstacleCircular(arc[i].getPoint(j).getPosition(), 4));
			}

		data.envoieArcCourbe(path, 0);
		if(!simuleSerie)
			robot.followTrajectory(v);
    }

	@Test
    public void grand_cercle_droite_sym() throws Exception
    {
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		int nbArc = 9;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1500, -Math.PI/2, true, -2, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(1000);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[3]);
		clotho.getTrajectoire(arc[3], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[4]);
		clotho.getTrajectoire(arc[4], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[5]);
		clotho.getTrajectoire(arc[5], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[6]);
		clotho.getTrajectoire(arc[6], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[7]);
		clotho.getTrajectoire(arc[7], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[8]);
		
		ArrayList<CinematiqueObs> path = new ArrayList<CinematiqueObs>();
		
		for(int i = 0; i < nbArc; i++)
			for(int j = 0; j < arc[i].getNbPoints(); j++)
			{
				log.debug(arc[i].getPoint(j));
				path.add(arc[i].getPoint(j));
				buffer.addSupprimable(new ObstacleCircular(arc[i].getPoint(j).getPosition(), 4));
			}

		data.envoieArcCourbe(path, 0);
		if(!simuleSerie)
			robot.followTrajectory(v);
    }
	

	@Test
    public void grand_cercle_gauche_sym() throws Exception
    {
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		int nbArc = 9;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1500, -Math.PI/2, true, 2, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(1000);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[3]);
		clotho.getTrajectoire(arc[3], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[4]);
		clotho.getTrajectoire(arc[4], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[5]);
		clotho.getTrajectoire(arc[5], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[6]);
		clotho.getTrajectoire(arc[6], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[7]);
		clotho.getTrajectoire(arc[7], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[8]);
		
		ArrayList<CinematiqueObs> path = new ArrayList<CinematiqueObs>();
		
		for(int i = 0; i < nbArc; i++)
			for(int j = 0; j < arc[i].getNbPoints(); j++)
			{
				log.debug(arc[i].getPoint(j));
				path.add(arc[i].getPoint(j));
				buffer.addSupprimable(new ObstacleCircular(arc[i].getPoint(j).getPosition(), 4));
			}

		data.envoieArcCourbe(path, 0);
		if(!simuleSerie)
			robot.followTrajectory(v);
    }
	
	@Test
    public void petit_cercle_droite_sym() throws Exception
    {
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		int nbArc = 4;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1500, -Math.PI/2, true, -5, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(1000);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[3]);
		
		ArrayList<CinematiqueObs> path = new ArrayList<CinematiqueObs>();
		
		for(int i = 0; i < nbArc; i++)
			for(int j = 0; j < arc[i].getNbPoints(); j++)
			{
				log.debug(arc[i].getPoint(j));
				path.add(arc[i].getPoint(j));
				buffer.addSupprimable(new ObstacleCircular(arc[i].getPoint(j).getPosition(), 4));
			}

		data.envoieArcCourbe(path, 0);
		if(!simuleSerie)
			robot.followTrajectory(v);
    }

	@Test
    public void petit_cercle_gauche_sym() throws Exception
    {
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		int nbArc = 4;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1500, -Math.PI/2, true, 5, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(1000);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[3]);
		
		ArrayList<CinematiqueObs> path = new ArrayList<CinematiqueObs>();
		
		for(int i = 0; i < nbArc; i++)
			for(int j = 0; j < arc[i].getNbPoints(); j++)
			{
				log.debug(arc[i].getPoint(j));
				path.add(arc[i].getPoint(j));
				buffer.addSupprimable(new ObstacleCircular(arc[i].getPoint(j).getPosition(), 4));
			}

		data.envoieArcCourbe(path, 0);
		if(!simuleSerie)
			robot.followTrajectory(v);
    }

	@Test
    public void droite_sym() throws Exception
    {
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		int nbArc = 6;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1500, -Math.PI/2, true, -2, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(1000);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.GAUCHE_3, Speed.STANDARD, arc[3]);
		clotho.getTrajectoire(arc[3], VitesseClotho.GAUCHE_3, Speed.STANDARD, arc[4]);
		clotho.getTrajectoire(arc[4], VitesseClotho.GAUCHE_3, Speed.STANDARD, arc[5]);
		
		ArrayList<CinematiqueObs> path = new ArrayList<CinematiqueObs>();
		
		for(int i = 0; i < nbArc; i++)
			for(int j = 0; j < arc[i].getNbPoints(); j++)
			{
				log.debug(arc[i].getPoint(j));
				path.add(arc[i].getPoint(j));
				buffer.addSupprimable(new ObstacleCircular(arc[i].getPoint(j).getPosition(), 4));
			}

		data.envoieArcCourbe(path, 0);
		if(!simuleSerie)
			robot.followTrajectory(v);
    }
	
	@Test
    public void gauche_sym() throws Exception
    {
		ClothoidesComputer clotho = container.getService(ClothoidesComputer.class);
		PrintBuffer buffer = container.getService(PrintBuffer.class);
		
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		int nbArc = 6;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1500, -Math.PI/2, true, 2, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(1000);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.COURBURE_IDENTIQUE, Speed.STANDARD, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.DROITE_3, Speed.STANDARD, arc[3]);
		clotho.getTrajectoire(arc[3], VitesseClotho.DROITE_3, Speed.STANDARD, arc[4]);
		clotho.getTrajectoire(arc[4], VitesseClotho.DROITE_3, Speed.STANDARD, arc[5]);
		
		ArrayList<CinematiqueObs> path = new ArrayList<CinematiqueObs>();
		
		for(int i = 0; i < nbArc; i++)
			for(int j = 0; j < arc[i].getNbPoints(); j++)
			{
				log.debug(arc[i].getPoint(j));
				path.add(arc[i].getPoint(j));
				buffer.addSupprimable(new ObstacleCircular(arc[i].getPoint(j).getPosition(), 4));
			}

		data.envoieArcCourbe(path, 0);
		if(!simuleSerie)
			robot.followTrajectory(v);
    }
	
}

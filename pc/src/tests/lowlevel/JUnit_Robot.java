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

import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import config.ConfigInfo;
import graphic.PrintBuffer;
import obstacles.types.ObstacleCircular;
import pathfinding.RealGameState;
import pathfinding.SensFinal;
import pathfinding.astar.AStarCourbe;
import pathfinding.astar.arcs.ArcCourbeDynamique;
import pathfinding.astar.arcs.ArcCourbeStatique;
import pathfinding.astar.arcs.BezierComputer;
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
	
	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
		state = container.getService(RealGameState.class);
		robot = container.getService(RobotReal.class);
		chemin = container.getService(CheminPathfinding.class);
		astar = container.getService(AStarCourbe.class);
		data = container.getService(BufferOutgoingOrder.class);
		data.startStream();
	}
	
	@Override
	@After
	public void tearDown() throws Exception {
		data.stopStream();
		super.tearDown();
	}
	@Test
	public void test_ax12() throws Exception
	{
		robot.baisseFilet();
		robot.leveFilet();
		robot.bougeFiletMiChemin();
	}

	@Test
	public void test_actionneurs() throws Exception
	{
		robot.ejecteBalles();
		robot.rearme();
		robot.ouvreFilet();
		robot.fermeFilet();
		robot.traverseBascule();
	}
	
	@Test
    public void test_follow_trajectory_creneau() throws Exception
    {
		Cinematique depart = new Cinematique(-900, 650, 0, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		Cinematique c = new Cinematique(-400, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin);
		robot.followTrajectory(true, Speed.TEST);
    }

	@Test
    public void test_follow_trajectory_courbe_loin() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		Cinematique c = new Cinematique(1000, 700, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		robot.followTrajectory(true, Speed.TEST);
    }

	@Test
    public void test_follow_trajectory_courbe_arriere_loin() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1700, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		Cinematique c = new Cinematique(1000, 700, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		robot.followTrajectory(true, Speed.TEST);
    }

	
	@Test
    public void test_follow_trajectory_courbe_gauche() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		Cinematique c = new Cinematique(300, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);

		depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		c = new Cinematique(-300, 1200, -Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);

		//		robot.followTrajectory(true, Speed.TEST);
    }

	@Test
    public void test_follow_trajectory_courbe_droite() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		Cinematique c = new Cinematique(300, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);

		BezierComputer bezier = container.getService(BezierComputer.class);
		
		int nbArc = 1;
		ArcCourbeDynamique arc[] = new ArcCourbeDynamique[nbArc];

		c = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		data.correctPosition(c.getPosition(), c.orientationReelle); // on envoie la position haut niveau
		Cinematique arrivee = new Cinematique(-300, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		arc[0] = bezier.interpolationQuadratique(c, arrivee.getPosition(), Speed.STANDARD);
		
		Thread.sleep(500);
		data.envoieArcCourbe(arc[0].arcs, 0);
//		robot.followTrajectory(true, Speed.TEST);
    }

	@Test
    public void test_follow_trajectory_courbe_arriere() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1700, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		Cinematique c = new Cinematique(200, 1400, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, SensFinal.MARCHE_ARRIERE, true, state);
		astar.process(chemin);
		robot.followTrajectory(false, Speed.TEST);
    }
	
	@Test
    public void test_follow_trajectory_droite_arriere() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1600, Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(500);
		robot.avance(-200, Speed.TEST);
    }
	
	
	
	@Test
    public void test_follow_trajectory_droite() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(500);
		robot.avance(200, Speed.TEST);
    }
	
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
//		robot.followTrajectory(true, Speed.TEST);
    }

	
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
		robot.followTrajectory(true, Speed.TEST);
    }

}

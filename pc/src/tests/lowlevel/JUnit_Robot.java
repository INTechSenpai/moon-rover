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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pathfinding.RealGameState;
import pathfinding.SensFinal;
import pathfinding.astar.AStarCourbe;
import pathfinding.chemin.CheminPathfinding;
import robot.Cinematique;
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
    public void test_follow_trajectory_courbe() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/2, true, 0, Speed.STANDARD.translationalSpeed);
		robot.setCinematique(depart);
		data.correctPosition(depart.getPosition(), depart.orientationReelle); // on envoie la position haut niveau
		Thread.sleep(100); // on attend un peu que la position soit affectée bas niveau
		Cinematique c = new Cinematique(300, 1200, Math.PI, false, 0, Speed.STANDARD.translationalSpeed);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		robot.followTrajectory(true, Speed.TEST);
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
}

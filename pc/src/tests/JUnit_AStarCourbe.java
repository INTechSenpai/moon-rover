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

package tests;

import obstacles.types.ObstacleCircular;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import config.ConfigInfo;
import graphic.PrintBuffer;
import pathfinding.PathCache;
import pathfinding.RealGameState;
import pathfinding.SensFinal;
import pathfinding.astar.AStarCourbe;
import pathfinding.astar.arcs.ArcCourbeStatique;
import pathfinding.astar.arcs.BezierComputer;
import pathfinding.astar.arcs.CercleArrivee;
import pathfinding.astar.arcs.ArcCourbe;
import pathfinding.astar.arcs.ArcCourbeDynamique;
import pathfinding.astar.arcs.ClothoidesComputer;
import pathfinding.astar.arcs.vitesses.VitesseClotho;
import pathfinding.astar.arcs.vitesses.VitesseDemiTour;
import pathfinding.astar.arcs.vitesses.VitesseRameneVolant;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.IteratorCheminPathfinding;
import pathfinding.dstarlite.gridspace.GridSpace;
import robot.Cinematique;
import robot.CinematiqueObs;
import robot.RobotReal;
import table.GameElementNames;
import threads.ThreadName;
import threads.ThreadUpdatePathfinding;
import utils.Vec2RO;

/**
 * Tests unitaires de la recherche de chemin courbe
 * @author pf
 *
 */

public class JUnit_AStarCourbe extends JUnit_Test {

	private AStarCourbe astar;
	private ClothoidesComputer clotho;
	protected BezierComputer bezier;
	private PrintBuffer buffer;
	private RobotReal robot;
	private IteratorCheminPathfinding iterator;
	private boolean graphicTrajectory;
	private CheminPathfinding chemin;
	private GridSpace gridspace;
	private CercleArrivee cercle;
	private RealGameState state;
//	private PrecomputedPaths prepaths;
//	private ArcManager arcmanager;
//	private DStarLite dstarlite;

	@Override
	@Before
    public void setUp() throws Exception {
        super.setUp();
		clotho = container.getService(ClothoidesComputer.class);
		buffer = container.getService(PrintBuffer.class);
		astar = container.getService(AStarCourbe.class);
		state = container.getService(RealGameState.class);
//		dstarlite = container.getService(DStarLite.class);
		robot = container.getService(RobotReal.class);
		chemin = container.getService(CheminPathfinding.class);
		iterator = new IteratorCheminPathfinding(chemin);
		gridspace = container.getService(GridSpace.class);
		bezier = container.getService(BezierComputer.class);
		cercle = container.getService(CercleArrivee.class);
//		arcmanager = container.getService(ArcManager.class);
		graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY_FINAL);
	}

	@Test
    public void test_clotho() throws Exception
    {
		int demieLargeurNonDeploye = config.getInt(ConfigInfo.LARGEUR_NON_DEPLOYE)/2;
		int demieLongueurArriere = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_ARRIERE);
		int demieLongueurAvant = config.getInt(ConfigInfo.DEMI_LONGUEUR_NON_DEPLOYE_AVANT);

		boolean graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		int nbArc = 16;
		ArcCourbeStatique arc[] = new ArcCourbeStatique[nbArc];
		for(int i = 0; i < nbArc; i++)
			arc[i] = new ArcCourbeStatique(demieLargeurNonDeploye, demieLongueurArriere, demieLongueurAvant);

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, false, 0);
		log.debug("Initial : "+c);
		clotho.getTrajectoire(c, VitesseClotho.COURBURE_IDENTIQUE, arc[0]);
		clotho.getTrajectoire(arc[0], VitesseClotho.GAUCHE_2, arc[1]);
		clotho.getTrajectoire(arc[1], VitesseClotho.COURBURE_IDENTIQUE, arc[2]);
		clotho.getTrajectoire(arc[2], VitesseClotho.GAUCHE_1, arc[3]);
		clotho.getTrajectoire(arc[3], VitesseClotho.COURBURE_IDENTIQUE, arc[4]);
		clotho.getTrajectoire(arc[4], VitesseClotho.COURBURE_IDENTIQUE, arc[5]);
		clotho.getTrajectoire(arc[5], VitesseClotho.COURBURE_IDENTIQUE, arc[6]);
		clotho.getTrajectoire(arc[6], VitesseClotho.GAUCHE_1, arc[7]);
		clotho.getTrajectoire(arc[7], VitesseClotho.GAUCHE_2, arc[8]);
		clotho.getTrajectoire(arc[8], VitesseClotho.GAUCHE_2, arc[9]);
		clotho.getTrajectoire(arc[9], VitesseClotho.DROITE_1, arc[10]);
		clotho.getTrajectoire(arc[10], VitesseClotho.DROITE_1, arc[11]);
		clotho.getTrajectoire(arc[11], VitesseClotho.DROITE_1, arc[12]);
		clotho.getTrajectoire(arc[12], VitesseClotho.DROITE_1, arc[13]);
		clotho.getTrajectoire(arc[13], VitesseClotho.DROITE_1, arc[14]);
		clotho.getTrajectoire(arc[14], VitesseClotho.GAUCHE_2, arc[15]);

		for(int a = 0; a < nbArc; a++)	
		{
//			System.out.println("arc "+arc[a].v+" avec "+arc[a].arcselems[0]);
			for(int i = 0; i < ClothoidesComputer.NB_POINTS; i++)
			{
/*				if(i > 0)
					System.out.println(arc[a].arcselems[i-1].point.distance(arc[a].arcselems[i].point));
				else if(a > 0)
					System.out.println(arc[a-1].arcselems[ClothoidesComputer.NB_POINTS - 1].point.distance(arc[a].arcselems[0].point));
	*/				
				System.out.println(a+" "+i+" "+arc[a].arcselems[i]);
				if(graphicTrajectory)
					buffer.addSupprimable(new ObstacleCircular(arc[a].getPoint(i).getPosition(), 4));
			}
			if(a == 0)
			{
				Assert.assertEquals(arc[0].arcselems[ClothoidesComputer.NB_POINTS - 1].getPositionEcriture().getX(), 0, 0.1);
				Assert.assertEquals(arc[0].arcselems[ClothoidesComputer.NB_POINTS - 1].getPositionEcriture().getY(), 1000+(int)ClothoidesComputer.DISTANCE_ARC_COURBE, 0.1);
			}
/*			else if(arc[a].arcselems[0].enMarcheAvant != arc[a-1].arcselems[0].enMarcheAvant)
				Assert.assertEquals(arc[a].vitesseCourbure.vitesse / 1000. * ClothoidesComputer.DISTANCE_ARC_COURBE, arc[a].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, 0.1);
			else if(a > 0)
				Assert.assertEquals(arc[a].vitesseCourbure.vitesse / 1000. * ClothoidesComputer.DISTANCE_ARC_COURBE + arc[a-1].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, arc[a].arcselems[ClothoidesComputer.NB_POINTS-1].courbure, 0.1);
*/		}
	
		log.debug(arc[nbArc-1].arcselems[arc[nbArc-1].arcselems.length - 1].getPosition());
		Assert.assertEquals(0, arc[nbArc-1].arcselems[arc[nbArc-1].arcselems.length - 1].getPosition().distance(new Vec2RO(-220.06207166901692,1372.8216060022155)), 0.1);
    }

	@Test
    public void test_demi_tour() throws Exception
    {
		boolean graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		
		int nbArc = 2;
		ArcCourbeDynamique arc[] = new ArcCourbeDynamique[nbArc];

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, false, 0);
		log.debug("Initial : "+c);
		arc[0] = clotho.getTrajectoireDemiTour(c, VitesseDemiTour.DEMI_TOUR_GAUCHE);
		arc[1] = clotho.getTrajectoireDemiTour(arc[0].getLast(), VitesseDemiTour.DEMI_TOUR_DROITE);

		for(int a = 0; a < nbArc; a++)	
		{
			for(int i = 0; i < arc[a].getNbPoints(); i++)
			{
				System.out.println(a+" "+i+" "+arc[a].getPoint(i));
				if(graphicTrajectory)
					buffer.addSupprimable(new ObstacleCircular(arc[a].getPoint(i).getPosition(), 4));
			}
		}
    }
	
	@Test
    public void test_ramene() throws Exception
    {
		boolean graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		
		int nbArc = 2;
		ArcCourbe arc[] = new ArcCourbe[nbArc];

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, false, 0);
		log.debug("Initial : "+c);
		arc[0] = clotho.getTrajectoireDemiTour(c, VitesseDemiTour.DEMI_TOUR_GAUCHE);
//		arc[0] = new ArcCourbeStatique(container.getService(RobotReal.class));
//		clotho.getTrajectoire(c, VitesseCourbure.DROITE_5, (ArcCourbeStatique)arc[0]);
		arc[1] = clotho.getTrajectoireRamene(arc[0].getLast(), VitesseRameneVolant.RAMENE_VOLANT);

		for(int a = 0; a < nbArc; a++)	
		{
			for(int i = 0; i < arc[a].getNbPoints(); i++)
			{
				System.out.println(a+" "+i+" "+arc[a].getPoint(i));
				if(graphicTrajectory)
					buffer.addSupprimable(new ObstacleCircular(arc[a].getPoint(i).getPosition(), 4));
			}
		}
    }
	
	@Test
    public void test_bezier_quad() throws Exception
    {
		boolean graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		
		int nbArc = 1;
		ArcCourbeDynamique arc[] = new ArcCourbeDynamique[nbArc];

		Cinematique c = new Cinematique(0, 1000, Math.PI/2, true, -1);
		Cinematique arrivee = new Cinematique(400, 1400, Math.PI/2, false, 0);
		log.debug("Initial : "+c);
		arc[0] = bezier.interpolationQuadratique(c, arrivee.getPosition());
		
		Assert.assertTrue(arc[0] != null);
		
		for(int a = 0; a < nbArc; a++)	
		{
			for(int i = 0; i < arc[a].getNbPoints(); i++)
			{
				System.out.println(a+" "+i+" "+arc[a].getPoint(i));
				if(graphicTrajectory)
					buffer.addSupprimable(new ObstacleCircular(arc[a].getPoint(i).getPosition(), 4));
			}
		}
    }

	@Test
    public void test_bezier_quad_cercle() throws Exception
    {
		boolean graphicTrajectory = config.getBoolean(ConfigInfo.GRAPHIC_TRAJECTORY);
		
		int nbArc = 1;
		ArcCourbeDynamique arc[] = new ArcCourbeDynamique[nbArc];

		Cinematique c = new Cinematique(-200, 1000, Math.PI, true, -1);
		cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_GAUCHE, 250);
		log.debug("Initial : "+c);
		arc[0] = bezier.interpolationQuadratiqueCercle(c);
		
		Assert.assertTrue(arc[0] != null);
		
		for(int a = 0; a < nbArc; a++)	
		{
			for(int i = 0; i < arc[a].getNbPoints(); i++)
			{
				System.out.println(a+" "+i+" "+arc[a].getPoint(i));
				if(graphicTrajectory)
					buffer.addSupprimable(new ObstacleCircular(arc[a].getPoint(i).getPosition(), 4));
			}
		}
		Assert.assertTrue(cercle.isArrived(arc[0].getLast()));
    }
	
	/*
	@Test
    public void test_bench() throws Exception
    {
		int nbmax = 10000;
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-1100, 600, 0, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(0, 1200, Math.PI, false, 0);
		for(int i = 0; i < nbmax; i++)
		{
			astar.computeNewPath(c, false);
			chemin.clear();
		}
		log.debug("Temps : "+(System.nanoTime() - avant) / (nbmax * 1000000.));
    }
*/
	@Test
    public void test_recherche_manoeuvre() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-900, 450, -Math.PI/6, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(-400, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
    }
	
	@Test
    public void test_recherche_shoot() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/3, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		iterator.reinit();
		CinematiqueObs a = null, b = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(b != null)
				log.debug(a.getPosition().distance(b.getPosition()));
			b = a;
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
    }

	@Test
    public void test_recherche_shoot2() throws Exception
    {
		Cinematique depart = new Cinematique(-200, 1600, -Math.PI/3+Math.PI, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		iterator.reinit();
		CinematiqueObs a = null, b = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(b != null)
				log.debug(a.getPosition().distance(b.getPosition()));
			b = a;
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
    }

	@Test
    public void test_recherche_shoot_pas() throws Exception
    {
		Cinematique depart = new Cinematique(0, 1800, -Math.PI/3, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(1000, 1200, Math.PI, false, 0);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin);
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
    }
	
	@Test
    public void test_prepathsf() throws Exception
    {
		container.getService(PathCache.class);
    }
	
	@Test
    public void test_replanif() throws Exception
    {
		// Ce test impose l'arrêt du thread de pathfinding pour ne pas avoir d'interférence
		ThreadUpdatePathfinding thread = container.getService(ThreadUpdatePathfinding.class);
		thread.interrupt();
		thread.join(1000);
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 300, Math.PI/2, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(0, 1600, Math.PI, false, 0);
		astar.initializeNewSearch(c, false, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int n = 10;
		while(iterator.hasNext() && iterator.getIndex() < n)
		{
			a = iterator.next();
			chemin.setCurrentIndex(iterator.getIndex());
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(-400, 1300));
//		gridspace.addObstacleAndRemoveNearbyObstacles(new Vec2RO(0, 1600)); // TODO
		chemin.checkColliding();
		avant = System.nanoTime();
		astar.updatePath(true);
		log.debug("Temps recalcul : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		while(iterator.hasNext())
		{
			a = iterator.next();
			chemin.setCurrentIndex(iterator.getIndex());
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		container.restartThread(ThreadName.UPDATE_PATHFINDING); // pour pas qu'il soit mécontent
	}
	
	@Test
    public void test_recherche_finit_en_arriere() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, Math.PI/2, true, 0);
		robot.setCinematique(depart);
		cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_GAUCHE, 250);
		astar.initializeNewSearchToCircle(true, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}
	
	@Test
    public void test_recherche_finit_en_arriere2() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, Math.PI/2, true, 0);
		robot.setCinematique(depart);
		cercle.set(GameElementNames.MINERAI_CRATERE_HAUT_DROITE, 250);
		astar.initializeNewSearchToCircle(false, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}
	
	@Test
    public void test_recherche_finit_en_arriere3() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, Math.PI/2, true, 0);
		robot.setCinematique(depart);
		cercle.set(GameElementNames.MINERAI_CRATERE_BAS_DROITE, 250);
		astar.initializeNewSearchToCircle(true, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}
	
	@Test
    public void test_recherche_finit_en_avant() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-400, 1200, Math.PI, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(300, 1200, 0, false, 0);
		astar.initializeNewSearch(c, SensFinal.MARCHE_AVANT, false, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}
	
	/**
	 * Trajectoire avec des arcs "…_AFTER_STOP"
	 * @throws Exception
	 */
	@Test
    public void test_trajectoire_avec_arret() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, 3*Math.PI/4, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(-300, 800, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}
	
	@Test
    public void test_recherche_loin() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 350, 3*Math.PI/4, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(800, 700, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}

	@Test
    public void test_recherche_loin_arriere() throws Exception
    {
		long avant = System.nanoTime();
		Cinematique depart = new Cinematique(-800, 400, -Math.PI/2, true, 0);
		robot.setCinematique(depart);
		Cinematique c = new Cinematique(800, 700, Math.PI, false, 0);
		astar.initializeNewSearch(c, true, state);
		astar.process(chemin);
		log.debug("Temps : "+(System.nanoTime() - avant) / (1000000.));
		iterator.reinit();
		CinematiqueObs a = null;
		int i = 0;
		while(iterator.hasNext())
		{
			i++;
			a = iterator.next();
			log.debug(a);
			robot.setCinematique(a);
			if(graphicTrajectory)
				Thread.sleep(100);
		}
		log.debug("Nb points : "+i);
	}

}

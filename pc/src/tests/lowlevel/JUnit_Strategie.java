/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package tests.lowlevel;

import org.junit.Before;
import org.junit.Test;
import pathfinding.ChronoGameState;
import pathfinding.KeyPathCache;
import pathfinding.PathCache;
import robot.Cinematique;
import scripts.ScriptNames;
import scripts.Strategie;
import tests.JUnit_Test;

/**
 * Tests unitaires pour les capteurs
 * 
 * @author pf
 *
 */

public class JUnit_Strategie extends JUnit_Test
{

	private Strategie strat;
	private PathCache path;
	private ChronoGameState chrono;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		strat = container.getService(Strategie.class);
		path = container.getService(PathCache.class);
		chrono = container.make(ChronoGameState.class);
	}

	@Test
	public void test_pathcache() throws Exception
	{
		chrono.robot.setCinematique(new Cinematique(700, 1800, Math.PI, true, 0));
		path.computeAndFollow(new KeyPathCache(chrono, ScriptNames.SCRIPT_CRATERE_HAUT_DROITE, false));
		path.computeAndFollow(new KeyPathCache(chrono, ScriptNames.SCRIPT_DEPOSE_MINERAI, false));
	}

	@Test
	public void test_strat() throws Exception
	{
		strat.doWinMatch();
	}

}
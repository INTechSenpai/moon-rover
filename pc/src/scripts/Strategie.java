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

package scripts;

import java.util.LinkedList;

import config.Config;
import config.DynamicConfigurable;
import container.Service;
import container.dependances.CoreClass;
import exceptions.MemoryManagerException;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import pathfinding.ChronoGameState;
import pathfinding.KeyPathCache;
import pathfinding.PathCache;
import pathfinding.RealGameState;
import utils.Log;

/**
 * La stratégie : enchaîne les scripts et utilise le pathfinding
 * 
 * @author pf
 *
 */

public class Strategie implements Service, CoreClass, DynamicConfigurable
{
	protected Log log;
	private PathCache pathcache;
	private RealGameState state;
	private ChronoGameState chrono;
	private LinkedList<ScriptsSymetrises> strategie = new LinkedList<ScriptsSymetrises>();
	private boolean symetrie;
	
	public Strategie(Log log, PathCache pathcache, RealGameState state, ChronoGameState chrono)
	{
		this.log = log;
		this.pathcache = pathcache;
		this.state = state;
		this.chrono = chrono;
		strategie.add(ScriptsSymetrises.SCRIPT_CRATERE_HAUT_A_NOUS);
		strategie.add(ScriptsSymetrises.SCRIPT_DEPOSE_MINERAI);
		strategie.add(ScriptsSymetrises.SCRIPT_CRATERE_HAUT_ENNEMI);
		strategie.add(ScriptsSymetrises.SCRIPT_DEPOSE_MINERAI);
	}

	/**
	 * La méthode qui gagne un match
	 * 
	 * @throws InterruptedException
	 */
	public void doWinMatch() throws InterruptedException
	{
		ScriptNames s = strategie.getFirst().getScript(symetrie);
		try
		{
			state.copyAStarCourbe(chrono); // TODO vérifier si la copie est
											// correcte
			s.s.execute(chrono);
			KeyPathCache k = new KeyPathCache(chrono, s, true);
			pathcache.prepareNewPath(k);
			s.s.execute(state);
			pathcache.follow(k);
		}
		catch(PathfindingException | UnableToMoveException | MemoryManagerException e)
		{
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
	}

	@Override
	public synchronized void updateConfig(Config config)
	{
		symetrie = config.getSymmetry();
	}

}

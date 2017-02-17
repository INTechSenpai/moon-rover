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

package scripts;

import java.util.HashMap;
import java.util.LinkedList;

import container.Container;
import container.Service;
import container.dependances.CoreClass;
import exceptions.ContainerException;
import exceptions.PathfindingException;
import pathfinding.PathCache;
import pathfinding.RealGameState;
import table.GameElementNames;
import utils.Log;

/**
 * La stratégie : enchaîne les scripts et utilise le pathfinding
 * @author pf
 *
 */

public class Strategie implements Service, CoreClass
{
	protected Log log;
	private PathCache pathcache;
	private RealGameState state;
	private LinkedList<Script> strategie = new LinkedList<Script>();
	
	public Strategie(Log log, PathCache pathcache, Container container, RealGameState state, ScriptManager scriptsm)
	{
		this.log = log;
		this.pathcache = pathcache;
		this.state = state;
		HashMap<String, Script> scripts = scriptsm.getScripts();
		try {
			strategie.add(scripts.get(GameElementNames.MINERAI_CRATERE_HAUT_GAUCHE.toString()));
			strategie.add(container.make(ScriptDeposeMinerai.class));
		} catch (ContainerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * La méthode qui gagne un match
	 * @throws InterruptedException 
	 */
	public void doWinMatch() throws InterruptedException
	{
		synchronized(state.robot)
		{
			if(!state.robot.isCinematiqueInitialised())
				state.robot.wait();
		}
		Script s = strategie.getFirst();
		try {
			// le chemin est automatiquement exécuté
			pathcache.computeNewPathToScript(state.robot.getCinematique(), s, true);
			
		} catch (PathfindingException e) {
			e.printStackTrace();
		}
		finally
		{
			/*
			 * Dans tous les cas, il faut signaler au pathfinding que la recherche est finie
			 */
			pathcache.stopSearch();
		}
	}
	
}

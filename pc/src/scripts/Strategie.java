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

import container.Service;
import container.dependances.CoreClass;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import pathfinding.ChronoGameState;
import pathfinding.PFInstruction;
import pathfinding.PathCache;
import pathfinding.RealGameState;
import robot.Speed;
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
	private ChronoGameState chrono;
	private LinkedList<Script> strategie = new LinkedList<Script>();
	private PFInstruction inst;
	
	public Strategie(Log log, PathCache pathcache, RealGameState state, ChronoGameState chrono, ScriptManager scriptsm, PFInstruction inst)
	{
		this.log = log;
		this.pathcache = pathcache;
		this.state = state;
		this.chrono = chrono;
		this.inst = inst;
		HashMap<String, Script> scripts = scriptsm.getScripts();
		strategie.add(scripts.get(GameElementNames.MINERAI_CRATERE_HAUT_GAUCHE.toString()));
		strategie.add(new ScriptDeposeMinerai(log));
	}
	
	/**
	 * La méthode qui gagne un match
	 * @throws InterruptedException 
	 */
	public void doWinMatch() throws InterruptedException
	{
		Script s = strategie.getFirst();
		try {
			state.copyAStarCourbe(chrono); // TODO vérifier si la copie est correcte
			s.execute(chrono);
			inst.set(s, true, chrono);
			s.execute(state);
			pathcache.sendPreparedPath();
			state.robot.followTrajectory(Speed.STANDARD); // TODO
		} catch (PathfindingException e) {
			e.printStackTrace();
		} catch (UnableToMoveException e) {
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

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
import pathfinding.KeyPathCache;
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
	private LinkedList<ScriptNames> strategie = new LinkedList<ScriptNames>();
	private PFInstruction inst;
	
	public Strategie(Log log, PathCache pathcache, RealGameState state, ChronoGameState chrono, PFInstruction inst)
	{
		this.log = log;
		this.pathcache = pathcache;
		this.state = state;
		this.chrono = chrono;
		this.inst = inst;
		strategie.add(ScriptNames.SCRIPT_CRATERE_HAUT_DROITE);
		strategie.add(ScriptNames.SCRIPT_DEPOSE_MINERAI);
		strategie.add(ScriptNames.SCRIPT_CRATERE_HAUT_GAUCHE);
		strategie.add(ScriptNames.SCRIPT_DEPOSE_MINERAI);
	}
	
	/**
	 * La méthode qui gagne un match
	 * @throws InterruptedException 
	 */
	public void doWinMatch() throws InterruptedException
	{
		ScriptNames s = strategie.getFirst();
		try {
			state.copyAStarCourbe(chrono); // TODO vérifier si la copie est correcte
			s.s.execute(chrono);
			inst.set(new KeyPathCache(chrono, s, true));
			s.s.execute(state);
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

/*
Copyright (C) 2016 Pierre-François Gimenez

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

import pathfinding.RealGameState;
import pathfinding.astar.arcs.CercleArrivee;
import table.GameElementNames;

/**
 * Le script qui récupère les balles d'un cratère
 * @author pf
 *
 */

public class ScriptCratere extends Script
{
	private GameElementNames element;
	private CercleArrivee cercle;
	
	public ScriptCratere(CercleArrivee cercle, GameElementNames element)
	{
		this.element = element;
		this.cercle = cercle;
	}

	@Override
	public void setUpCercleArrivee()
	{
		cercle.set(element);
	}

	@Override
	public void run(RealGameState state) throws InterruptedException
	{
		state.robot.baisseFilet();
	}

}
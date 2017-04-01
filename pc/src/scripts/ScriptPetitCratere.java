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

import exceptions.UnableToMoveException;
import pathfinding.GameState;
import pathfinding.astar.arcs.CercleArrivee;
import robot.Robot;
import robot.Speed;
import table.EtatElement;
import table.GameElementNames;
import utils.Log;

/**
 * Le script qui récupère les balles d'un petit cratère
 * @author pf
 *
 */

public class ScriptPetitCratere extends Script
{
	private GameElementNames element;
	private CercleArrivee cercle;
	
	public ScriptPetitCratere(Log log, CercleArrivee cercle, GameElementNames element)
	{
		super(log);
		this.element = element;
		this.cercle = cercle;
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof ScriptPetitCratere && other.hashCode() == hashCode();
	}
	
	@Override
	public int hashCode()
	{
		return element.hashCode();
	}
	
	@Override
	public void setUpCercleArrivee()
	{
		// il faut se mettre à 180mm du bord pour récupérer les balles
		cercle.set(element, 220);
	}

	@Override
	protected void run(GameState<? extends Robot> state) throws InterruptedException, UnableToMoveException
	{
		state.robot.avance(-40, Speed.STANDARD);
		state.robot.ouvreFilet();
		state.robot.baisseFilet();
		state.table.setDone(element, EtatElement.PRIS_PAR_NOUS);
	}
	
	@Override
	protected void termine(GameState<? extends Robot> state) throws InterruptedException, UnableToMoveException
	{
		state.robot.fermeFilet();
		state.robot.leveFilet();		
		state.robot.avance(40, Speed.STANDARD);
	}

	@Override
	public String toString()
	{
		return element.name();
	}
}

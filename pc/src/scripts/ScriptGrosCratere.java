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

import java.util.ArrayList;
import java.util.List;

import exceptions.UnableToMoveException;
import pathfinding.GameState;
import pathfinding.SensFinal;
import pathfinding.astar.arcs.CercleArrivee;
import robot.Robot;
import table.GameElementNames;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Le script qui récupère les balles du gros cratère
 * @author pf
 *
 */

public class ScriptGrosCratere extends Script
{
	private List<GameElementNames> elements;
	private double angle;
	private Vec2RO pos;
	private String nom;
	
	public ScriptGrosCratere(String nom)
	{
		this.nom = nom;
		elements = new ArrayList<GameElementNames>();
		Vec2RW pos = new Vec2RW();
		for(GameElementNames n : GameElementNames.values())
			if(n.toString().startsWith(nom))
			{
				elements.add(n);
				pos.plus(n.obstacle.getPosition());
			}
		angle = elements.get(0).orientationArriveeDStarLite;
		pos.scalar(1./elements.size());
		this.pos = pos;
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof ScriptGrosCratere && other.hashCode() == hashCode();
	}
	
	@Override
	public int hashCode()
	{
		return nom.hashCode();
	}
	
	@Override
	public void setUpCercleArrivee()
	{
		cercle.set(pos, angle, 350, SensFinal.MARCHE_ARRIERE);
	}

	@Override
	protected void run(GameState<? extends Robot> state) throws InterruptedException, UnableToMoveException
	{
		// TODO
	}

	@Override
	public String toString()
	{
		return nom;
	}
}

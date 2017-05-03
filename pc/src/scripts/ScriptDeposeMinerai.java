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

import exceptions.ActionneurException;
import exceptions.UnableToMoveException;
import pathfinding.GameState;
import pathfinding.SensFinal;
import pathfinding.astar.arcs.CercleArrivee;
import robot.Robot;
import robot.Speed;
import utils.Log;
import utils.Vec2RO;

/**
 * Le script qui dépose le minerai dans le panier
 * @author pf
 *
 */

public class ScriptDeposeMinerai extends Script
{
	private Vec2RO centre = new Vec2RO(700, 1800);
	private double rayon = 200;

	@Override
	public void setUpCercleArrivee()
	{
		cercle.set(centre, Math.PI, rayon, SensFinal.MARCHE_AVANT);
	}

	@Override
	protected void run(GameState<? extends Robot> state) throws InterruptedException, UnableToMoveException, ActionneurException
	{
		state.robot.avance(-200, Speed.BASCULE);
		state.robot.traverseBascule();
		state.robot.avance(-200, Speed.BASCULE);
		state.robot.ouvreFilet();
		try {
			state.robot.ejecteBalles();
			try {
				state.robot.ejecteBallesAutreCote();
				try {
					state.robot.rearmeAutreCote();
				} catch (ActionneurException e) {
					log.warning(e);
					try {
						state.robot.ejecteBallesAutreCote();
					}
					catch (ActionneurException e1) {
						log.warning(e1);
					}
					try
					{
						state.robot.rearmeAutreCote();
					}
					catch (ActionneurException e1) {
						log.warning(e1);
					}	
				}
			}
			catch (ActionneurException e) {
				log.warning(e);
			}
			finally
			{
				try {
					state.robot.rearme();
				} catch (ActionneurException e) {
					log.warning(e);
					try {
						state.robot.ejecteBalles();
					}
					catch (ActionneurException e1) {
						log.warning(e1);
					}
					try
					{
						state.robot.rearme();
					}
					catch (ActionneurException e1) {
						log.warning(e1);
					}
				}
			}
		}
		catch (ActionneurException e) {
			log.warning(e);
		}
		finally
		{
			state.robot.fermeFilet();
		}
	}
	
	@Override
	public boolean equals(Object other)
	{
		return other instanceof ScriptDeposeMinerai; // de toute façon, il n'y a qu'un seul script de ce type
	}
	
	@Override
	public int hashCode()
	{
		return 0;
	}

	@Override
	public String toString()
	{
		return "DEPOSE";
	}
}

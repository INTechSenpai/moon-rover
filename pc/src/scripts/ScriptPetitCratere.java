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

import exceptions.ActionneurException;
import exceptions.MemoryManagerException;
import exceptions.UnableToMoveException;
import pathfinding.GameState;
import robot.Robot;
import robot.Speed;
import table.EtatElement;
import table.GameElementNames;

/**
 * Le script qui récupère les balles d'un petit cratère
 * 
 * @author pf
 *
 */

public class ScriptPetitCratere extends Script
{
	private GameElementNames element;

	public ScriptPetitCratere(GameElementNames element)
	{
		this.element = element;
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
		cercle.set(element, 250);
	}

	@Override
	protected void run(GameState<? extends Robot> state) throws InterruptedException, UnableToMoveException, MemoryManagerException
	{
		int nbEssai = 3;
		cercle.set(element, 200); // nouveau rayon : 200
		state.robot.avanceToCircle(Speed.STANDARD);
		while(!state.robot.isArrivedAsser())
		{
			if(nbEssai == 0)
				throw new UnableToMoveException("Le robot est encore arrivé au mauvais endroit !");
			log.warning("Le robot n'est pas bien arrivé sur le cratère : on retente.");
			state.robot.avance(40, Speed.STANDARD);
			state.robot.avanceToCircle(Speed.STANDARD);
			nbEssai--;
		}
		
		try
		{
			try
			{
				state.robot.rearme();
			}
			catch(ActionneurException e)
			{
				log.warning(e);
			}

			try
			{
				state.robot.rearmeAutreCote();
			}
			catch(ActionneurException e)
			{
				log.warning(e);
			}

			state.robot.ouvreFilet();

			try
			{
				state.robot.baisseFilet();
			}
			catch(ActionneurException e)
			{
				log.warning(e);
				try
				{
					state.robot.leveFilet();
				}
				catch(ActionneurException e1)
				{
					log.warning(e1);
					state.robot.fermeFilet();
					throw e1;
				}

				try
				{
					state.robot.baisseFilet();
				}
				catch(ActionneurException e1)
				{
					log.warning(e1);
					try
					{
						state.robot.leveFilet();
					}
					catch(ActionneurException e2)
					{
						log.warning(e2);
					}
					state.robot.fermeFilet();
					throw e1;
				}
			}

			state.robot.fermeFilet();
			state.robot.ouvreFilet();
			state.robot.fermeFiletForce();

			try
			{
				state.robot.leveFilet();
			}
			catch(ActionneurException e)
			{
				log.warning(e);
			}

			// le minerai est considéré comme pris
			state.table.setDone(element, EtatElement.PRIS_PAR_NOUS);
			state.robot.setFiletPlein(true);

		}
		catch(ActionneurException e)
		{
			state.robot.setFiletPlein(false);
			log.warning(e);
		}
		finally
		{
			// on se dégage dans tous les cas
			state.robot.avance(50, Speed.STANDARD);
		}
	}
}

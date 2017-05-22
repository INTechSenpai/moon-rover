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
import exceptions.UnableToMoveException;
import pathfinding.GameState;
import pathfinding.SensFinal;
import robot.Robot;
import robot.Speed;
import serie.SerialProtocol.InOrder;
import serie.SerialProtocol.State;
import serie.Ticket;
import utils.Vec2RW;

/**
 * Le script qui dépose le minerai dans le panier
 * 
 * @author pf
 *
 */

public class ScriptDeposeMinerai extends Script
{
	private Vec2RW centre = new Vec2RW(700, 1800);
	private double rayon = 200;
	private boolean gauche;

	public ScriptDeposeMinerai(boolean gauche)
	{
		this.gauche = gauche;
		centre.setX(-centre.getX());
	}
	
	@Override
	public void setUpCercleArrivee()
	{
		cercle.set(centre, Math.PI, rayon, SensFinal.MARCHE_AVANT, null);
	}

	@Override
	protected void run(GameState<? extends Robot> state) throws InterruptedException, UnableToMoveException, ActionneurException
	{
		Ticket t = state.robot.traverseBascule();
		try {
			state.robot.avance(-900, Speed.BASCULE);
		}
		catch(UnableToMoveException e)
		{
			log.warning(e);
			double xRobot = state.robot.getCinematique().getPosition().getX();
			double yRobot = state.robot.getCinematique().getPosition().getY();
			if(yRobot < 1650 || (gauche && (xRobot < -1350 || xRobot > -1140)) || (!gauche && (xRobot < 1140 || xRobot > 1350)))
			{
				log.warning("On est trop loin ! Position : "+state.robot.getCinematique().getPosition());
				throw e;
			}
			log.debug("On a eu un problème en reculant, mais on est bien positionné, alors on continue !");
		}

		InOrder o = t.attendStatus();
		if(o.etat == State.KO)
		{
			try {
				state.robot.leveFilet();
			}
			catch(ActionneurException e)
			{
				log.warning(e);
			}
		}

		state.robot.ouvreFilet();
		try
		{
			state.robot.ejecteBalles();
			try
			{
				state.robot.ejecteBallesAutreCote();
				try
				{
					state.robot.rearmeAutreCote();
				}
				catch(ActionneurException e)
				{
					log.warning(e);
					try
					{
						state.robot.ejecteBallesAutreCote();
					}
					catch(ActionneurException e1)
					{
						log.warning(e1);
					}
					try
					{
						state.robot.rearmeAutreCote();
					}
					catch(ActionneurException e1)
					{
						log.warning(e1);
					}
				}
			}
			catch(ActionneurException e)
			{
				log.warning(e);
			}
			finally
			{
				try
				{
					state.robot.rearme();
				}
				catch(ActionneurException e)
				{
					log.warning(e);
					try
					{
						state.robot.ejecteBalles();
					}
					catch(ActionneurException e1)
					{
						log.warning(e1);
					}
					try
					{
						state.robot.rearme();
					}
					catch(ActionneurException e1)
					{
						log.warning(e1);
					}
				}
			}
		}
		catch(ActionneurException e)
		{
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
		return other instanceof ScriptDeposeMinerai && ((ScriptDeposeMinerai) other).gauche == gauche;
	}

	@Override
	public int hashCode()
	{
		return 64+(gauche ? 1 : 0);
	}
}

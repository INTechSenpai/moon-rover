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

import exceptions.MemoryManagerException;
import exceptions.PathfindingException;
import exceptions.UnableToMoveException;
import robot.RobotColor;
import robot.Speed;
import scripts.ScriptsSymetrises;
import utils.Log;

/**
 * Match
 * 
 * @author pf
 *
 */

public class Match
{
	public static void main(String[] args)
	{
		MatchUtilitary match = null;
		Log log = null;
		boolean simuleSerie = false;
		try {
			match = new MatchUtilitary();
			try
			{
				match.setUp(RobotColor.JAUNE, ScriptsSymetrises.SCRIPT_CRATERE_HAUT_A_NOUS);
				simuleSerie = match.getSimuleSerie();
				log = match.getLog();
				match.doTheFirstBarrelRoll(Speed.TEST);				
				match.doABarrelRoll(ScriptsSymetrises.SCRIPT_DEPOSE_MINERAI, Speed.STANDARD);
			}
			catch(PathfindingException | UnableToMoveException | MemoryManagerException e)
			{
				e.printStackTrace();
				e.printStackTrace(log.getPrintWriter());
			}
			finally
			{
				match.doABarrelRoll(ScriptsSymetrises.SCRIPT_CRATERE_BAS_A_NOUS, Speed.TEST);
				if(simuleSerie)
					Thread.sleep(2000);

				match.doABarrelRoll(ScriptsSymetrises.SCRIPT_DEPOSE_MINERAI_FIN, Speed.STANDARD);

				if(simuleSerie)
					Thread.sleep(10000);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(log != null)
				e.printStackTrace(log.getPrintWriter());
		}
		finally
		{
			match.stop();
		}
	}
}
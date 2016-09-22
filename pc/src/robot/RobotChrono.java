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

package robot;

import pathfinding.astarCourbe.arcs.ArcCourbe;
import utils.Log;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 */

public class RobotChrono extends Robot
{
	// Date en millisecondes depuis le début du match.
	protected long date;

	/**
	 * Constructeur clone
	 * @param log
	 * @param robot
	 */
	public RobotChrono(Log log, RobotReal robot)
	{
		super(log);
		robot.copy(this);
	}

	@Override
	public long getTempsDepuisDebutMatch()
	{
		return date;
	}
	
	public void suitArcCourbe(ArcCourbe came_from_arc)
	{
		date += came_from_arc.getDuree();
		came_from_arc.getLast().copy(cinematique);
	}
	
	public Cinematique getCinematique()
	{
		return cinematique;
	}
	
}

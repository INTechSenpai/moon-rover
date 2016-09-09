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

package pathfinding;

import robot.RobotReal;
import container.Service;
import table.Table;
import utils.Config;

/**
 * Utilisé par les scripts
 * @author pf
 *
 */

public class RealGameState extends GameState<RobotReal> implements Service
{
    public RealGameState(RobotReal robot, Table table)
    {
        this.robot = robot;
        this.table = table;
    }
    
    @Override
    public void updateConfig(Config config)
    {}

    @Override
    public void useConfig(Config config)
    {}
    
    @Override
	public final void copyAStarCourbe(ChronoGameState modified)
    {
    	table.copy(modified.table);
        robot.copy(modified.robot);
        modified.iterator.init(System.currentTimeMillis());
    }

}

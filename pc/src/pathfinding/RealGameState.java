package pathfinding;

import robot.RobotChrono;
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

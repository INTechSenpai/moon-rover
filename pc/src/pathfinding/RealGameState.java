package pathfinding;

import robot.RobotChrono;
import robot.RobotReal;
import container.Service;
import table.Table;
import utils.Log;
import utils.Config;

/**
 * Utilisé par les scripts
 * @author pf
 *
 */

public class RealGameState extends GameState<RobotReal> implements Service
{
	// cet iterator et cette table sont ceux du gridspace. Modifier l'un modifie l'autre.
    protected Log log;
    
    public RealGameState(Log log, RobotReal robot, Table table)
    {
        this.log = log;
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
        robot.copy((RobotChrono) modified.robot);
        modified.iterator.init(System.currentTimeMillis());
    }

}

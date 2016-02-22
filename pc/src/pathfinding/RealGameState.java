package pathfinding;

import obstacles.memory.ObstaclesIteratorFutur;
import obstacles.memory.ObstaclesMemory;
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

public class RealGameState extends GameState implements Service
{
	// cet iterator et cette table sont ceux du gridspace. Modifier l'un modifie l'autre.
    protected Log log;
    private ObstaclesMemory memory;
    
    public RealGameState(Log log, RobotReal robot, ObstaclesMemory memory, Table table)
    {
        this.log = log;
        this.robot = robot;
        this.memory = memory;
        this.table = table;
    }
    
	/**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si l'original est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
	public final ChronoGameState cloneGameState()
	{
		ChronoGameState cloned = new ChronoGameState(log, robot.cloneIntoRobotChrono(), new ObstaclesIteratorFutur(log, memory), table.clone());
		return cloned;
	}

    @Override
    public void updateConfig(Config config)
    {}

    @Override
    public void useConfig(Config config)
    {}
    
    public final void copyAStarCourbe(ChronoGameState modified)
    {
    	table.copy(modified.table);
        robot.copyThetaStar((RobotChrono) modified.robot);
        modified.iterator.init(System.currentTimeMillis());
    }

}

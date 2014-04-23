package strategie;

import container.Service;
import pathfinding.Pathfinding;
import robot.Robot;
import robot.RobotChrono;
import table.Table;
import utils.Log;
import utils.Read_Ini;

public class GameState<R extends Robot> implements Service
{    
    public Table table;
    public R robot;
    public Pathfinding pathfinding;
    private Log log;
    private Read_Ini config;
        
    public GameState(Read_Ini config, Log log, Table table, R robot, Pathfinding pathfinding)
    {
        this.config = config;
        this.log = log;
        this.table = table;
        this.robot = robot;
        this.pathfinding = pathfinding;
    }
    
    /**
     * Fournit un clone de this. Le clone sera un GameState<RobotChrono>, peu importe si this est un GameState<RobotVrai> ou un GameState<RobotChrono>
     */
    public GameState<RobotChrono> clone()
    {
        Table new_table = table.clone();
        RobotChrono new_rc = new RobotChrono(config, log); 
        robot.copy(new_rc);
        Pathfinding new_pf = new Pathfinding(new_table, config, log);
        GameState<RobotChrono> out = new GameState<RobotChrono>(config, log, new_table, new_rc, new_pf);
        return out;
    }

    /**
     * Copie this dans other. this reste inchangé.
     * @param other
     */
    public void copy(GameState<RobotChrono> other)
    {
        table.copy(other.table);
        robot.copy(other.robot);
        pathfinding.copy(other.pathfinding);
        pathfinding.update();
    }

    @Override
    public void maj_config()
    {
        table.maj_config();
        robot.maj_config();
        pathfinding.maj_config();
    }

}

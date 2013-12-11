package scripts;

import pathfinding.Pathfinding;
import hook.HookGenerator;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotVrai;
import table.Table;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import container.Service;

 /**
  * Classe enregistrée comme service qui fournira les scripts
  * @author pf
  */
 
public class ScriptManager implements Service {
	
	private Pathfinding pathfinding;
	private ThreadTimer threadtimer;
	private Robot robot;
	private RobotVrai robotvrai;
	private RobotChrono robotchrono;
	private HookGenerator hookgenerator;
	private Table table;
	private Read_Ini config;
	private Log log;
	
	public Script[] scripts;
	
	public ScriptManager(Service pathfinding, Service threadtimer, Service robotvrai, Service robotchrono, Service hookgenerator, Service table, Service config, Service log) {
		this.pathfinding = (Pathfinding) pathfinding;
		this.threadtimer = (ThreadTimer) threadtimer;
		this.robotvrai = (RobotVrai) robotvrai;
		this.robotchrono = (RobotChrono) robotchrono;
		this.hookgenerator = (HookGenerator) hookgenerator;
		this.table = (Table) table;
		this.config = (Read_Ini) config;
		this.log = (Log) log;

	}
	
	// TODO
	public Script[] scriptsRestants()
	{
		return null;
	}
	
}

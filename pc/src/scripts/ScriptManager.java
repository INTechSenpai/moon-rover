package scripts;

import java.util.ArrayList;

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
	
	public String[] scripts;
	
	public ScriptManager(Pathfinding pathfinding, ThreadTimer threadtimer, RobotVrai robotvrai, RobotChrono robotchrono, HookGenerator hookgenerator, Table table, Read_Ini config, Log log) {
		this.pathfinding = pathfinding;
		this.threadtimer = threadtimer;
		this.robotvrai = robotvrai;
		this.robotchrono = robotchrono;
		this.hookgenerator = hookgenerator;
		this.table = table;
		this.config = config;
		this.log = log;

	}
	
	// TODO
	public ArrayList<Script> scriptsRestants()
	{
		return null;
	}

	// TODO
	public Script getScript(String nom, Table table, Robot robot, Pathfinding pathfinding)
	{
		return null;
//		if(nom == "ScriptPosition")
//			return new Script
	}
	
	// TODO
	public int[] getId(String nom_script)
	{
		return null;
	}
	
}

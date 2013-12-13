package scripts;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import pathfinding.Pathfinding;
import hook.HookGenerator;
import robot.RobotChrono;
import robot.RobotVrai;
import table.Table;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exception.ScriptException;

 /**
  * Classe enregistrée comme service qui fournira les scripts
  * @author pf
  */
 
public class ScriptManager implements Service {
	
	private Read_Ini config;
	private Log log;
	
	private Map<String,Script> instancesScripts = new Hashtable<String,Script>();

	public String[] scripts;
	
	public ScriptManager(Pathfinding pathfinding, ThreadTimer threadtimer, RobotVrai robotvrai, RobotChrono robotchrono, HookGenerator hookgenerator, Table table, Read_Ini config, Log log) {
		this.config = config;
		this.log = log;

		instancesScripts.put("ScriptTree", new ScriptTree(pathfinding, threadtimer, robotvrai, robotchrono, hookgenerator, table, config, log));
		instancesScripts.put("ScriptLances", new ScriptTree(pathfinding, threadtimer, robotvrai, robotchrono, hookgenerator, table, config, log));
		
	}
	
	// TODO
	public ArrayList<Script> scriptsRestants()
	{
		return null;
	}

	// TODO
	public Script getScript(String nom, Table table, RobotChrono robotchrono, Pathfinding pathfinding) throws ScriptException
	{
		Script script = instancesScripts.get(nom);
		if(script == null)
			throw new ScriptException();
		script.setRobotChrono(robotchrono);
		script.setTable(table);
		return script;
	}
	
	// TODO
	public int[] getId(String nom_script)
	{
		return null;
	}
	
}

package scripts;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import pathfinding.Pathfinding;
import robot.RobotVrai;
import hook.HookGenerator;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exception.ScriptException;

 /**
  * Classe enregistrée comme service qui fournira les scripts
  * @author pf
  */
 
public class ScriptManager implements Service {
	
	private Log log;

	private Map<String,Script> instancesScripts = new Hashtable<String,Script>();

	private ArrayList<String> scripts_robot1;
	private ArrayList<String> scripts_robot2;
	
	public ScriptManager(Pathfinding pathfinding, HookGenerator hookgenerator, Read_Ini config, Log log, RobotVrai robotvrai)
	{
		this.log = log;
		
		instancesScripts.put("ScriptTree", new ScriptTree(pathfinding, hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptLances", new ScriptLances(pathfinding, hookgenerator, config, log, robotvrai));
		
		scripts_robot1 = new ArrayList<String>();
		scripts_robot1.add("ScriptTree");
		scripts_robot1.add("ScriptLances");

		scripts_robot2 = new ArrayList<String>();
	}
	
	public ArrayList<String> getNomsScripts(int id_robot)
	{
		// if assez moche, il faudrait chercher à passer outre
		if(id_robot == 1)
			return scripts_robot1;
		else
			return scripts_robot2;
	}

	public Script getScript(String nom) throws ScriptException
	{
		Script script = instancesScripts.get(nom);
		if(script == null)
		{
			log.warning("Script inconnu: "+nom, this);
			throw new ScriptException();
		}
		return script;
	}
	
}

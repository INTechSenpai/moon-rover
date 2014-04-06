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

	private ArrayList<String> scripts_robot0;
	private ArrayList<String> scripts_robot1;
	
	public ScriptManager(HookGenerator hookgenerator, Read_Ini config, Log log, RobotVrai robotvrai)
	{
		this.log = log;
		
		instancesScripts.put("ScriptTree", new ScriptTree(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptLances", new ScriptLances(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptDeposerFeu", new ScriptDeposerFeu(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptTorche", new ScriptTorche(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptFresque", new ScriptFresque(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptDeposerFruits", new ScriptDeposerFruits(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptFunnyAction", new ScriptFunnyAction(hookgenerator, config, log, robotvrai));
		
		scripts_robot0 = new ArrayList<String>();
		scripts_robot0.add("ScriptTree");
		scripts_robot0.add("ScriptLances");
		scripts_robot0.add("ScriptFresque");
		scripts_robot0.add("ScriptTorche");
		scripts_robot0.add("ScriptDeposerFruits");
		scripts_robot0.add("ScriptFunnyAction");
		scripts_robot0.add("ScriptDeposerFeu");

		scripts_robot1 = new ArrayList<String>();
	}
	
	public ArrayList<String> getNomsScripts(int id_robot)
	{
		// if assez moche, il faudrait chercher à passer outre
		if(id_robot == 0)
			return scripts_robot0;
		else
			return scripts_robot1;
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
	
	public void maj_config()
	{
		// TODO
	}
}

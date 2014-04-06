package scripts;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import robot.RobotVrai;
import hook.HookGenerator;
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
	
	private Log log;

	private Map<String,Script> instancesScripts = new Hashtable<String,Script>();

	private ArrayList<String> scripts_robot;
	
	public ScriptManager(HookGenerator hookgenerator, ThreadTimer threadtimer, Read_Ini config, Log log, RobotVrai robotvrai)
	{
		this.log = log;
		
		instancesScripts.put("ScriptTree", new ScriptTree(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptLances", new ScriptLances(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptDeposerFeu", new ScriptDeposerFeu(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptTorche", new ScriptTorche(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptFresque", new ScriptFresque(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptDeposerFruits", new ScriptDeposerFruits(hookgenerator, config, log, robotvrai));
		instancesScripts.put("ScriptFunnyAction", new ScriptFunnyAction(hookgenerator, config, log, robotvrai, threadtimer));
		
		scripts_robot = new ArrayList<String>();
		scripts_robot.add("ScriptTree");
		scripts_robot.add("ScriptLances");
		scripts_robot.add("ScriptFresque");
		scripts_robot.add("ScriptTorche");
		scripts_robot.add("ScriptDeposerFruits");
		scripts_robot.add("ScriptFunnyAction");
		scripts_robot.add("ScriptDeposerFeu");
	}
	
	public ArrayList<String> getNomsScripts()
	{
		// if assez moche, il faudrait chercher à passer outre
		return scripts_robot;
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
	}
}

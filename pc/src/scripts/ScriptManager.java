package scripts;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import pathfinding.Pathfinding;
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

	public ScriptManager(Pathfinding pathfinding, ThreadTimer threadtimer, HookGenerator hookgenerator, Read_Ini config, Log log) {
		this.log = log;
		
		instancesScripts.put("ScriptTree", new ScriptTree(pathfinding, threadtimer, hookgenerator, config, log));
		instancesScripts.put("ScriptLances", new ScriptTree(pathfinding, threadtimer, hookgenerator, config, log));
		
	}
	
	public Set<String> getNomsScripts()
	{
		return instancesScripts.keySet();
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

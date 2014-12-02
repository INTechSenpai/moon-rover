package scripts;

import hook.types.HookFactory;

import utils.Log;
import utils.Config;
import container.Service;
import enums.ScriptNames;
import exceptions.UnknownScriptException;

 /**
  * Classe enregistrée comme service qui fournira les scripts
  * @author pf, marsu
  */
 
public class ScriptManager implements Service
{
	
	private Log log;

	private Script[] instancesScripts = new Script[ScriptNames.values().length];

	public ScriptManager(HookFactory hookfactory, Config config, Log log)
	{
		this.log = log;
//		instancesScripts[ScriptNames.script_A.ordinal()] = new Script_A(hookfactory, config, log);
	}
	
	public Script getScript(ScriptNames nom) throws UnknownScriptException
	{
		Script script = instancesScripts[nom.ordinal()];
		if(script == null)
		{
			log.warning("Script inconnu: "+nom, this);
			throw new UnknownScriptException();
		}
		return script;
	}
	
	public void updateConfig()
	{
	}

}

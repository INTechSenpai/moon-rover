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

// DEPENDS_ON_RULES
public class ScriptManager implements Service
{
	
	private Script[] instancesScripts = new Script[ScriptNames.values().length];

	public ScriptManager(HookFactory hookfactory, Config config, Log log) throws UnknownScriptException
	{
		instancesScripts[ScriptNames.ScriptClap.ordinal()] = new ScriptClap(hookfactory, config, log);
		instancesScripts[ScriptNames.ScriptTapis.ordinal()] = new ScriptTapis(hookfactory, config, log);
		instancesScripts[ScriptNames.SortieZoneDepart.ordinal()] = new SortieZoneDepart(hookfactory, config, log);
		instancesScripts[ScriptNames.ScriptAttente.ordinal()] = new ScriptAttente(hookfactory, config, log);

		for(int i = 0; i < ScriptNames.values().length; i++)
			if(instancesScripts[i] == null)
			{
				log.warning("Script non instancié: "+ScriptNames.values()[i], this);
				throw new UnknownScriptException();
			}
		updateConfig();
	}
	
	public Script getScript(ScriptNames nom)
	{
		Script script = instancesScripts[nom.ordinal()];
		return script;
	}
	
	public void updateConfig()
	{}

}
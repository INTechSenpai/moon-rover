package scripts;

import hook.HookFactory;
import scripts.anticipables.ScriptAttente;
import scripts.anticipables.ScriptCherchePlot;
import scripts.anticipables.ScriptClap;
import scripts.anticipables.ScriptTapis;
import scripts.anticipables.SortieZoneDepart;
import scripts.hooks.ScriptFunnyAction;
import scripts.hooks.ScriptPrendPlot;
import utils.Log;
import utils.Config;
import container.Service;
import exceptions.UnknownScriptException;

 /**
  * Classe enregistrée comme service qui fournira les scripts anticipables et de hook
  * @author pf, marsu
  */

public class ScriptManager implements Service
{
	
	private Script[] instancesScriptsAnticipables = new Script[ScriptAnticipableNames.values().length];
	private Script[] instancesScriptsHook = new Script[ScriptHookNames.values().length];
	
	public ScriptManager(HookFactory hookfactory, Config config, Log log) throws UnknownScriptException
	{
		// DEPENDS_ON_RULES
		instancesScriptsAnticipables[ScriptAnticipableNames.ScriptClap.ordinal()] = new ScriptClap(hookfactory, config, log);
		instancesScriptsAnticipables[ScriptAnticipableNames.ScriptTapis.ordinal()] = new ScriptTapis(hookfactory, config, log);
		instancesScriptsAnticipables[ScriptAnticipableNames.SortieZoneDepart.ordinal()] = new SortieZoneDepart(hookfactory, config, log);
		instancesScriptsAnticipables[ScriptAnticipableNames.ScriptAttente.ordinal()] = new ScriptAttente(hookfactory, config, log);
		instancesScriptsAnticipables[ScriptAnticipableNames.ScriptCherchePlot.ordinal()] = new ScriptCherchePlot(hookfactory, config, log);

		instancesScriptsHook[ScriptHookNames.FUNNY_ACTION.ordinal()] = new ScriptFunnyAction(hookfactory, config, log);
		instancesScriptsHook[ScriptHookNames.SCRIPT_PREND_PLOT.ordinal()] = new ScriptPrendPlot(hookfactory, config, log);
		
		for(int i = 0; i < ScriptAnticipableNames.values().length; i++)
			if(instancesScriptsAnticipables[i] == null)
			{
				log.warning("Script non instancié: "+ScriptAnticipableNames.values()[i], this);
				throw new UnknownScriptException();
			}
		for(int i = 0; i < ScriptHookNames.values().length; i++)
			if(instancesScriptsHook[i] == null)
			{
				log.warning("Script non instancié: "+ScriptHookNames.values()[i], this);
				throw new UnknownScriptException();
			}

		updateConfig();
	}

	public Script getScript(ScriptAnticipableNames nom)
	{
		Script script = instancesScriptsAnticipables[nom.ordinal()];
		return script;
	}

	public Script getScript(ScriptHookNames nom)
	{
		Script script = instancesScriptsHook[nom.ordinal()];
		return script;
	}

	public void updateConfig()
	{}

}
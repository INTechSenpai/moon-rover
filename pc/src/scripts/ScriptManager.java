package scripts;

import pathfinding.dstarlite.GridSpace;
import hook.HookFactory;
import scripts.anticipables.ScriptAttente;
import scripts.anticipables.SortieZoneDepart;
import scripts.hooks.ScriptHookExemple;
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
	
	private ScriptAnticipable[] instancesScriptsAnticipables = new ScriptAnticipable[ScriptAnticipableNames.values().length];
	private ScriptHook[] instancesScriptsHook = new ScriptHook[ScriptHookNames.values().length];
	
	public ScriptManager(HookFactory hookfactory, Log log, GridSpace gridspace) throws UnknownScriptException
	{
		// DEPENDS_ON_RULES
		instancesScriptsAnticipables[ScriptAnticipableNames.SORTIE_ZONE_DEPART.ordinal()] = new SortieZoneDepart(hookfactory, log, gridspace);
		instancesScriptsAnticipables[ScriptAnticipableNames.ATTENTE.ordinal()] = new ScriptAttente(hookfactory, log, gridspace);

		instancesScriptsHook[ScriptHookNames.EXEMPLE.ordinal()] = new ScriptHookExemple(hookfactory, log);
		instancesScriptsHook[ScriptHookNames.FUNNY_ACTION.ordinal()] = new ScriptHookExemple(hookfactory, log);
				
		for(int i = 0; i < ScriptAnticipableNames.values().length; i++)
			if(instancesScriptsAnticipables[i] == null)
			{
				log.warning("Script non instancié: "+ScriptAnticipableNames.values()[i]);
				throw new UnknownScriptException();
			}
		for(int i = 0; i < ScriptHookNames.values().length; i++)
			if(instancesScriptsHook[i] == null)
			{
				log.warning("Script non instancié: "+ScriptHookNames.values()[i]);
				throw new UnknownScriptException();
			}
	}

	/**
	 * Récupère un script anticipable
	 * @param nom
	 * @return
	 */
	public ScriptAnticipable getScript(ScriptAnticipableNames nom)
	{
		ScriptAnticipable script = instancesScriptsAnticipables[nom.ordinal()];
		return script;
	}

	/**
	 * Récupère un script de hook
	 * @param nom
	 * @return
	 */
	public ScriptHook getScript(ScriptHookNames nom)
	{
		ScriptHook script = instancesScriptsHook[nom.ordinal()];
		return script;
	}

	@Override
	public void useConfig(Config config)
	{
		for(int i = 0; i < ScriptAnticipableNames.values().length; i++)
			if(instancesScriptsAnticipables[i] == null)
				instancesScriptsAnticipables[i].useConfig(config);
		for(int i = 0; i < ScriptHookNames.values().length; i++)
			if(instancesScriptsHook[i] == null)
				instancesScriptsHook[i].useConfig(config);
	}

	@Override
	public void updateConfig(Config config)
	{
		for(int i = 0; i < ScriptAnticipableNames.values().length; i++)
			if(instancesScriptsAnticipables[i] == null)
				instancesScriptsAnticipables[i].updateConfig(config);
		for(int i = 0; i < ScriptHookNames.values().length; i++)
			if(instancesScriptsHook[i] == null)
				instancesScriptsHook[i].updateConfig(config);
	}

}
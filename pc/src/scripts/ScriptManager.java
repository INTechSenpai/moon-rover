package scripts;

import hook.HookFactory;
import scripts.anticipables.Poissons;
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
	
	public ScriptManager(HookFactory hookfactory, Log log) throws UnknownScriptException
	{
		// DEPENDS_ON_RULES
		instancesScriptsAnticipables[ScriptAnticipableNames.SORTIE_ZONE_DEPART.ordinal()] = new Poissons(hookfactory, log);

		for(int i = 0; i < ScriptAnticipableNames.values().length; i++)
			if(instancesScriptsAnticipables[i] == null)
			{
				log.warning("Script non instancié: "+ScriptAnticipableNames.values()[i]);
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

	@Override
	public void useConfig(Config config)
	{
		for(int i = 0; i < ScriptAnticipableNames.values().length; i++)
			if(instancesScriptsAnticipables[i] == null)
				instancesScriptsAnticipables[i].useConfig(config);
	}

	@Override
	public void updateConfig(Config config)
	{
		for(int i = 0; i < ScriptAnticipableNames.values().length; i++)
			if(instancesScriptsAnticipables[i] == null)
				instancesScriptsAnticipables[i].updateConfig(config);
	}

}
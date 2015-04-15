package scripts;

import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.ScriptHookException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import robot.RobotReal;
import strategie.GameState;
import table.GameElementNames;
import utils.Config;
import utils.Log;

public abstract class ScriptHook
{
	protected HookFactory hookfactory;
	protected Config config;
	protected Log log;

	public ScriptHook(HookFactory hookgenerator, Config config, Log log)
	{
		this.hookfactory = hookgenerator;
		this.config = config;
		this.log = log;
		updateConfig();
	}

	/**
	 * Surcouche d'exécute, avec une gestion d'erreur.
	 * On ne vérifie pas le point d'entrée pour les scripts de hook
	 * Peut être appelé avec un RobotReal ou un RobotChrono
	 * @param id_version
	 * @param state
	 * @throws ScriptException
	 * @throws FinMatchException
	 * @throws ScriptHookException
	 */
	public final void agit(GameElementNames id_version, GameState<RobotReal> state) throws ScriptException, FinMatchException
	{
		log.debug("Agit script hook version "+id_version);
		try
		{
			execute(id_version, state);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			// le script a échoué, on prévient le haut niveau
			throw new ScriptException();
		}
		finally
		{
			try {
				termine(state);
			} catch (SerialConnexionException e) {
				try {
					state.robot.sleep(100); // on attends un petit peu...
					termine(state);  // on réessaye encore une fois
				} catch (SerialConnexionException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}

	}

	protected abstract void execute(GameElementNames id_version, GameState<RobotReal>state) throws UnableToMoveException, SerialConnexionException, FinMatchException;
	
	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs, on se décale du mur, ...
	 * A priori sans avoir besoin du numéro de version; si besoin est, à rajouter en paramètre.
	 * @throws ScriptHookException 
	 */
	protected abstract void termine(GameState<RobotReal> gamestate) throws ScriptException, FinMatchException, SerialConnexionException;

	public void updateConfig()
	{}
}

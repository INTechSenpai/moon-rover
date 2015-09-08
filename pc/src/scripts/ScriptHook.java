package scripts;

import pathfinding.GameState;
import permissions.ReadWrite;
import exceptions.FinMatchException;
import exceptions.ScriptException;
import exceptions.UnableToMoveException;
import hook.HookFactory;
import table.GameElementNames;
import utils.Config;
import utils.Log;

public abstract class ScriptHook
{
	protected HookFactory hookfactory;
	protected Config config;
	protected Log log;
	
	protected volatile boolean symetrie;

	public ScriptHook(HookFactory hookgenerator, Log log)
	{
		this.hookfactory = hookgenerator;
		this.log = log;
	}

	/**
	 * Surcouche d'exécute, avec une gestion d'erreur.
	 * On ne vérifie pas le point d'entrée pour les scripts de hook
	 * Peut être appelé avec un RobotReal ou un RobotChrono
	 * @param id_version
	 * @param state
	 * @throws ScriptException
	 * @throws FinMatchException
	 */
	public final void agit(GameElementNames id_version, GameState<?,ReadWrite> state) throws ScriptException, FinMatchException
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
			termine(state);
		}

	}

	protected abstract void execute(GameElementNames id_version, GameState<?,ReadWrite>state) throws UnableToMoveException, FinMatchException;
	
	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs, on se décale du mur, ...
	 * A priori sans avoir besoin du numéro de version; si besoin est, à rajouter en paramètre.
	 * @throws ScriptException 
	 * @throws FinMatchException 
	 */
	protected abstract void termine(GameState<?,ReadWrite> gamestate) throws ScriptException, FinMatchException;

	/**
	 * Le robot peut-il effectuer cette action ?
	 * (il ne peut par exemple pas prendre un objet si sa pile est pleine, etc.)
	 * Utilisé lors de la planification de trajectoire
	 * @param gamestate
	 * @return
	 */
	protected abstract boolean isPossible(GameState<?,ReadWrite> gamestate);
	
	public void updateConfig(Config config)
	{
		symetrie = config.getSymmetry();
	}

	public void useConfig(Config config)
	{}
}

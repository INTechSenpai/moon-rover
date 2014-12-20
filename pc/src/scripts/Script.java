package scripts;

import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Config;
import container.Service;
import hook.types.HookFactory;

import java.util.ArrayList;

import enums.ConfigInfo;
import enums.PathfindingNodes;
import enums.RobotColor;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import exceptions.strategie.ScriptException;
/**
 * Classe abstraite dont héritent les différents scripts.
 * S'occupe de robotvrai et robotchrono de manière à ce que ce soit transparent pour les différents scripts
 * @author pf, marsu
 */

public abstract class Script implements Service 
{

	// Ces services resteront toujours les mêmes, on les factorise avec un static
	protected HookFactory hookfactory;
	protected Config config;
	protected Log log;
	protected RobotColor color;
	
	/**
	 * Renvoie le tableau des méta-verions d'un script
	 * @return le tableau des méta-versions possibles
	 */
	public abstract ArrayList<Integer> meta_version(final GameState<?> state);

	public Script(HookFactory hookgenerator, Config config, Log log)
	{
		this.hookfactory = hookgenerator;
		this.config = config;
		this.log = log;
		updateConfig();
	}

	public void agit(int id_version, GameState<?> state) throws ScriptException, FinMatchException, ScriptHookException
	{
		if(state.robot.getPosition().squaredDistance(point_entree(id_version).getCoordonnees()) > 400) // tolérance de 2cm
		{
			log.critical("Appel d'un script à une mauvaise position. Le robot devrait être en "+point_entree(id_version)+" et est en "+state.robot.getPosition(), this);
			throw new ScriptException();
		}
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
					termine(state);  // on réessaye encore une fois
				} catch (SerialConnexionException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}

	}

	/**
	 * Retourne la position d'entrée associée à la version id
	 * Ne se préoccupe pas de savoir si cette version est possible ou pas.
	 * @param id de la version
	 * @return la position du point d'entrée
	 */
	public abstract PathfindingNodes point_entree(int id);

	// Utilisé uniquement par robotchrono
	public abstract PathfindingNodes point_sortie(int id);

	public abstract void setPointSortie(int id, Vec2 position);
	
	/**
	 * Exécute ou calcule le script, avec RobotVrai ou RobotChrono
	 * @throws SerialConnexionException 
	 * @throws ScriptHookException 
	 */
	protected abstract void execute(int id_version, GameState<?>state) throws UnableToMoveException, SerialConnexionException, FinMatchException, ScriptHookException;

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs, on se décale du mur, ...
	 * @throws ScriptHookException 
	 */
	abstract protected void termine(GameState<?> state) throws SerialConnexionException, FinMatchException, ScriptHookException;
	
	public void updateConfig()
	{
		color = RobotColor.parse(config.get(ConfigInfo.COULEUR));
	}

}

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
import exceptions.PointSortieException;
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
	
	private int squared_tolerance_depart_script = 400;
	
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

	public final void agit(int id_version, GameState<?> state) throws ScriptException, FinMatchException, ScriptHookException
	{
		PathfindingNodes pointEntree = point_entree(id_version);
		if(pointEntree != null && state.robot.getPosition().squaredDistance(pointEntree.getCoordonnees()) > squared_tolerance_depart_script)
		{
			log.critical("Appel d'un script à une mauvaise position. Le robot devrait être en "+pointEntree+" et est en "+state.robot.getPosition(), this);
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
					state.robot.sleep(100); // on attends un petit peu...
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

	/**
	 * Utilisé par robotchrono
	 * @param id
	 * @return
	 */
	public abstract PathfindingNodes point_sortie(int id);

	public final void checkPointSortie(int id, Vec2 position) throws PointSortieException
	{
		PathfindingNodes sortie = point_sortie(id);
		if(!position.equals(sortie.getCoordonnees()))
		{
			log.critical("Position de "+sortie+" incorrecte! Sa bonne position est: "+position, this);
			throw new PointSortieException();
		}

	}
	
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
		color = config.getColor();
		squared_tolerance_depart_script = config.getInt(ConfigInfo.TOLERANCE_DEPART_SCRIPT);
		squared_tolerance_depart_script *= squared_tolerance_depart_script; // on en utilise le carré
	}

}

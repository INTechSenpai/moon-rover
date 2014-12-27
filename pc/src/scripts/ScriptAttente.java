package scripts;

import java.util.ArrayList;

import enums.PathfindingNodes;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import hook.types.HookFactory;
import robot.RobotChrono;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * N'est pas vraiment un script à proprement parler. C'est juste une attente.
 * Mis sous la forme d'un script afin d'être utilisé dans la stratégie
 * @author pf
 *
 */

public class ScriptAttente extends Script
{

	private PathfindingNodes entree_sortie = null;
	
	public ScriptAttente(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	public ArrayList<Integer> getVersions(GameState<?> state) {
		ArrayList<Integer> version = new ArrayList<Integer>();
		if(state.robot instanceof RobotChrono)
		{
			entree_sortie = ((RobotChrono)state.robot).getPositionPathfinding();
			if(entree_sortie != null)
				version.add(0);
		}
		return version;
	}

	@Override
	public PathfindingNodes point_entree(int id) {
		// ce qui signifie: on ne bouge pas
		return entree_sortie;
	}

	@Override
	public PathfindingNodes point_sortie(int id) {
		// ce qui signifie: on ne bouge pas
		return entree_sortie;
	}

	@Override
	protected void execute(int id_version, GameState<?> state)
			throws UnableToMoveException, SerialConnexionException,
			FinMatchException, ScriptHookException
	{
		state.robot.sleep(2000);
	}

	@Override
	protected void termine(GameState<?> state) throws SerialConnexionException,
			FinMatchException, ScriptHookException
	{
	}

}

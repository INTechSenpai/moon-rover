package scripts;

import java.util.ArrayList;

import enums.PathfindingNodes;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import hook.types.HookFactory;
import strategie.GameState;
import utils.Config;
import utils.Log;

/**
 * Faux script. Permet de sortir de la zone de départ.
 * Pas de version. Exécutée dès le début du match.
 * Permet de calculer une stratégie à la sortie de ce "script"
 * @author pf
 *
 */

public class SortieZoneDepart extends Script {

	public SortieZoneDepart(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	public ArrayList<Integer> getVersions(GameState<?> state) {
		ArrayList<Integer> out = new ArrayList<Integer>();
		out.add(0);
		return out;
	}

	@Override
	public PathfindingNodes point_entree(int id) {
		return PathfindingNodes.POINT_DEPART;
	}

	@Override
	public PathfindingNodes point_sortie(int id) {
		return PathfindingNodes.SORTIE_ZONE_DEPART;
	}

	@Override
	protected void execute(int id_version, GameState<?> state)
			throws UnableToMoveException, SerialConnexionException,
			FinMatchException, ScriptHookException
	{
		state.robot.tourner(Math.PI);
		state.robot.avancer(500); // TODO vérifier distance
	}

	@Override
	protected void termine(GameState<?> state) throws SerialConnexionException, FinMatchException, ScriptHookException
	{}
	
}

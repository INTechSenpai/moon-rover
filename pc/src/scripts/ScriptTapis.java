package scripts;

import java.util.ArrayList;

import enums.PathfindingNodes;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import hook.types.HookFactory;
import smartMath.Vec2;
import strategie.GameState;
import utils.Config;
import utils.Log;

public class ScriptTapis extends Script {

	public ScriptTapis(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override
	public ArrayList<Integer> meta_version(GameState<?> state)
	{
		// Si les tapis sont posés, pas de métaversion possible
		ArrayList<Integer> out = new ArrayList<Integer>();
		if(state.robot.areTapisPoses())
			return out;
		
		// Sinon, on renvoie uniquement la méta_version 0
		out.add(0);
		return out;
	}

	@Override
	public PathfindingNodes point_entree(int id)
	{
		return PathfindingNodes.NODE_TAPIS;
	}

	@Override
	public void execute(int id_version, GameState<?> state) throws UnableToMoveException, SerialConnexionException, FinMatchException, ScriptHookException
	{
		state.robot.tourner(-Math.PI/2);
		state.robot.avancer_dans_mur(-100); // TODO: vérifier distance
		state.robot.poserDeuxTapis();
	}

	@Override
	protected void termine(GameState<?> state) throws SerialConnexionException, FinMatchException, ScriptHookException
	{
		// on relève les tapis
		state.robot.leverDeuxTapis();
		try {
			// et on se dégage
			state.robot.avancer(300); // TODO vérifier distance
		} catch (UnableToMoveException e) {
			e.printStackTrace();
		}
	}

	@Override
	public PathfindingNodes point_sortie(int id) {
		return PathfindingNodes.SORTIE_TAPIS;
	}

	@Override
	public void setPointSortie(int id, Vec2 position)
	{
		log.debug("Nouvelle position de "+PathfindingNodes.SORTIE_TAPIS+": "+position, this);
		PathfindingNodes.SORTIE_TAPIS.setCoordonnees(position);
	}
}

package scripts;

import java.util.ArrayList;

import enums.GameElementNames;
import enums.HauteurBrasClap;
import enums.PathfindingNodes;
import enums.RobotColor;
import enums.Side;
import enums.Tribool;
import exceptions.FinMatchException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialConnexionException;
import hook.Hook;
import hook.types.HookFactory;
import strategie.GameState;
import utils.Config;
import utils.Log;

public class ScriptClap extends Script {

	public ScriptClap(HookFactory hookgenerator, Config config, Log log)
	{
		super(hookgenerator, config, log);
	}

	// Deux métaversions, contenant chacune une version.
	// id 0: droite
	// id 1: gauche
	@Override
	public ArrayList<Integer> meta_version(GameState<?> state) {
		ArrayList<Integer> out = new ArrayList<Integer>();
		// on tente même si c'est peut-être fait par l'ennemi
		if(state.gridspace.isDone(GameElementNames.CLAP_1) != Tribool.TRUE && state.gridspace.isDone(GameElementNames.CLAP_3) != Tribool.TRUE)
			out.add(0);
		if(state.gridspace.isDone(GameElementNames.CLAP_2) != Tribool.TRUE)
			out.add(1);
		return out;
	}

	@Override
	public PathfindingNodes point_entree(int id) {
		if(id == 0)
			return PathfindingNodes.CLAP_DROIT;
		else
			return PathfindingNodes.CLAP_GAUCHE;
	}

	@Override
	public void execute(int id_version, GameState<?> state)
			throws UnableToMoveException, SerialConnexionException,
			FinMatchException
	{
		ArrayList<Hook> hooks_entre_scripts = hookfactory.getHooksEntreScripts(state);
		// côté droit
		if(id_version == 0)
		{
			Side cote;
			if(color == RobotColor.GREEN)
				cote = Side.LEFT;
			else
				cote = Side.RIGHT;
			state.robot.tourner(Math.PI);
			// TODO: probablement pas possible s'il y a des plots dans ce coin
			state.robot.avancer_dans_mur(-100);
			state.robot.bougeBrasClap(cote, HauteurBrasClap.FRAPPE_CLAP);
			state.robot.clapTombe();
			state.gridspace.setDone(GameElementNames.CLAP_3, Tribool.TRUE);
			state.robot.avancer(300, hooks_entre_scripts);
			state.robot.bougeBrasClap(cote, HauteurBrasClap.TOUT_EN_HAUT);
			state.robot.avancer(300, hooks_entre_scripts);
			state.robot.bougeBrasClap(cote, HauteurBrasClap.FRAPPE_CLAP);
			state.robot.clapTombe();
			state.gridspace.setDone(GameElementNames.CLAP_1, Tribool.TRUE);
			state.robot.avancer(300, hooks_entre_scripts);
		}
		else // côté gauche
		{
			Side cote;
			if(color == RobotColor.GREEN)
				cote = Side.RIGHT;
			else
				cote = Side.LEFT;
			
			state.robot.tourner(0);
			state.robot.bougeBrasClap(cote, HauteurBrasClap.FRAPPE_CLAP);
			state.robot.clapTombe();
			state.gridspace.setDone(GameElementNames.CLAP_2, Tribool.TRUE);
			state.robot.avancer(300);
		}
	}

	@Override
	protected void termine(GameState<?> state) throws SerialConnexionException,
			FinMatchException {
		state.robot.bougeBrasClap(Side.LEFT, HauteurBrasClap.RENTRE);
		state.robot.bougeBrasClap(Side.RIGHT, HauteurBrasClap.RENTRE);
		try {
			state.robot.tourner(Math.PI/2);
			state.robot.avancer(200);		
		} catch (UnableToMoveException e) {
			e.printStackTrace();
		}
	}

}

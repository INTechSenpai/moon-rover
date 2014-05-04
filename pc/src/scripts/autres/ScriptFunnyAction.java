package scripts.autres;

import java.util.ArrayList;

import enums.Cote;
import enums.PositionRateau;
import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;
import hook.sortes.HookGenerator;
import scripts.Script;
import smartMath.Vec2;
import strategie.GameState;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;

/**
 * Script de la funny action (se met juste en position)
 * @author pf, krissprolls, rasbeguy
 *
 */

public class ScriptFunnyAction extends Script {

	private ThreadTimer threadtimer;
	
	public ScriptFunnyAction(HookGenerator hookgenerator, Read_Ini config, Log log, ThreadTimer threadtimer) {
		super(hookgenerator, config, log);
		this.threadtimer = threadtimer;
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		metaversionList.add(0);
		return metaversionList;
	}
	@Override
	public  ArrayList<Integer> version_asso(int id_meta)
	{
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		if(id_meta == 0)
			versionList.add(0);
		return versionList;
	}

	@Override
	public Vec2 point_entree(int id) {
		return new Vec2(800,1300); 
	}
	
	@Override
	public int score(int id_version, GameState<?> state) {
		// Point si ça marche
		return 6;
	}

	@Override
	public int poids(GameState<?> state) {
		return 0;
	}

	@Override
	protected void execute(int id_version, GameState<?> state)
			throws MouvementImpossibleException, SerialException {
		state.robot.tourner_sans_symetrie((float)(-1 * Math.PI/2));	// pas de symétrie
		state.robot.rateau(PositionRateau.BAS, Cote.DROIT);
		if(threadtimer.temps_restant() > 0)
			state.robot.sleep(threadtimer.temps_restant());
		state.robot.sleep(1500);
		state.robot.lancerFilet();
	}

	@Override
	protected void termine(GameState<?> state) {
		// rien à faire, la partie est finie et rien n'est dérangé.
		
	}

}

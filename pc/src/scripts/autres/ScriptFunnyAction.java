package scripts.autres;

import java.util.ArrayList;

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

        versions = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> versionList = new ArrayList<Integer>();
        versionList.add(0);
        versions.add(versionList);
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();

		// 8 secondes avant la fin du match, on propose cette possibilité
		if(threadtimer.temps_restant() < 12000)	// On fait la funny action qu'a la fin du match
			metaversionList.add(0);
		return metaversionList;
	}

	@Override
	public Vec2 point_entree(int id) {
		if(id == 0)
			return new Vec2(750,1250);
		else
			return new Vec2(-750,1250);
	}
	
	@Override
	public int score(int id_version, GameState<?> state)
	{
		return 6;
	}
	@Override
	public int poids(final GameState<?> state)
	{
		return 1;
	}


	@Override
	protected void execute(int id_version, GameState<?> state)
			throws MouvementImpossibleException, SerialException {
		// C'est le thread timer qui s'occupe de lancer le filet
	}

	@Override
	protected void termine(GameState<?> state) {
		// rien à faire, la partie est finie et rien n'est dérangé.
		
	}

	@Override
	public float probaDejaFait(int id_metaversion, GameState<?> state)
	{
		return 0;
	}
}

package scripts.autres;

import hook.sortes.HookGenerator;

import java.util.ArrayList;

import scripts.Script;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;
import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;

/**
 * Script de dépose des fresques
 * @author pf
 *
 */

public class ScriptFresque extends Script {
	

	public ScriptFresque(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
        super(hookgenerator, config, log);
        ArrayList<Integer> versionList = new ArrayList<Integer>();
        versionList.add(0);
        versionList.add(1);
        versionList.add(2);
        versions.add(versionList);
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		if(state.robot.isFresquesPosees())
			return metaversionList;
		metaversionList.add(0);
		return metaversionList;
	}

	// Points d'entrées vérifiés. Ne pas modifier sans prévenir PF (oui, Martial, c'est à toi que je parle!)
	@Override
	public Vec2 point_entree(int id) {
        return new Vec2(-200+id*200, 1400);
	}
	@Override
	public int score(int id_version, final GameState<?> state) {
		return 6;
	}

	@Override
	public int poids(final GameState<?> state)
	{
		return 1;
	}

	@Override
	protected void execute(int id_version, GameState<?> state) throws MouvementImpossibleException, SerialException
	{
	    super.execute(id_version, state);
        state.robot.tourner(Math.PI/2);
        state.robot.avancer(250);
        state.robot.tourner(-Math.PI/2);
	    state.robot.bac_bas();
	    state.robot.avancer_dans_mur(-300);
	    state.robot.deposer_fresques();
	    state.robot.avancer(300);
	    state.table.appendFresco(id_version);
	}

	@Override
	protected void termine(GameState<?> state) {
		// vide
	}
	
	public String toString()
	{
		return "ScriptFresque";
	}
	
	@Override
	public float probaDejaFait(int id_metaversion, GameState<?> state)
	{
		return state.table.getProbaFresco(id_metaversion);
	}

}

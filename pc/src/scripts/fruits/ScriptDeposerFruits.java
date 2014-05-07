package scripts.fruits;

import java.util.ArrayList;

import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;
import hook.sortes.HookGenerator;
import scripts.Script;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;

/**
 * Script de dépose de fruits
 * @author pf
 * @author raspbeguy
 * @author krissprolls 
 *
 */

public class ScriptDeposerFruits extends Script {

	public ScriptDeposerFruits(HookGenerator hookgenerator, Read_Ini config, Log log) {
		super(hookgenerator, config, log);
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		if(state.robot.get_nombre_fruits_bac() > 0)
			metaversionList.add(0);
		return metaversionList;
	}
	@Override
	public  ArrayList<Integer> version_asso(int id_meta)
	{
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		if(id_meta == 0)
		{
			versionList.add(0);
			versionList.add(1);
		}
		return versionList;
	}

	@Override
	public Vec2 point_entree(int id) {
		return new Vec2(-600-300*id, 1300);
	}
	@Override
	public int score(int id_version, GameState<?> state)
	{
		return state.robot.get_nombre_fruits_bac();
	}

	@Override
	public int poids(GameState<?> state)
	{
		// Ne pas aller déposer de fruits s'il n'y en a pas a déposer
		if(state.robot.get_nombre_fruits_bac() == 0) 
			return 0;
		else
			return 2;	// les arbres rapportent un max
	}
	@Override
	protected void execute(int id_version, GameState<?> state)
			throws MouvementImpossibleException, SerialException {
	    state.robot.tourner((float)-Math.PI/2);
	    state.robot.avancer(-160);
	    state.robot.bac_haut();	// histoire d'être sûr qu'il y arrive bien
	    state.robot.bac_haut();
	    state.robot.bac_haut();
	    state.robot.sleep(500);
	    state.robot.avancer(160);
	    state.robot.bac_bas();
	}
	@Override
	protected void termine(GameState<?> state) {
		try {
		    state.robot.bac_bas();
		} catch (SerialException e) {
			e.printStackTrace();
		}
	}

	public String toString()
	{
		return "ScriptDeposerFruits";
	}

}

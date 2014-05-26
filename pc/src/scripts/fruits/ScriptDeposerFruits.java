package scripts.fruits;

import java.util.ArrayList;

import enums.Cote;
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
        ArrayList<Integer> versionList = new ArrayList<Integer>();
        versionList.add(0);
        versionList.add(1);
        versions.add(versionList);
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
	public Vec2 point_entree(int id) {
		return new Vec2(-600-300*id, 1400);
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
	    state.robot.tourner(-1.5707);
	    state.robot.avancer_dans_mur(-300);
	    state.robot.bac_haut();	// histoire d'être sûr qu'il y arrive bien
	    state.robot.bac_haut();
	    state.robot.bac_haut();
	    state.robot.sleep(2000);
	    state.robot.avancer(200);
	    state.robot.bac_bas();
	}
	@Override
	protected void termine(GameState<?> state) {
		try {
		    state.robot.bac_bas();
		    state.robot.lever_pince(Cote.GAUCHE);
			state.robot.lever_pince(Cote.DROIT);
		} catch (SerialException e) {
			e.printStackTrace();
		}
	}

	public String toString()
	{
		return "ScriptDeposerFruits";
	}
	
	@Override
	public float probaDejaFait(int id_metaversion, GameState<?> state)
	{
		return 0.5f;	// non surveillé
	}

}

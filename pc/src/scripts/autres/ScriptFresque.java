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
	@Override
	public  ArrayList<Integer> version_asso(int id_meta)
	{
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		if(id_meta == 0)
		{
			versionList.add(0);
			versionList.add(1);
			versionList.add(2);
		}
		return versionList;
	}

	@Override
	public Vec2 point_entree(int id) {
	    if(id == 1)
	        return new Vec2(0, 1700);
	    else
            return new Vec2(-50+id*50, 1600);
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
	    state.robot.tourner((float)-Math.PI/2);
	    state.robot.bac_bas();
	    state.robot.avancer_dans_mur(-250);
	    state.robot.deposer_fresques();
	    state.robot.avancer(450);
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

}

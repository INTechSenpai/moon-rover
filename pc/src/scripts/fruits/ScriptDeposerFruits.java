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
 * @author marsu
 * @author krissprolls 
 *
 */

public class ScriptDeposerFruits extends Script
{

	public ScriptDeposerFruits(HookGenerator hookgenerator, Read_Ini config, Log log) {
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
		if(state.robot.get_nombre_fruits_bac() > 0)
			metaversionList.add(0);
		return metaversionList;
	}

	@Override
	public Vec2 point_entree(int id) {
		if(id == 0)
			return new Vec2(-600,1400);
		else if(id == 1)
			return new Vec2(-900,1400);
		else if (id == 2)
			return new Vec2(-200,1400);
		else //if (id == 2)
			return new Vec2(-1350,1400);
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
	protected void execute(int id_version, GameState<?> state) throws MouvementImpossibleException, SerialException
	{
        super.execute(id_version, state);
        if(id_version == 2) // si on dépose les fruits par le coté central, on met  aussi les fresques !
        {
        	//dépose les fresques
            state.robot.tourner(Math.PI/2);
            state.robot.avancer(250);
            state.robot.tourner(-Math.PI/2);
    	    state.robot.bac_bas();
    	    state.robot.avancer_dans_mur(-300);
    	    state.robot.deposer_fresques();
    	    state.robot.avancer(150);
    	    state.table.appendFresco(id_version);
    	    

    	    // dépose les fruits
		    state.robot.tourner(0);
		    state.robot.bac_haut();	// histoire d'être sûr qu'il y arrive bien
		    state.robot.bac_haut();
		    state.robot.bac_haut();
		    state.robot.avancer_dans_mur(-300);
		    state.robot.sleep(2000);
		    state.robot.avancer(50);
		    state.robot.bac_bas();
		    
		    // retourne a portée des autoroutes
            state.robot.tourner(-Math.PI/2);
            state.robot.avancer(250);
        	
        }
        else if(id_version == 3) // dépose des fruits par la grotte ennemie
        {

        	//avance dans la grotte
            state.robot.tourner(Math.PI/2);
            state.robot.avancer(250);
    	    

    	    // dépose les fruits
		    state.robot.tourner(3.141);
		    state.robot.bac_haut();	// histoire d'être sûr qu'il y arrive bien
		    state.robot.bac_haut();
		    state.robot.bac_haut();
		    state.robot.avancer_dans_mur(-300);
		    state.robot.sleep(2000);
		    state.robot.avancer(50);
		    state.robot.bac_bas();
		    
		    // retourne a portée des autoroutes
            state.robot.tourner(-Math.PI/2);
            state.robot.avancer(250);
        	
        }
        else
        {
		    state.robot.tourner(-1.5707f);
		    state.robot.avancer_dans_mur(-300);
		    state.robot.bac_haut();	// histoire d'être sûr qu'il y arrive bien
		    state.robot.bac_haut();
		    state.robot.bac_haut();
		    state.robot.sleep(2000);
		    state.robot.avancer(200);
		    state.robot.bac_bas();
        }
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

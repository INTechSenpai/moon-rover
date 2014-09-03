package scripts.feux;

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
 * Script de récupération des feux debouts
 * Pour ceux qui préfèrent séparer les feux dans les torches 
 * NON UTILISÉ (FAIT PAR HOOK)
 * @author  krissprolls
 *
 */
public class ScriptFeuDebout extends Script{
	
	
	public ScriptFeuDebout(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
		super(hookgenerator, config, log);
        ArrayList<Integer> versionList = new ArrayList<Integer>();
        versionList.add(0);
        versions.add(versionList);
        versionList.clear();
        versionList.add(1);
        versions.add(versionList);
        versionList.clear();
        versionList.add(2);
        versions.add(versionList);
        versionList.clear();
        versionList.add(3);
        versions.add(versionList);
        versionList.clear();
        versionList.add(4);
        versions.add(versionList);
        versionList.clear();
        versionList.add(5);
        versions.add(versionList);
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		/*
		 * arrayFire[0] = new Fire(new Vec2(1100,900), 1, 0, Colour.RED);	// ok
		arrayFire[1] = new Fire(new Vec2(600,1400), 2, 0, Colour.YELLOW); // OK
		arrayFire[2] = new Fire(new Vec2(600,400), 6, 0, Colour.RED); // ok
		arrayFire[3] = new Fire(new Vec2(-600,1400), 9, 0, Colour.YELLOW); //ok
		arrayFire[4] = new Fire(new Vec2(-600,400), 13, 0, Colour.RED); // ok
		arrayFire[5] = new Fire(new Vec2(-1100,900), 14, 0, Colour.YELLOW); // ok

		 */
		if(!(state.robot.isTient_feu(Cote.DROIT)||state.robot.isTient_feu(Cote.GAUCHE)))
		{
			if(state.table.isTakenFire(5))
				metaversionList.add(0);
			if(state.table.isTakenFire(0))
				metaversionList.add(1);
			if(state.table.isTakenFire(4))
				metaversionList.add(2);
			if(state.table.isTakenFire(1))
				metaversionList.add(3);
			if(state.table.isTakenFire(3))
				metaversionList.add(4);
			if(state.table.isTakenFire(2))
				metaversionList.add(5);
		}
		return metaversionList;
	}

	public ArrayList<Integer> version(GameState<?> state) {
		// TODO
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		//Les feux verticaux
		//Ajouter une condition pour chaque feu pour savoir s'il est toujours là ?
		if(!(state.robot.isTient_feu(Cote.DROIT)||state.robot.isTient_feu(Cote.GAUCHE)))
		{
			versionList.add(0);
			versionList.add(1);
			versionList.add(2);
			versionList.add(3);
			versionList.add(4);
			versionList.add(5);
		}
		return versionList;
	}
	
	public Vec2 point_entree(int id) {
		//Les coordonnées ont été prises à partir du réglement
		
		if(id ==0)
			return new Vec2(-1100, 700);
		else if(id ==1)
			return new Vec2(1100,700);
		else if(id ==2)
			return new Vec2(-400, 400);
		else if(id ==3)
			return new Vec2(400,400);
		else if(id ==4)
			return new Vec2(-400, 1400);
		else if(id ==5)
			return new Vec2(400, 1400);
		else
			return null;		
	}

	@Override
	public int score(int id_version, GameState<?> state) {
		return 0;
	}


	public int poids(GameState<?> state) 
	{
		return 1;
	}


	protected void execute(int id_version, GameState<?> state)
			throws MouvementImpossibleException {
		if(id_version == 0)
			//Vec2(-1100, 900);
		{
		    state.robot.tourner((float)Math.PI/2);
		}	
		else if(id_version == 1)
			// Vec2(1100,900);
		{
		    state.robot.tourner((float)Math.PI/2);
		}	
		else if(id_version == 2)
			// Vec2(-600, 400);
		{
		    state.robot.tourner((float)Math.PI);
		}	
		else if(id_version == 3)
			// Vec2(600,400);
		{
		    state.robot.tourner((float)Math.PI);
		}	
		else if(id_version == 4)
			// Vec2(-600, 1400);
		{
		    state.robot.tourner((float)Math.PI);
		}	
		else if(id_version == 5)
			// Vec2(600, 1400);
		{
		    state.robot.tourner((float)0);			
		}
		
		
		if(!state.robot.isTient_feu(Cote.GAUCHE))
			try {
//				state.robot.takefire(Cote.GAUCHE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else if(state.robot.isFeu_tenu_rouge(Cote.DROIT))
			try {
//				state.robot.takefire(Cote.DROIT);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//state.table.pickFire(id_version);
		/*
		if(!state.robot.isTient_feu(Cote.GAUCHE))
		{
			//Pour les feux à pousser
				
				try {
				    state.robot.milieu_pince(Cote.GAUCHE);
				    state.robot.avancer(10);
				    state.robot.avancer(-10);
				    state.robot.baisser_pince(Cote.GAUCHE);
				    state.robot.ouvrir_pince(Cote.GAUCHE);
				    state.robot.avancer(10);
				    state.robot.fermer_pince(Cote.GAUCHE);
				    state.robot.avancer(-10);
				} catch (SerialException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			
			
			
			}
		
		else if(state.robot.isFeu_tenu_rouge(Cote.DROIT))
		{
			
		    // TODO: cette condition est impossible à réaliser. Clément le sait-il?
			
				try {
				
				    state.robot.milieu_pince(Cote.DROIT);
				    state.robot.avancer(10);
				    state.robot.avancer(-10);
				    state.robot.baisser_pince(Cote.DROIT);
				    state.robot.ouvrir_pince(Cote.DROIT);
				    state.robot.avancer(10);
				    state.robot.fermer_pince(Cote.DROIT);
				    state.robot.avancer(-10);
				} catch (SerialException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			
		}
		*/
	}

	protected void termine(GameState<?> state) {
		try
		{
		    state.robot.lever_pince(Cote.DROIT);
		    state.robot.fermer_pince(Cote.DROIT);
		    state.robot.lever_pince(Cote.GAUCHE);
		    state.robot.fermer_pince(Cote.GAUCHE);
		}
		catch(SerialException e) {
			e.printStackTrace();
		}
	}

	public float proba_reussite()
	{
		// TODO
		return 1;
	}

	public String toString()
	{
		return "ScriptFeuDebout";
	}
	
	public void maj_config()
	{
	}

	
	@Override
	public float probaDejaFait(int id_metaversion, GameState<?> state)
	{
		return 0.5f;
	}
}

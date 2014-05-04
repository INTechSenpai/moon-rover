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
 * Script de récupération de feux sur les torches mobiles et les feux debout
 * @author pf
 * @author krissprolls
 *
 */
public class ScriptTorche extends Script {

	public ScriptTorche(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
		super(hookgenerator, config, log);
	}

	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		if(!(state.robot.isTient_feu(Cote.DROIT) && state.robot.isTient_feu(Cote.GAUCHE)))
		{
			if ((state.table.codeTorches() == 3 || state.table.codeTorches() == 2)  && !state.table.isTorchTaken(0))
			{
				metaversionList.add(0);	
			}
			if((state.table.codeTorches() == 3 || state.table.codeTorches() == 1) && !state.table.isTorchTaken(1))
			{
				metaversionList.add(1);
			}
		}
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
		if(id_meta == 1)
		{
			versionList.add(3);
			versionList.add(4);
			versionList.add(5);
		}
		return versionList;
	}
	
	@Override
	public Vec2 point_entree(int id) {
		//Les coordonnées ont été prises à partir du réglement
		if(id ==0)
			return new Vec2(-600,750);
		else if(id ==1)
			return new Vec2(600,750);
		else if(id ==2)
			//X = -600+150*cos(-pi/6)
			//Y = 900+150*sin(-pi/6)
			return new Vec2(-470,825);
		else if(id ==3)
			//X = 600+150*cos(-pi/6)
			//Y = 900+150*sin(-pi/6)
			return new Vec2(730,825);
		else if(id ==4)
			//X = -600+150*cos(7*pi/6)
			//Y = 900+150*sin(7*pi/6)
			return new Vec2(-730,825);
		else if(id ==5)
			//X = 600+150*cos(7*pi/6)
			//Y = 900+150*sin(7*pi/6)
			return new Vec2(470,825);
		else
			return null;		
	}
	@Override
	public int score(int id_version, GameState<?> state) {
		return 0;
	}

	@Override
	public int poids(GameState<?> state) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void execute(int id_version, GameState<?> state)
			throws MouvementImpossibleException, SerialException {
		if(id_version ==0)
			//Vec2(-600,900)
		    state.robot.tourner(0);

		else if(id_version ==1)
			//Vec2(600,900);
		    state.robot.tourner(0);

		else if(id_version ==2)
			//Vec2(-600,900)
		    state.robot.tourner((float)-Math.PI/6);

		else if(id_version ==3)
			//Vec2(600,900)
		    state.robot.tourner((float)-Math.PI/6);

		else if(id_version ==4)
			//Vec2(-600,900)
		    state.robot.tourner((float)(7*Math.PI/6));

		else if(id_version ==5)
			//Vec2(600,900)
		    state.robot.tourner((float)(7*Math.PI/6));
		state.robot.set_vitesse_translation("torche");
		/*
		
		robot.prendre_torche(Cote.GAUCHE);
		robot.set_vitesse_translation("torche");
		robot.avancer(100);
		robot.sleep(500);
		robot.fermer_pince(Cote.GAUCHE);
		robot.sleep(200);
		robot.lever_pince(Cote.GAUCHE);
		robot.sleep(300);
		robot.ouvrir_pince(Cote.GAUCHE);
		robot.sleep(50);
		robot.fermer_pince(Cote.GAUCHE);
		*/
		/*
		 * robot.tourner((float)Math.PI);
			robot.prendre_torche(Cote.DROIT);
			robot.set_vitesse_translation("torche");
			robot.avancer(100);
			robot.sleep(500);
			robot.fermer_pince(Cote.DROIT);
			robot.sleep(200);
			robot.lever_pince(Cote.DROIT);
			robot.sleep(300);
			robot.ouvrir_pince(Cote.DROIT);
			robot.sleep(50);
			robot.fermer_pince(Cote.DROIT);
		 */
		if(!state.robot.isTient_feu(Cote.GAUCHE))
		{
			//Pour les feux à ramasser dans les torches
			try {
				state.robot.prendre_torche(Cote.GAUCHE);
				state.table.pickTorch(1);
				/*
			    state.robot.ouvrir_pince(Cote.GAUCHE);
			    state.robot.milieu_pince(Cote.GAUCHE);
			    state.robot.fermer_pince(Cote.GAUCHE);
			    state.robot.lever_pince(Cote.GAUCHE);
			    */
				
                // On retire la torche des obstacles.
                state.table.torche_disparue(Cote.GAUCHE);
			    
			} catch (SerialException e) {
				e.printStackTrace();
			}
		}
		else if(!state.robot.isTient_feu(Cote.DROIT))
		{		
			//Pour les feux à ramasser dans les torches
			try {
				/*
			    state.robot.ouvrir_pince(Cote.DROIT);
			    state.robot.milieu_pince(Cote.DROIT);
			    state.robot.fermer_pince(Cote.DROIT);
			    state.robot.lever_pince(Cote.DROIT);
			    */
				state.robot.prendre_torche(Cote.DROIT);
				state.table.pickTorch(0);
				// On retire la torche des obstacles.
				state.table.torche_disparue(Cote.DROIT);
			    // TODO mettre à jour robot, mais pas en utilisant torche disparue
			} catch (SerialException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
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

	public String toString()
	{
		return "ScriptTorche";
	}
	
	public void maj_config()
	{
	}
}


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
 * @author krissprolls
 *
 */


public class ScriptFeuBord extends Script {

	public ScriptFeuBord(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
		super(hookgenerator, config, log);
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		// TODO
		if(!(state.robot.isTient_feu(Cote.DROIT)||state.robot.isTient_feu(Cote.GAUCHE)))
		{
			if (state.table.isTakenFixedFire(0))
				metaversionList.add(0);
			if (state.table.isTakenFixedFire(3))
				metaversionList.add(1);
			if (state.table.isTakenFixedFire(2))
				metaversionList.add(2);
			if (state.table.isTakenFixedFire(1))
				metaversionList.add(3);
		}
		return metaversionList;
	}
	@Override
	public  ArrayList<Integer> version_asso(int id_meta)
	{
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		versionList.add(id_meta);
		return versionList;
	}
	
	@Override
	public Vec2 point_entree(int id) {
		//Les coordonnées ont été prises à partir du réglement
		if(id ==0)
			return new Vec2(-900,1100);
		else if(id ==1)
			return new Vec2(900, 1100);		
		else if(id ==2)
			return new Vec2(-300, 500);
		else if(id ==3)
			return new Vec2(100, 500);
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
			// Vec2(-1500,1200);
		    state.robot.tourner((float)Math.PI);

		else if(id_version ==1)
			// Vec2(1500, 1200);
		    state.robot.tourner(0);

		else if(id_version ==2||id_version ==3)
			// Vec2(-200, 0);
			// Vec2(200, 0);
		    state.robot.tourner((float)(-Math.PI/2));

		Cote cote = Cote.GAUCHE;
		if(!state.robot.isTient_feu(Cote.GAUCHE))
		{
			cote = Cote.GAUCHE;
		}
		else if(state.robot.isTient_feu(Cote.DROIT))
		{
			int decalage = 150; //sert à passer d'une pince à l'autre
			if(id_version ==0)
			{
				// Vec2(-1500,1200);
			    state.robot.tourner((float)Math.PI/2);
				state.robot.avancer(decalage);
				state.robot.tourner(-(float)Math.PI);
			}
			else if(id_version ==1)
			{
				// Vec2(1500, 1200);
			    state.robot.tourner((float)Math.PI/2);
				state.robot.avancer(decalage);
				state.robot.tourner(0);
			}
			else if(id_version ==2||id_version ==3)
			{
				state.robot.tourner(0);
				state.robot.avancer(decalage);
				 state.robot.tourner(-(float)Math.PI/2);
			}
			cote = Cote.DROIT;
			/*
			 * Ici il faudra se redéplacer pour compenser le décalage
			 * 
			 */
		}
		//Pour les feux à tirer
		try 
		{
			state.robot.avancer(330);
			state.robot.ouvrir_pince(cote);
			state.robot.sleep(1000);
			state.robot.milieu_pince(cote);
			state.robot.sleep(1000);			
			state.robot.fermer_pince(cote);
			state.robot.sleep(1000);
			state.robot.avancer(-200);
			state.robot.ouvrir_pince(cote);
			state.robot.sleep(1000);
			state.robot.baisser_pince(cote);
			state.robot.avancer(130);
			state.robot.fermer_pince(cote);
			state.robot.sleep(1000);
			state.robot.lever_pince(cote);
		    state.robot.setTient_feu(cote);
		}
		catch (SerialException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Et oui, il faut parler à la stratégie !!! GNNNNN
		state.table.pickFixedFire(id_version);
	}

	@Override
	protected void termine(GameState<?> state) 
	{
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
		return "ScriptFeuBord";
	}
	
	public void maj_config()
	{
	}
}

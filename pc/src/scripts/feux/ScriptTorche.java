package scripts.feux;

import java.util.ArrayList;

import enums.Colour;
import enums.Cote;
import enums.Vitesse;
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

	@SuppressWarnings("unchecked")
    public ScriptTorche(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
		super(hookgenerator, config, log);
        ArrayList<Integer> versionList = new ArrayList<Integer>();
        versionList.add(1);
        versionList.add(5);
        versions.add((ArrayList<Integer>) versionList.clone());
        versionList.clear();
        versionList.add(0);
        versionList.add(4);
        versions.add((ArrayList<Integer>) versionList.clone());
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
	public Vec2 point_entree(int id) {
		//Les coordonnées ont été prises à partir du réglement
		if(id ==0)
			return new Vec2(-600,500);
		else if(id ==1)
			return new Vec2(600,500);
		
		else if(id ==2)
			//X = -600+400*cos(-pi/6)
			//Y = 900+400*sin(-pi/6)
			return new Vec2(-946,1100);
		/*
		else if(id ==3)
			//X = 600-400*cos(-pi/6)
			//Y = 900-400*sin(-pi/6)
			return new Vec2(253,1100);
			
		else if(id ==4)
			//X = -600-400*cos(7*pi/6)
			//Y = 900+400*sin(7*pi/6)
			return new Vec2(-253,1100);
		*/
		else if(id ==5)
			//X = 600-400*cos(7*pi/6)
			//Y = 900+400*sin(7*pi/6)
			return new Vec2(946,1100);
		else
			return null;		
	}
	@Override
	public int score(int id_version, GameState<?> state) {
		return 0;
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
		float angle  = 0;
		int decalage = -130;
		int avancement = 120;
		if(id_version ==0)
			//Vec2(-600,900)
			angle = 1.5707f;
			//(float)Math.PI/2;
		else if(id_version ==1)
			//Vec2(600,900);
			angle = 1.5707f;
			//(float)Math.PI/2;
		
		else if(id_version ==2)
			//X = -600+600*cos(-pi/6)
			//Y = 900+600*sin(-pi/6)
			//Vec2(-80,600);
			angle = .5235f;
			//(float)-Math.PI/6;
		/*
		else if(id_version ==3)
			//X = 600+400*cos(-pi/6)
			//Y = 900+400*sin(-pi/6)
			//Vec2(946,700);
		    angle = (float)-Math.PI/6;
	
		else if(id_version ==4)
			//X = -600+400*cos(7*pi/6)
			//Y = 900+400*sin(7*pi/6)
			//Vec2(-946,700);
		    angle = (float)(7*Math.PI/6);
		*/
		else if(id_version ==5)
			//X = 600+600*cos(7*pi/6)
			//Y = 900+600*sin(7*pi/6)
			//Vec2(80,600);
		    angle = 3.6651f;
		    //(float)(7*Math.PI/6);
		
		Cote cote = Cote.GAUCHE;
		
		if(!state.robot.isTient_feu(Cote.GAUCHE))
		{
			cote = Cote.GAUCHE;
			decalage = -130;
			avancement = 130;
		}
		else if(!state.robot.isTient_feu(Cote.DROIT))
		{	
			cote = Cote.DROIT;
			decalage = 110;
			avancement = 130;
		}
		state.robot.set_vitesse(Vitesse.PRISE_FEU);
		try {
			state.robot.tourner(angle+1.5707f);
			state.robot.avancer(decalage);
			state.robot.tourner(angle);
			state.robot.avancer(avancement);
			state.robot.prendre_torche(cote);
			state.robot.sleep(500);
			state.robot.fermer_pince(cote);
			state.robot.sleep(500);
			state.robot.avancer(-200);
		} catch (SerialException e) {
			e.printStackTrace();
		}
			//Pour les feux à ramasser dans les torches
				/*
			    state.robot.ouvrir_pince(Cote.GAUCHE);
			    state.robot.milieu_pince(Cote.GAUCHE);
			    state.robot.fermer_pince(Cote.GAUCHE);
			    state.robot.lever_pince(Cote.GAUCHE);
			    */
		// On retire la torche des obstacles.
		if(id_version ==0)
			//Vec2(-600,900)
		{
            state.table.torche_disparue(Cote.GAUCHE);
            state.robot.setTient_feu(cote);
			state.robot.setFeu_tenu_rouge(cote, Colour.RED);
		}
		else if(id_version ==1)
			//Vec2(600,900);
		{
			state.table.torche_disparue(Cote.DROIT);
			state.robot.setTient_feu(cote);
			state.robot.setFeu_tenu_rouge(cote, Colour.YELLOW);
		}
		else if(id_version ==2)
			//X = -600+600*cos(-pi/6)
			//Y = 900+600*sin(-pi/6)
			//Vec2(-80,600);
		{
            state.table.torche_disparue(Cote.GAUCHE);
            state.robot.setTient_feu(cote);
			state.robot.setFeu_tenu_rouge(cote, Colour.RED);
		}
		/*
		else if(id_version ==3)
			//X = 600+400*cos(-pi/6)
			//Y = 900+400*sin(-pi/6)
			//Vec2(946,700);
			state.table.torche_disparue(Cote.DROIT);
		
		else if(id_version ==4)
			//X = -600+400*cos(7*pi/6)
			//Y = 900+400*sin(7*pi/6)
			//Vec2(-946,700);
            state.table.torche_disparue(Cote.GAUCHE);
		*/
		else if(id_version ==5)
			//X = 600+600*cos(7*pi/6)
			//Y = 900+600*sin(7*pi/6)
			//Vec2(80,600);
		{
			state.table.torche_disparue(Cote.DROIT);
			state.robot.setTient_feu(cote);
			state.robot.setFeu_tenu_rouge(cote, Colour.YELLOW);
		}
		
		
		// TEMPORAIRE :  pour debug stratégie
		/*
	    state.robot.ouvrir_pince(Cote.GAUCHE);
	    state.robot.milieu_pince(Cote.GAUCHE);
	    state.robot.fermer_pince(Cote.GAUCHE);
	    state.robot.lever_pince(Cote.GAUCHE);
	    */
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
	
	@Override
	public float probaDejaFait(int id_metaversion, GameState<?> state)
	{
		return 0.5f;	// non surveilllé
	}
}

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
 * @author krissprolls
 *
 */


public class ScriptFeuBord extends Script {

	@SuppressWarnings("unchecked")
    public ScriptFeuBord(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
		super(hookgenerator, config, log);
		ArrayList<Integer> versionList = new ArrayList<Integer>();
        versionList.add(0);
        versions.add((ArrayList<Integer>) versionList.clone());
        versionList.clear();
        versionList.add(1);
        versions.add((ArrayList<Integer>) versionList.clone());
        versionList.clear();
        versionList.add(2);
        versions.add((ArrayList<Integer>) versionList.clone());
        versionList.clear();
        versionList.add(3);
        versions.add((ArrayList<Integer>) versionList.clone());
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		
		if (!state.table.isTakenFixedFire(0)&&!(state.robot.isTient_feu(Cote.DROIT)))
			metaversionList.add(1);
		if (!state.table.isTakenFixedFire(3)&&!(state.robot.isTient_feu(Cote.GAUCHE)))
			metaversionList.add(0);
		if (!state.table.isTakenFixedFire(2)&&!(state.robot.isTient_feu(Cote.GAUCHE)))
			metaversionList.add(2);
		if (!state.table.isTakenFixedFire(1)&&state.robot.isTient_feu(Cote.DROIT))
			metaversionList.add(3);
		
		return metaversionList;
	}
	@Override
	public Vec2 point_entree(int id) {
		//Les coordonnées ont été prises à partir du réglement
		if(id ==0)
			return new Vec2(-1100,1470);
		else if(id ==1)
			return new Vec2(1100, 1470);		
		else if(id ==2)
			return new Vec2(-250, 500);
		else if(id ==3)
			return new Vec2(250, 500);
		else
			return null;		
	}
	@Override
	public int score(int id_version, GameState<?> state)
	{
		if(id_version ==0)
			return 0;
		else if(id_version ==1)
			return 1;		
		else if(id_version ==2)
			return 0;
		else if(id_version ==3)
			return 1; 
		
		return 0;
	}

	public int poids(GameState<?> state) 
	{
		return 1;
	}


	@Override
	protected void execute(int id_version, GameState<?> state)
			throws MouvementImpossibleException, SerialException {
        super.execute(id_version, state);
		Cote cote = Cote.GAUCHE;
		int decalage = 180;
		float angle = 0f;
		int avancement = 200;
		if(id_version ==0)
			{
			// Vec2(-1500,1200);
			angle = 0;	    
			cote = Cote.GAUCHE;
			}
		else if(id_version ==1)
		{
			// Vec2(1500, 1200);
		    //state.robot.tourner(0);
			angle = 0;
			cote = Cote.DROIT;
		}
		else if(id_version ==2)
		{
			// Vec2(-200, 0);
			
		    //state.robot.tourner((float)(-Math.PI/2));
		    angle = -1.5707f;
			cote = Cote.GAUCHE;
		}
		else if(id_version ==3)
		{
			// Vec2(200, 0);
			//state.robot.tourner((float)(-Math.PI/2));
			angle = 1.5707f;
			cote = Cote.DROIT;
		}		
		
		
		try{
			if(cote == Cote.GAUCHE)
			{
				decalage = 150;
			}
			else if(cote == Cote.DROIT)
			{
				decalage = -150;
			}
			//POUR GÉRER LA couleur 
			 if(couleur.contains("rouge") && cote == Cote.DROIT)
			{
				cote = Cote.GAUCHE;
			}
			else if(couleur.contains("rouge") && cote == Cote.GAUCHE)
			{
				cote = Cote.DROIT;
			}
			
			state.robot.tourner(angle);
			state.robot.avancer(avancement);
			state.robot.sleep(200);
			state.robot.milieu_pince(cote);
			state.robot.sleep(200);
			state.robot.renverserFeu(cote);
			state.robot.sleep(500);
			state.robot.fermer_pince(cote);
			state.robot.lever_pince(cote);
			state.robot.avancer(-avancement+50);
			state.robot.tourner(angle+1.5707f);
			state.robot.avancer(decalage);
			state.robot.tourner(angle);
			//state.robot.takefire(cote, cote); // TODO
			
			// ces 2 versions
			if(id_version == 0 || id_version == 2)
			{
				
			
				//Ca remplace le takefire
				int signe = 1;
				if(cote == Cote.GAUCHE)
					signe = -1;
				state.robot.stopper();
				state.robot.avancer(-150);
				//state.robot.tourner_relatif(-signe*0.4f);
		        state.robot.ouvrir_bas_pince(cote);
				state.robot.tourner_relatif(signe*0.2f);
				state.robot.sleep(600);
				state.robot.avancer(120);
				state.robot.set_vitesse(Vitesse.PRISE_FEU);
				state.robot.fermer_pince(cote);
				state.robot.sleep(500);
				state.robot.avancer(-120);
				state.robot.sleep(500);
				state.robot.lever_pince(cote);
				state.robot.sleep(500);
				
				// On signale à la table qu'on a prit un feu. A priori, c'est le plus proche de cette position.
			    
		        
			}
		        
			state.robot.sleep(500);
			state.robot.avancer(-avancement-50);
		}catch (SerialException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//il faut parler à la stratégie
		if(id_version ==0)
		{
			state.table.pickFixedFire(3);
			state.robot.setFeu_tenu_rouge(cote, Colour.RED);
		}
		else if(id_version ==1)
		{
			//On laisse juste tomber le feu
			//state.table.pickFixedFire(0);
			//state.robot.setFeu_tenu_rouge(cote, Colour.YELLOW);
		}
		else if(id_version ==2)
		{
			state.table.pickFixedFire(2);
			state.robot.setFeu_tenu_rouge(cote, Colour.YELLOW);
		}
		else
		{
			//On laisse juste tomber le feu
			//state.table.pickFixedFire(1);
			//state.robot.setFeu_tenu_rouge(cote, Colour.RED);
		}
		state.robot.setTient_feu(cote);
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
	
	@Override
	public float probaDejaFait(int id_metaversion, GameState<?> state)
	{
		return state.table.getProbaFixedFire(id_metaversion);
	}
}


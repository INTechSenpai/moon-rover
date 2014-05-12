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
 * Script de dépose de feu
 * @author pf
 * @author krissprolls
 *
 */
public class ScriptDeposerFeu extends Script {

	public ScriptDeposerFeu(HookGenerator hookgenerator, Read_Ini config, Log log)
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
        versionList.add(3);
        versionList.add(4);
        versions.add(versionList);
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		if(state.robot.isTient_feu(Cote.DROIT) || state.robot.isTient_feu(Cote.GAUCHE))
		{
			metaversionList.add(0);
			metaversionList.add(1);
			metaversionList.add(2);
		}
		return metaversionList;
	}

	@Override
	public Vec2 point_entree(int id) {
		if(id == 0)
			return new Vec2(-800,466);			

		else if(id == 1)
			return new Vec2(800,466);

		else if(id == 2)
			return new Vec2(0, 1400);

		else if(id == 3)
			return new Vec2(-782, 528);

		else if(id == 4)
			return new Vec2(782, 528);

		return null;
	}	
	@Override
	public int score(int id_version, GameState<?> state) 
	{
		if(state.robot.isTient_feu(Cote.DROIT) && state.robot.isTient_feu(Cote.GAUCHE))
			return 4;
		else if(state.robot.isTient_feu(Cote.DROIT) || state.robot.isTient_feu(Cote.GAUCHE))
			return 2;
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
		float angle;
		//Suivant là où on va poser, on doit se positionner différemment
		
		if (id_version == 0)
			angle = 3.729f;		//(float)(Math.PI+Math.atan(2/3)));
		else if(id_version == 1)
			angle = -0.588f;	//(float)(-Math.atan(2/3)));
		else if(id_version == 2)
			angle = -1.57f;		//(float)(-Math.PI/2));
		else if(id_version == 3)
			angle = 0.588f;		//(float)(Math.atan(2/3)));
		else
			angle = 2.553f;		//(float)(Math.PI-Math.atan(2/3)));

		// TODO : si on est équipe rouge il faut inverser les if 
		
		if(state.robot.isTient_feu(Cote.GAUCHE))
		{
			state.robot.tourner(angle-0.523f);
			if(state.robot.isFeu_tenu_rouge(Cote.GAUCHE) ^ couleur == "rouge")
			    state.robot.poserFeuEnRetournant(Cote.GAUCHE);
			else
			    state.robot.poserFeuBonCote(Cote.GAUCHE);			
		}
		else // il faut tourner dans tout les cas, même si la version n'est pas cohérente
		{
			state.robot.tourner(angle+0.523f);
			if(state.robot.isFeu_tenu_rouge(Cote.DROIT) ^ couleur == "rouge")
			    state.robot.poserFeuEnRetournant(Cote.DROIT);
			else
			    state.robot.poserFeuBonCote(Cote.DROIT);
		}		
		state.robot.avancer(-50);
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
		return "ScriptDeposerFeu";
	}

}

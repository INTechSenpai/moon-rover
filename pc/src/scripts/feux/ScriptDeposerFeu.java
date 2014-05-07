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
	public  ArrayList<Integer> version_asso(int id_meta)
	{
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		if(id_meta == 0)
			versionList.add(0);
		else if(id_meta == 1)
			versionList.add(1);
		else if(id_meta == 2)
		{
			versionList.add(2);
			versionList.add(3);
			versionList.add(4);
		}
		return versionList;
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
		//Suivant là où on va poser, on doit se positionner différemment
		if (id_version == 0)
		    state.robot.tourner((float)(Math.PI+Math.atan(2/3)));
		else if(id_version == 1)
		    state.robot.tourner((float)(-Math.atan(2/3)));
		else if(id_version == 2)
		    state.robot.tourner((float)(-Math.PI/2));
		else if(id_version == 3)
		    state.robot.tourner((float)(Math.atan(2/3)));
		else															
		    state.robot.tourner((float)(Math.PI-Math.atan(2/3)));

		// TODO : si on est équipe rouge il faut inverser les if 
		
		if(state.robot.isTient_feu(Cote.GAUCHE))
		{
			if(state.robot.isFeu_tenu_rouge(Cote.GAUCHE) ^ couleur == "rouge")
			    state.robot.poserFeuEnRetournant(Cote.GAUCHE);
			else
			    state.robot.poserFeuBonCote(Cote.GAUCHE);			
		}
		else // il faut tourner dans tout les cas, même si la version n'est pas cohérente
		{
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

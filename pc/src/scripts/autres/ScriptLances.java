package scripts.autres;

import hook.sortes.HookGenerator;

import java.util.ArrayList;

import scripts.Script;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;
import exceptions.deplacements.MouvementImpossibleException;

/**
 * Script pour les lances
 * @author pf, Krissprolls
 *
 */

public class ScriptLances extends Script {
	

	public ScriptLances(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
		super(hookgenerator, config, log);
	}
	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		if(state.robot.getNbrLances() > 0)
		{
			versionList.add(0);
			versionList.add(1);
		}
		return versionList;
	}

	/*
	 * Le script lance 3 balles sur le c��t�� droit
	 * Le script donne un point d'entr��e
	 */
	@Override
	public  ArrayList<Integer> version_asso(int id_meta)
	{
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		versionList.add(id_meta);
		return versionList;
	}

	@Override
	public Vec2 point_entree(int id) {
		// Les points d'entrée ne sont pas symétriques car le lanceur n'est que d'un seul c��t��
		//if(couleur == "jaune")
		if(id == 0)
			//return new Vec2(400,1400);
			return new Vec2(750,1300);
		else
			//return new Vec2(-1200,1400);
			return new Vec2(-750,1300);
	}
	
	@Override
	public int score(int id_version, final GameState<?> state) {
		return state.robot.getNbrLances()*2;
	}

	@Override
	public int poids(final GameState<?> state) {
		// On s'en fout pour le moment
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void execute(int id_version, GameState<?> state) throws MouvementImpossibleException
	{
		// TODO: tester!
		/*
		int a1,a2,a3;
		
		
		//robot.tourner(0, true);
		robot.tourner((float)(Math.PI), true);
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		Executable tirerballes = new TirerBalles(robot);
		if(id_version ==  0)
		{
			//Il faut toujours que a1<a2<a3 car sinon, on recule et on risque de ne pas voir le robot adverse
			//a1 = 950;
			a1 = 650;
			a2 = 750;
			// a3 = 750;
			a3 = 850;
		}
		else
		{
			// a1 = -650;
			a1 = -850;
			a2 = -750;
			// a3 = -850;
			a3 = -650;
		}
		// Hook pour la 1ere balle
		Hook hook1 = hookgenerator.hook_abscisse(a1);
		hook1.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook1);
		
		// Hook pour la 2e balle
		Hook hook2 = hookgenerator.hook_abscisse(a2);
		hook2.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook2);
		
		// Hook pour la 3e balle
		Hook hook3 = hookgenerator.hook_abscisse(a3);
		hook3.ajouter_callback(new Callback(tirerballes, true));
		hooks.add(hook3);
		*/
	    state.robot.tourner_sans_symetrie((float)(Math.PI));
		//Abadon des hooks, on fait donc tout à la main
	    state.robot.tirerBalle();
	    state.robot.sleep(800);
	    state.robot.tirerBalle();
	    state.robot.sleep(800);
	    state.robot.tirerBalle();
	    state.robot.sleep(1500);
		//robot.set_vitesse_translation("vitesse_mammouth");
		//robot.avancer(600, hooks);
		
	}

	@Override
	protected void termine(GameState<?> state) {
		// vide car rien qui gène
	}
	
	public String toString()
	{
		return "ScriptLances";
	}

}

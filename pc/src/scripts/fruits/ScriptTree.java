package scripts.fruits;

import robot.RobotVrai;
import robot.Vitesse;
import scripts.Script;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;
import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.methodes.LeverRateau;
import hook.sortes.HookGenerator;

import java.util.ArrayList;

import enums.Cote;
import enums.PositionRateau;
import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;

/**
 * Script de prise de fruits
 * @author pf
 * @author krissprolls
 *
 */
public class ScriptTree extends Script{

    private ArrayList<ArrayList<Hook>> hooks = null;
    
	public ScriptTree(HookGenerator hookgenerator, Read_Ini config, Log log)
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
	}

	@Override 
	public  ArrayList<Integer> meta_version(final GameState<?> state)
	{
		ArrayList<Integer> metaversionList = new ArrayList<Integer>();
		if (state.robot.isFresquesPosees())
		{
			for (int i = 0; i < 4; i++)
				if (!state.table.isTreeTaken(i))
					metaversionList.add(i);
		}
		return metaversionList;
	}

	@Override
	public Vec2 point_entree(int id_version){
		//Les points d'entrée véritables sont mis en commentaire
		//Quand tout marchera correctement, ça sera ces points qui faudra retenir
		if (id_version == 0)
			return new Vec2(1000, 700);

		else if (id_version == 1)
			return new Vec2(800, 500);

		else if (id_version == 2)
			return new Vec2(-800, 500);

		else if (id_version == 3)
			return new Vec2(-1000, 700);
		
		log.critical("Version/Métaversion inconnue", this);
		return null;
	}
	@Override
	public int score(int id_version, final GameState<?> state)
	{
		int res = 0;
		
		if (id_version <= 1)
			res = state.table.nbrTotalTree(0) + state.table.nbrTotalTree(1);
		else if(id_version <= 3)
			res = state.table.nbrTotalTree(2) + state.table.nbrTotalTree(3);
		else
			log.critical("Version/Métaversion inconnue", this);
		return res;
	}

	@Override
	public int poids(final GameState<?> state)
	{
		return 1;
	}

    @SuppressWarnings("unchecked")
    @Override
	protected void execute(int id_version, GameState<?> state) throws MouvementImpossibleException, SerialException
	{
	    if(hooks == null && state.robot instanceof RobotVrai)
	    {
	        hooks = new ArrayList<ArrayList<Hook>>();
	        for(int i = 0; i < 4; i++)
	        {
	            hooks.add(new ArrayList<Hook>());
                initialise_hooks(Cote.DROIT, (GameState<RobotVrai>)state, i);
                initialise_hooks(Cote.GAUCHE, (GameState<RobotVrai>)state, i);
	        }
        }
	    
		// Orientation du robot, le rateau étant à l'arrière
		if (id_version == 0)
			state.robot.tourner(Math.PI);
		else if (id_version == 1 || id_version == 2)
			state.robot.tourner(Math.PI/2);
		else if (id_version == 3)
			state.robot.tourner(0) ;

		// on déploie les bras 
		state.robot.rateau(PositionRateau.BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.BAS, Cote.GAUCHE);
		
		// on avance et on rebaisse les rateaux au min
		state.robot.avancer_dans_mur(-400);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.GAUCHE);
		state.robot.sleep(500);
		//On remonte juste un peu pour éviter que les rateaux cognent sur le rebord de la table
		state.robot.rateau(PositionRateau.BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.BAS, Cote.GAUCHE);
		// on remonte les bras à mi-hauteur en fonction de la position du fruit pourri, tout en reculant
		
		state.robot.set_vitesse(Vitesse.ARBRE_AVANT);
		
		state.robot.add_fruits(state.table.nbrTree(id_version, Cote.DROIT) + state.table.nbrTree(id_version, Cote.GAUCHE));
		state.table.pickTree(id_version);
		state.robot.avancer(350, hooks.get(id_version));
		state.robot.sleep(1000);
	}

	@Override
	protected void termine(GameState<?> state) {
		try {
			state.robot.rateau(PositionRateau.RANGER, Cote.DROIT);
			state.robot.rateau(PositionRateau.RANGER, Cote.GAUCHE);
		} catch (SerialException e) {
			e.printStackTrace();
		}
	}

	public String toString()
	{
		return "ScriptTree";
	}
	
	/**
	 * Initialise les hooks une fois pour toutes
	 * @param cote
	 * @param state
	 * @param version
	 */
	private void initialise_hooks(Cote cote, GameState<RobotVrai> state, int version)
	{
            int nbFruits = state.table.nbrTree(version, cote) ;
            Executable remonte = new LeverRateau(state.robot, cote);
            Hook hook;

            if(nbFruits == 0)
                return; // pas de hook
            
            int distance = 0;
            
            if(nbFruits == 3)
                distance = 150;
            else if(nbFruits == 2)
                distance = 250;
            else if(nbFruits == 1)
                distance = 350;

            if(version == 0)
                hook = hookgenerator.hook_abscisse(1500-distance, 35);
            else if(version == 1 || version == 2)
                hook = hookgenerator.hook_ordonnee(distance, 35);
            else // version == 3
                hook = hookgenerator.hook_abscisse(-1500+distance, 35);
                
            hook.ajouter_callback(new Callback(remonte, true));
            hooks.get(version).add(hook);
	}
	
}

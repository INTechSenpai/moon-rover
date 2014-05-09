package scripts.fruits;

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

	public ScriptTree(HookGenerator hookgenerator, Read_Ini config, Log log)
	{
		super(hookgenerator, config, log);
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
	public ArrayList<Integer> version_asso(int id_meta)
	{
		ArrayList<Integer> versionList = new ArrayList<Integer>();
		versionList.add(id_meta);
		return versionList;
	}

	@Override
	public Vec2 point_entree(int id_version){
		//Les points d'entrée véritables sont mis en commentaire
		//Quand tout marchera correctement, ça sera ces points qui faudra retenir
		if (id_version == 0)
			return new Vec2(1000, 700);
			//1000,700
		else if (id_version == 1)
			return new Vec2(800, 500);
			//800,500
		else if (id_version == 2)
			return new Vec2(-800, 500);
			//-800,500
		else if (id_version == 3)
			//-1000, 700
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

	@Override
	protected void execute(int id_version, GameState<?> state) throws MouvementImpossibleException, SerialException
	{
		// Orientation du robot, le rateau étant à l'arrière
		int recul = 0;
		if (id_version == 0)
			state.robot.tourner((float)Math.PI);
		else if (id_version == 1 || id_version == 2)
			state.robot.tourner((float) (Math.PI / 2));
		else if (id_version == 3)
			state.robot.tourner(0) ;
		//Les reculs servent à calibrer l'avancement du robot lors de la prise des fruits
		//50 est plutôt trop prudent
		//30 est ce qui est à retenir pour id_version valant 0 et 3
		//et 0 pour id_version 1 et 2
		//résultats obtenus une semaine avant la pré-coupe
		recul = 50;

		// on déploie les bras 
		state.robot.rateau(PositionRateau.BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.BAS, Cote.GAUCHE);
		
		// on avance et on rebaisse les rateaux au min
		state.robot.set_vitesse_translation("arbre_arriere");
		state.robot.avancer(-318+recul);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.GAUCHE);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.GAUCHE);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.GAUCHE);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.SUPER_BAS, Cote.GAUCHE);
		state.robot.sleep(500);
		//pour être sûr
		//On remonte juste un peu pour éviter que les rateaux cognent sur le rebord de la table
		state.robot.rateau(PositionRateau.BAS, Cote.DROIT);
		state.robot.rateau(PositionRateau.BAS, Cote.GAUCHE);
		// on remonte les bras à mi-hauteur en fonction de la position du fruit pourri, tout en reculant
		
		ArrayList<Hook> hooks = new ArrayList<Hook>();
		
		Cote cote = Cote.GAUCHE;
		do {
			int nbFruits = state.table.nbrTree(id_version, cote) ;
			Executable remonte = new LeverRateau(state.robot, cote);
			double distance = 0;
			if(nbFruits == 3)
				distance = 0;
			else if(nbFruits == 2)
				distance = 75;
			else if(nbFruits == 1)
				distance = 200;
			else if(nbFruits == 0)
				distance = 310;
			Vec2 diff = new Vec2((int)(distance*Math.cos((double)state.robot.getOrientation())),(int)(distance*Math.sin((double)state.robot.getOrientation())));
			Hook hook = hookgenerator.hook_position(state.robot.getPosition().PlusNewVector(diff));
			hook.ajouter_callback(new Callback(remonte, true));
			hooks.add(hook);

			if(cote == Cote.GAUCHE)
				cote = Cote.DROIT;
			else
				cote = Cote.GAUCHE;
		} while(cote == Cote.DROIT);
		state.robot.set_vitesse_translation("arbre_avant");
		//log.debug("adding " + state.table.nbrTree(id_version, Cote.DROIT) + state.table.nbrTree(id_version, Cote.GAUCHE) + " fruits to the bac", this);
		//l'ordre  est très important !!!
		state.robot.add_fruits(state.table.nbrTree(id_version, Cote.DROIT) + state.table.nbrTree(id_version, Cote.GAUCHE));
		state.table.pickTree(id_version);
		state.robot.avancer(318-recul, hooks);
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
	
}

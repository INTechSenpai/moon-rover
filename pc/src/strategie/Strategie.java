package strategie;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import strategie.NoteScriptVersion;
import strategie.arbre.Branche;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;
import container.Service;
import exception.ScriptException;
import smartMath.Vec2;

/**
 * Classe qui prend les décisions et exécute les scripts
 * @author pf, krissprolls
 *
 */

public class Strategie implements Service {

	// Dépendances
	private MemoryManager memorymanager;
	private ThreadTimer threadtimer;
	private ScriptManager scriptmanager;
	private GameState<RobotVrai> real_state;
	private Log log;
	
	private Map<String,Integer> echecs = new Hashtable<String,Integer>();

	private NoteScriptVersion scriptEnCours;
	
	public int TTL; //time toDatUltimateBest live

	// TODO initialisations des variables = première action
	// Prochain script à exécuter si on est interrompu par l'ennemi
	private volatile NoteScriptVersion prochainScriptEnnemi;
	
	// Prochain script à exécuter si l'actuel se passe bien
	private volatile NoteScriptVersion prochainScript;
	
	public Strategie(MemoryManager memorymanager, ThreadTimer threadtimer, ScriptManager scriptmanager, GameState<RobotVrai> real_state, Read_Ini config, Log log)
	{
		this.memorymanager = memorymanager;
		this.threadtimer = threadtimer;
		this.scriptmanager = scriptmanager;
		this.real_state = real_state;
		this.log = log;
		maj_config();
	}
	
	/**
	 * Méthode appelée à la fin du lanceur et qui exécute la meilleure stratégie (calculée dans threadStrategie)
	 */
	public void boucle_strategie()
	{
		while(!threadtimer.match_demarre)
			Sleep.sleep(20);

		log.debug("Stratégie lancée", this);
		while(!threadtimer.fin_match)
		{
			synchronized(prochainScript)
			{
				if(prochainScript != null)
				{
					scriptEnCours = prochainScript.clone();
					prochainScript = null;
				}
				else
					scriptEnCours = null;
			}

			if(scriptEnCours != null)
			{
				boolean dernier = (nbScriptsRestants() == 1);
				log.debug("Stratégie fait: "+scriptEnCours+", dernier: "+dernier, this);
				// le dernier argument, retenter_si_blocage, est vrai si c'est le dernier script. Sinon, on change de script sans attendre
				try {
					scriptEnCours.script.agit(scriptEnCours.version, real_state, dernier);
				}
				catch(Exception e)
				{
					// enregistrement de l'erreur
					String nom = scriptEnCours.script.toString();
					if(echecs.containsKey(nom))
					{
						int nb = echecs.get(nom);
						echecs.put(nom, nb+1);
					}
					else
						echecs.put(nom, 1);
				}
			}
			else
			{
				log.critical("Aucun ordre n'est à disposition. Attente.", this);
				Sleep.sleep(25);/**
				 * Méthode qui, à partir de la durée de freeze et de l'emplacement des ennemis, tire des conclusions.
				 * Exemples: l'ennemi vide cet arbre, il a posé sa fresque ici, ...
				 * Modifie aussi la variable TTL Time To Live!
				 */
			}

		}
		log.debug("Arrêt de la stratégie", this);
	}

	/**
	 * Méthode qui, à partir de la durée de freeze et de l'emplacement des ennemis, tire des conclusions.
	 * Exemples: l'ennemi vide cet arbre, il a posé sa fresque ici, ...
	 * Modifie aussi la variable TTL!long
	 */
	public void analyse_ennemi(Vec2[] positionsfreeze, int[] duree_freeze)
	//en fait on n'a pas besoin de la date des freezes mais de la durée des freeze
	{
		//
		int distance_influence = 500; //50 cm
		int duree_standard = 3000; //3 secondes
		int duree_blocage = 10000; //10 secondes
		//int larg_max = 100; //10 cm est la largeur maximale de la fresque
		//valeur amenée à être modifiée
		//inutile en fait
		int i_min_fire;
		int i_min_tree ;
		int i_min_fresco;
		for(int i = 0; i <2; i++)
		{
			/*
			 * Je mets en garde contre la façon dont peut être utilisé positionsfreeze
			 * en effet, une fois que le robot adverse a été considéré commme preneur
			 * de feu ou de fruit, alors il faut remettre à 0 le compteurmais si on fait ça, 
			 * on ne se prémunit pas, entre autre, contre les freezes
			 * 			 * 
			 */
			i_min_fire = real_state.table.nearestUntakenFire(positionsfreeze[i]);
			i_min_tree = real_state.table.nearestUntakenTree(positionsfreeze[i]);
			i_min_fresco = real_state.table.nearestFreeFresco(positionsfreeze[i]);
			
			if (duree_freeze[i] > duree_blocage)
			{
				//Il y a un blocage de l'ennemi, réfléchissons un peu et agissons optimalement
			}
			if (real_state.table.distanceTree(positionsfreeze[i], i_min_fire) < distance_influence && duree_freeze[i] > duree_standard)
			{
			    real_state.table.pickTree(i_min_tree);
			}
			if(real_state.table.distanceFire(positionsfreeze[i], i_min_tree) < distance_influence && duree_freeze[i] > duree_standard)
			{
			    real_state.table.pickFire(i_min_fire);
			}
			if(real_state.table.distanceFresco(positionsfreeze[i], i_min_tree) < distance_influence && duree_freeze[i] > duree_standard)
			{
			    real_state.table.appendFresco(i_min_fresco);
			}
			
			/*
			 * 
			 * else if(table.distanceFresque(positionsfreeze[i], i_min_tree) < distance_influence && duree_freeze[i] > duree_standard)
				{
				table.putOnFresque(larg_max);
				
				}
			 * 
			 * 
			 */
			 
			
		}
		/*
		 *On prend pas en compte le lancer de balles
		 *car on aura pas d'information sur le lancer potentiel qu'un adversaire a fait
		 *Et pour la funny action, il n'y a pas de stratédie nécessaire
		 * 
		 *  
		 */
		
		
		// modificiation de la table en conséquence
		/*
		 * Où l'ennemi dépose-t-il ses feux?
		 * Où l'ennemi dépose-t-il sa fresque?
		 * Quel arbre récupère-t-il?
		 * Quelle torche vide-t-il?
		 * Où tire-t-il ses balles? (tirer au moins une balle là où il a tiré)
		 */
		
		// Plus le robot ennemi reste fixe, plus le TTL doit être grand.
		// Le TTL est une durée en ms sur laquelle on estime que le robot demeurera immobile
		
	} 
	public float[] meilleurVersion(int meta_id, Script script, GameState<RobotChrono> state)
	{
		int id = 0;
		float meilleurNote = 0;
		int score;
		int duree_script; 
		for(int i : script.version_asso(meta_id))
		{
			score = script.score(id, state);
			duree_script = (int)script.calcule(id, state, true);
			if(calculeNote(score,duree_script, i,script, state)>meilleurNote)
			{
				id = i;
				meilleurNote = calculeNote(score,duree_script, i,script,state);
			}
			
		}
		float[] a= {id, meilleurNote};
		return a;
	}
	
	
	/**
	 * La note d'un script est fonction de son score, de sa durée, de la distance de l'ennemi à l'action 
	 * @param score
	 * @param duree
	 * @param id
	 * @param script
	 * @return note
	 */
	private float calculeNote(int score, int duree, int id, Script script, GameState<?> state)
	{
		// TODO
		
		int A = 1;
		int B = 1;
		float prob = script.proba_reussite();
		
		//abandon de prob_deja_fait
		Vec2[] position_ennemie = state.table.get_positions_ennemis();
		float pos = (float)1.0 - (float)(Math.exp(-Math.pow((double)(script.point_entree(id).distance(position_ennemie[0])),(double)2.0)));
		// pos est une valeur qui décroît de manière exponentielle en fonction de la distance entre le robot adverse et là où on veut aller
		float note = (score*A*prob/duree+pos*B)*prob;
		
//		log.debug((float)(Math.exp(-Math.pow((double)(script.point_entree(id).distance(position_ennemie[0])),(double)2.0))), this);
		
		return note;
	}
	

	/**
	 * La note d'un script est fonction de son score, de sa durée, de la distance de l'ennemi à l'action 
	 * @param score
	 * @param duree
	 * @param id
	 * @param script
	 * @return note
	 */
	private float calculeMetaNote(int score, int duree, int meta_id, Script script, GameState<?> state)
	{
		// TODO
		int id = script.version_asso(meta_id).get(0);
		int A = 1;
		int B = 1;
		float prob = script.proba_reussite();
		
		//abandon de prob_deja_fait
		Vec2[] position_ennemie = state.table.get_positions_ennemis();
		float pos = (float)1.0 - (float)(Math.exp(-Math.pow((double)(script.point_entree(id).distance(position_ennemie[0])),(double)2.0)));
		// pos est une valeur qui décroît de manière exponentielle en fonction de la distance entre le robot adverse et là où on veut aller
		float note = (score*A*prob/duree+pos*B)*prob;
		
//		log.debug((float)(Math.exp(-Math.pow((double)(script.point_entree(id).distance(position_ennemie[0])),(double)2.0))), this);
		
		return note;
	}

	/**
	 * Evaluation des scripts pour un robot et une certaine profondeur
	 * @param profondeur
	 * @param id_robot
	 * @return le meilleur triplet NoteScriptVersion
	 * @throws ScriptException
	 */
/*	public NoteScriptMetaversion evaluation(int profondeur, int id_robot) throws ScriptException
	{
		return _evaluation(System.currentTimeMillis(), 0, profondeur, id_robot);
	}
*/
	/**
	 * Evaluation des scripts pour un robot et une certaine profondeur, à partir d'une date future
	 * @param date
	 * @param profondeur
	 * @param id_robot
	 * @return le meilleur triplet NoteScriptVersion
	 * @throws ScriptException
	 */
/*	public NoteScriptMetaversion evaluation(long date, int profondeur, int id_robot) throws ScriptException
	{
		return _evaluation(date, 0, profondeur, id_robot);
	}

	private NoteScriptMetaversion _evaluation(long date, int duree_totale, int profondeur, int id_robot) throws ScriptException
	{
		if(profondeur == 0)
			return new NoteScriptMetaversion();
		
		NoteScriptMetaversion meilleur = new NoteScriptMetaversion(-1, null, -1);
		// TODO : Give a value to TTL
		int duree_connaissances = TTL;
		
		// Ittération sur les scripts
		for(String nom_script : scriptmanager.getNomsScripts())
		{
			Script script = scriptmanager.getScript(nom_script);
			ArrayList<Integer> metaversions = script.meta_version(	memorymanager.getCloneRobotChrono(profondeur),
																	memorymanager.getCloneTable(profondeur), 
																	memorymanager.getClonePathfinding(profondeur)	);
			// TODO corriger les scripts pour que ça n'arrive pas
			if(metaversions == null)
				break;
			
			
			
			// Ittération sur les métaversions des scripts
			for(int meta_id : metaversions)
			{
				
				try
				{
					Table cloned_table = memorymanager.getCloneTable(profondeur);
					RobotChrono cloned_robotchrono = memorymanager.getCloneRobotChrono(profondeur);
					Pathfinding cloned_pathfinding = memorymanager.getClonePathfinding(profondeur);

					int duree_script = (int)script.metacalcule(meta_id, cloned_robotchrono, cloned_table, cloned_pathfinding, duree_totale > duree_connaissances);
					
					// met a jour la table après exécution du script
					cloned_table.supprimerObstaclesPerimes(date+duree_script);

					float noteScript = calculeMetaNote(	script.meta_score(meta_id, cloned_robotchrono, cloned_table),
														duree_script,
														meta_id,
														script	);

					NoteScriptMetaversion out = _evaluation(date + duree_script, duree_script, profondeur-1, id_robot);
					out.note += noteScript;

					if(out.note > meilleur.note)
					{
						meilleur.note = out.note;
						meilleur.script = script;
						meilleur.metaversion = meta_id;
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				
			}
			
			
		}
		return meilleur;
	}
	
	*/

	
	/* Méthode qui calcule la note de cette branche en calculant celles de ses sous branches, puis en combinant leur notes
	 * C'est là qu'est logé le DFS
	 * 
	 * @param profondeur : la profondeur d'exploration de l'arbre, 1 pour n'explorer qu'un niveau
	 */
	public NoteScriptMetaversion evaluate(int profondeur)
	{
		/*
		 * 	Algorithme : Itterative Modified DFS
		 *  ( la différence entre un vrai DFS et l'algo qu'on utilise ici est que la branche parente
		 *    doit être évalué uniquement une fois que tout ses enfants ont étés évalués. Dans un
		 *    DFS normal, c'est le parent qui est évalué d'abord, et ses enfants ensuite )
		 *    
		 * Psuedocode :
		 * 
		 * soit P une pile
		 * mettre la racine au dessus de P 		// Dans notre cas, il y a autant de racines que de prochaine action possible
		 * 
		 * tant que P est non vide
		 * 		v = l'élément du dessus de P
		 * 		si v a des enfants, et qu'ils sont non notés
		 * 			mettre tout les enfants de v au dessus de P
		 * 		sinon
		 * 			calculer la note de v
		 * 			enlever v de P 
		 * 
		 * 
		 */
		
		// Pile des branches de l'arbre qu'il reste a explorer
		Stack<Branche> scope = new Stack<Branche>();
		
		// ajoute les différentes possibiités pour la prochaine action dans la pile
		Script mScript = null;
		ArrayList<Integer> metaversionList;
		GameState<RobotChrono> mState = memorymanager.getClone(0);
		Branche current;
		
		// racourccis pour les racines, afin du calcul du max final :
		ArrayList<Branche> rootList = new ArrayList<Branche>();
		
		
		// ajoute tous les scrips disponibles scripts
		for(String nom_script : scriptmanager.getNomsScripts())
		{
			try
			{
				mScript = scriptmanager.getScript(nom_script);				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			metaversionList = mScript.meta_version(	mState	);
			// TODO corriger les scripts pour que ça n'arrive pas
			if(metaversionList == null)
				continue;
			
			
			// ajoute toutes les métaversions de tous les scipts
			for(int metaversion : metaversionList)
			{
				log.debug("Abwabwa", this);
				//pour débug
				System.out.println(nom_script+"  : "+metaversion);
				scope.push( new Branche(	false,							// N'utilise pas le cache pour le premier niveau de profondeur 
											profondeur,						// Profondeur a laquel déployer des sous branches
											mScript, 						// Une branche par script et par métaversion
											metaversion, 
											mState	) );
				rootList.add(scope.lastElement());
			}	
		}

		
		


		// Boucle principale d'exploration des branches
		while (scope.size() != 0)
		{
			current = scope.lastElement();
			
			// Condition d'ajout des sous-branches : ne pas dépasser le profondeur max, et ne pas les ajouter 2 fois.
			if ( current.profondeur != 0 && (current.sousBranches.size() == 0) )
			{
				// ajoute a la pile a explorer l'ensemble des scripts disponibles pour cet étage		
				// attn profondeur n'est pas la position actuelle mais la taille de l'abre en ava
				mState = memorymanager.getClone(current.profondeur+1);
				// ajoute tous les scrips disponibles
				for(String nomScript : scriptmanager.getNomsScripts())
				{
					try
					{
						mScript = scriptmanager.getScript(nomScript);				
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					
					metaversionList = mScript.meta_version(	mState	);
					// TODO corriger les scripts pour que ça n'arrive pas
					if(metaversionList == null)
						continue;
					
					
					// ajoute toutes les métaversions de tous les scipts
					for(int metaversion : metaversionList)
					{
					    //Att, il faut executer le script a ce moment la sur la branche parente (via scipet::metacalcule) sinon l'état n'est pas changé
					                                //attn, profondeur n'est pas la position bsolue mais la taille de l'abre en aval
						scope.push( new Branche(	current.profondeur >= 2,		// Utiliser le cache dès le second niveau de profondeur
													current.profondeur+1,			// Profondeur a laquel déployer des sous branches
													mScript, 						// Une branche par script et par métaversion
													metaversion, 
													mState	) );
					}
				}
				
				
			}
			else	// Soit on a atteint la profondeur maximale, soit les enfants ont étés traités donc on calcule la note de ce niveau
			{
				current.computeNote();
				scope.pop();
			}
			
		}	// fin boucle principale d'exploration
		
		
		
		// la meilleure action a une meilleure note que les autres branches. Donc on calcule le max des notes des branches 
		NoteScriptMetaversion DatUltimateBest = new NoteScriptMetaversion(-42, null, 0);
		for (int i = 0; i < rootList.size(); ++i)
		{
			current = rootList.get(i);
			if (current.note > DatUltimateBest.note)
			{
				DatUltimateBest.note = current.note;
				DatUltimateBest.script = current.script;
				DatUltimateBest.metaversion = current.metaversion;
				
			}
		}
		
		return DatUltimateBest;
	}
	
	
	
	

	/**
	 * Renvoie le nombre de scripts qui peuvent encore être exécutés (indépendamment de leur nombre de version)
	 * @return
	 */
	private int nbScriptsRestants()
	{
		int compteur = 0;
		for(String nom_script : scriptmanager.getNomsScripts())
		{
			try {
				Script script = scriptmanager.getScript(nom_script);
				if(script.version(real_state).size() >= 1)
					compteur++;		
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		}
		return compteur;
	}
	
	
	/*
	 * GETTERS AND SETTERS
	 */
	
	/**
	 * Permet au thread de stratégie de définir le script à exécuter en cas de rencontre avec l'ennemi
	 * @param prochainScriptEnnemi
	 */
	public void setProchainScriptEnnemi(NoteScriptVersion prochainScriptEnnemi)
	{
		synchronized(this.prochainScriptEnnemi)
		{
			this.prochainScriptEnnemi = prochainScriptEnnemi;
		}
	}

	/**
	 * Permet au thread de stratégie de définir le prochain script à faire
	 * @param prochainScript
	 */
	public void setProchainScript(NoteScriptVersion prochainScript)
	{
		synchronized(this.prochainScript)
		{
			this.prochainScript = prochainScript;
		}
	}
	
	public boolean besoin_ProchainScript()
	{
		synchronized(prochainScript)
		{
			return prochainScript == null;
		}
	}
	
	public NoteScriptVersion getScriptEnCours()
	{
		synchronized(scriptEnCours)
		{
			return scriptEnCours.clone();
		}
	}

	public void maj_config()
	{
	}
	
}


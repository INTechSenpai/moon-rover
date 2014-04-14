package strategie;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import pathfinding.Pathfinding;
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import table.Table;
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
	private Table table;
	private RobotVrai robotvrai;
	private Pathfinding pathfinding;
	private Read_Ini config;
	private Log log;
	
	private Map<String,Integer> echecs = new Hashtable<String,Integer>();

	private NoteScriptVersion scriptEnCours;
	
	public int TTL; //time to live

	// TODO initialisations des variables = première action
	// Prochain script à exécuter si on est interrompu par l'ennemi
	private volatile NoteScriptVersion prochainScriptEnnemi;
	
	// Prochain script à exécuter si l'actuel se passe bien
	private volatile NoteScriptVersion prochainScript;
	
	public Strategie(MemoryManager memorymanager, ThreadTimer threadtimer, ScriptManager scriptmanager, Table table, RobotVrai robotvrai, Pathfinding pathfinding, Read_Ini config, Log log)
	{
		this.memorymanager = memorymanager;
		this.threadtimer = threadtimer;
		this.scriptmanager = scriptmanager;
		this.table = table;
		this.robotvrai = robotvrai;
		this.pathfinding = pathfinding;
		this.config = config;
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
					scriptEnCours.script.agit(scriptEnCours.version, robotvrai, table, pathfinding, dernier);
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
			i_min_fire = table.nearestUntakenFire(positionsfreeze[i]);
			i_min_tree = table.nearestUntakenTree(positionsfreeze[i]);
			i_min_fresco = table.nearestFreeFresco(positionsfreeze[i]);
			
			if (duree_freeze[i] > duree_blocage)
			{
				//Il y a un blocage de l'ennemi, réfléchissons un peu et agissons optimalement
			}
			if (table.distanceTree(positionsfreeze[i], i_min_fire) < distance_influence && duree_freeze[i] > duree_standard)
			{
				table.pickTree(i_min_tree);
			}
			if(table.distanceFire(positionsfreeze[i], i_min_tree) < distance_influence && duree_freeze[i] > duree_standard)
			{
				table.pickFire(i_min_fire);
			}
			if(table.distanceFresco(positionsfreeze[i], i_min_tree) < distance_influence && duree_freeze[i] > duree_standard)
			{
				table.appendFresco(i_min_fresco);
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

	/**
	 * La note d'un script est fonction de son score, de sa durée, de la distance de l'ennemi à l'action 
	 * @param score
	 * @param duree
	 * @param id
	 * @param script
	 * @return
	 */
	private float calculeNote(int score, int duree, int meta_id, Script script)
	{
		// TODO
		int id = script.version_asso(meta_id).get(0);
		int A = 1;
		int B = 1;
		float prob = script.proba_reussite();
		
		//abandon de prob_deja_fait
		Vec2[] position_ennemie = table.get_positions_ennemis();
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
	public NoteScriptVersion evaluation(int profondeur, int id_robot) throws ScriptException
	{
		return _evaluation(System.currentTimeMillis(), 0, profondeur, id_robot);
	}

	/**
	 * Evaluation des scripts pour un robot et une certaine profondeur, à partir d'une date future
	 * @param date
	 * @param profondeur
	 * @param id_robot
	 * @return le meilleur triplet NoteScriptVersion
	 * @throws ScriptException
	 */
	public NoteScriptVersion evaluation(long date, int profondeur, int id_robot) throws ScriptException
	{
		return _evaluation(date, 0, profondeur, id_robot);
	}

	private NoteScriptVersion _evaluation(long date, int duree_totale, int profondeur, int id_robot) throws ScriptException
	{
		if(profondeur == 0)
			return new NoteScriptVersion();
		NoteScriptVersion meilleur = new NoteScriptVersion(-1, null, -1);
		int duree_connaissances = TTL;
		
		for(String nom_script : scriptmanager.getNomsScripts())
		{
			Script script = scriptmanager.getScript(nom_script);
			Table table_version = memorymanager.getCloneTable(profondeur);
			RobotChrono robotchrono_version = memorymanager.getCloneRobotChrono(profondeur);
			Pathfinding pathfinding_version = memorymanager.getClonePathfinding(profondeur);
			//ArrayList<Integer> versions = script.version(robotchrono_version, table_version, pathfinding_version);
			ArrayList<Integer> metaversions = script.meta_version(robotchrono_version, table_version, pathfinding_version);
			// TODO corriger les scripts pour que ça n'arrive pas
			if(metaversions == null)
				break;
			for(int meta_id : metaversions)
			{
				try
				{
					Table cloned_table = memorymanager.getCloneTable(profondeur);
					RobotChrono cloned_robotchrono = memorymanager.getCloneRobotChrono(profondeur);
					Pathfinding cloned_pathfinding = memorymanager.getClonePathfinding(profondeur);
					//int score = script.score(id, cloned_robotchrono, cloned_table);
					int score = script.meta_score(meta_id, cloned_robotchrono, cloned_table);
					int duree_script = (int)script.metacalcule(meta_id, cloned_robotchrono, cloned_table, cloned_pathfinding, duree_totale > duree_connaissances);
					//log.debug("Durée de "+script+" "+id+": "+duree_script, this);
					cloned_table.supprimer_obstacles_perimes(date+duree_script);
					//log.debug("Score de "+script+" "+id+": "+score, this);
					float noteScript = calculeNote(score, duree_script, meta_id, script);
					//log.debug("Note de "+script+" "+id+": "+noteScript, this);
					NoteScriptVersion out = _evaluation(date + duree_script, duree_script, profondeur-1, id_robot);
					out.note += noteScript;

					if(out.note > meilleur.note)
					{
						meilleur.note = out.note;
						meilleur.script = script;
						meilleur.version = meta_id;
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
				RobotChrono robotchrono = new RobotChrono(config, log);
				// TODO
				Pathfinding pathfinding = null;
				if(script.version(robotchrono, table, pathfinding).size() >= 1)
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


package strategie;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import table.Table;
import threads.ThreadAnalyseEnnemi;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;
import container.Service;
import exception.ScriptException;
import smartMath.Vec2;
import robot.cartes.Laser;

/**
 * Classe qui prend les décisions et exécute les scripts
 * @author pf
 *
 */

public class Strategie implements Service {

	// Dépendances
	private MemoryManager memorymanager;
	private ThreadAnalyseEnnemi threadanalyseennemi;
	private ThreadTimer threadtimer;
	private ScriptManager scriptmanager;
	private Table table;
	private RobotVrai robotvrai;
	private Read_Ini config;
	private Log log;
	
	private Map<String,Integer> echecs = new Hashtable<String,Integer>();

	private NoteScriptVersion scriptEnCours;
	
	public int TTL;

	// TODO initialisations des variables = première action
	// Prochain script à exécuter si on est interrompu par l'ennemi
	private NoteScriptVersion prochainScriptEnnemi;
	
	// Prochain script à exécuter si l'actuel se passe bien
	private NoteScriptVersion prochainScript;
	
	public Strategie(MemoryManager memorymanager, ThreadAnalyseEnnemi threadanalyseennemi, ThreadTimer threadtimer, ScriptManager scriptmanager, Table table, RobotVrai robotvrai, Read_Ini config, Log log)
	{
		this.memorymanager = memorymanager;
		this.threadanalyseennemi = threadanalyseennemi;
		this.threadtimer = threadtimer;
		this.scriptmanager = scriptmanager;
		this.table = table;
		this.robotvrai = robotvrai;
		this.config = config;
		this.log = log;
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
					scriptEnCours.script.agit(scriptEnCours.version, robotvrai, table, dernier);
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
	 * Modifie aussi la variable TTL!
	 */
	public void analyse_ennemi()
	{
		int[] duree_freeze = threadanalyseennemi.duree_freeze();
		//pourquoi avoir un tableau à 2 dimensions pour la durée_freeze
		
		Vec2[] pos_ennemi = threadanalyseennemi.positionsfreeze;
		/*table.Fire[] arrayFire = table.getFire();
				
		for(int i = 0; i < 10; i++)
		{
			
		}*/
		
		/*Si ça n'a pas été vraiment codé, c'est parce qu'il faut utiliser Container (ou pas) et on sait pas encore comment
		 * Pour chaque feu 
		 * si rayon_feu +rayon_robot_adverse > distance(feu, robot_adverse) et duree_freeze > duree_prise_feu_generique alors
		 *	feu pris
		 *Pour chaque arbre 
		 *si rayon_arbre +rayon_robot_adverse > distance(arbre, robot_adverse) et duree_freeze > duree_prise_feu_generique alors
		 *	fruits pris
		 *Pour chaque bac
		 *si dimensions_bac +rayon_robot_adverse > distance(bac, robot_adverse) et duree_freeze_depot > duree_prise_feu_generique alors
		 *	fruits déposés
		 *On prend pas en compte le lancer de balles
		 *
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
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
	}

	/**
	 * La note d'un script est fonction de son score, de sa durée, de la distance de l'ennemi à l'action 
	 * @param score
	 * @param duree
	 * @param id
	 * @param script
	 * @return
	 */
	private float calculeNote(int score, int duree, int id, Script script)
	{
		// TODO
		int A = 1;
		int B = 1;
		float prob = script.proba_reussite();
		
		//abandon de prob_deja_fait
		Vec2[] position_ennemie = table.get_positions_ennemis();
		float pos = (float)1.0 - (float)(Math.exp(-Math.pow((double)(script.point_entree(id).distance(position_ennemie[0])),(double)2.0)));
		// pos est une valeur qui décroît de manière exponentielle en fonction de la distance entre le robot adverse et là où on veut aller
		float note = (score*A*prob/duree+pos*B)*prob;
		
		log.debug((float)(Math.exp(-Math.pow((double)(script.point_entree(id).distance(position_ennemie[0])),(double)2.0))), this);
		
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
		
		for(String nom_script : scriptmanager.getNomsScripts(id_robot))
		{
			Script script = scriptmanager.getScript(nom_script);
			Table table_version = memorymanager.getCloneTable(profondeur);
			RobotChrono robotchrono_version = memorymanager.getCloneRobotChrono(profondeur);
			ArrayList<Integer> versions = script.version(robotchrono_version, table_version);

			for(int id : versions)
			{
				try
				{
					Table cloned_table = memorymanager.getCloneTable(profondeur);
					RobotChrono cloned_robotchrono = memorymanager.getCloneRobotChrono(profondeur);
					int score = script.score(id, cloned_robotchrono, cloned_table);
					int duree_script = (int)script.calcule(id, cloned_robotchrono, cloned_table, duree_totale > duree_connaissances);
					log.debug("Durée de "+script+" "+id+": "+duree_script, this);
					cloned_table.supprimer_obstacles_perimes(date+duree_script);
					log.debug("Score de "+script+" "+id+": "+score, this);
					float noteScript = calculeNote(score, duree_script, id, script);
					log.debug("Note de "+script+" "+id+": "+noteScript, this);
					NoteScriptVersion out = _evaluation(date + duree_script, duree_script, profondeur-1, id_robot);
					out.note += noteScript;

					if(out.note > meilleur.note)
					{
						meilleur.note = out.note;
						meilleur.script = script;
						meilleur.version = id;
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
		for(String nom_script : scriptmanager.getNomsScripts(0))
		{
			try {
				Script script = scriptmanager.getScript(nom_script);
				RobotChrono robotchrono = new RobotChrono(config, log);
				if(script.version(robotchrono, table).size() >= 1)
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


}


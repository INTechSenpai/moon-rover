package strategie;

import java.util.ArrayList;

import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import scripts.ScriptManager;
import table.Table;
import threads.ThreadAnalyseEnnemi;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exception.ScriptException;

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
	
	public Script scriptEnCours;
	public int versionScriptEnCours;
	
	public int TTL;
	
	// TODO initialisations des variables = première action
	// Prochain script à exécuter si on est interrompu par l'ennemi
	public Script prochainScriptEnnemi;
	public Integer versionProchainScriptEnnemi;
	
	// Prochain script à exécuter si l'actuel se passe bien
	public Script prochainScript;
	public int versionProchainScript;

	
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
		while(!threadtimer.fin_match)
		{
			while(prochainScript == null)
			{
				synchronized(versionProchainScriptEnnemi)
				{
					synchronized(prochainScript)
					{
						scriptEnCours = prochainScript;
						versionScriptEnCours = versionProchainScriptEnnemi;
						prochainScript = null;
					}
				}
				if(scriptEnCours == null)
				{
					log.warning("Aucun ordre n'est à disposition.", this);
					try {
						Thread.sleep(500);
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
				}
			}
	
			log.debug("Stratégie fait: "+scriptEnCours.toString()+", version "+Integer.toString(versionScriptEnCours), this);
			scriptEnCours.agit(versionScriptEnCours, robotvrai, table, true); // le dernier argument, retenter_si_blocage, est vrai si c'est le dernier script. Sinon, on change de script sans attendre
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
		
		// modificiation de la table en conséquence
		/*
		 * Où l'ennemi dépose-t-il ses feux?
		 * Où l'ennemi dépose-t-il sa fresque?
		 * Quel arbre récupère-t-il?
		 * Quelle torche vide-t-il?
		 */
	}

	/**
	 * La note d'un script est fonction de son score, de sa durée, de la distance de l'ennemi
	 * @param score
	 * @param duree
	 * @param id
	 * @return
	 */
	private float calculeNote(int score, int duree, int id)
	{
		return 0;
	}

	public NoteScriptVersion evaluation(int profondeur, int id_robot) throws ScriptException
	{
		return _evaluation(System.currentTimeMillis(), 0, profondeur, id_robot);
	}
	
	private NoteScriptVersion _evaluation(long date, int duree_totale, int profondeur, int id_robot) throws ScriptException
	{
		if(profondeur == 0)
			return new NoteScriptVersion();
		table.supprimer_obstacles_perimes(date);
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
					float noteScript = calculeNote(score, duree_script, id);
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
					log.critical(e, this);
				}
			}
		}
		return meilleur;
	}
}

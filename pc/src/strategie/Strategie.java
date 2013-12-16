package strategie;

import java.util.ArrayList;

import pathfinding.Pathfinding;
import robot.RobotChrono;
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
	private ThreadTimer threadTimer;
	private ScriptManager scriptmanager;
	private Pathfinding pathfinding;
	private Table table;
	private Read_Ini config;
	private Log log;
	
	public Script scriptEnCours;
	public int versionScriptEnCours;
	
	// TODO initialisations des variables = première action
	// Prochain script à exécuter si on est interrompu par l'ennemi
	public Script prochainScriptEnnemi;
	public int versionProchainScriptEnnemi;
	
	// Prochain script à exécuter si l'actuel se passe bien
	public Script prochainScript;
	public int versionProchainScript;

	
	public Strategie(MemoryManager memorymanager, ThreadAnalyseEnnemi threadanalyseennemi, ThreadTimer threadTimer, ScriptManager scriptmanager, Pathfinding pathfinding, Table table, Read_Ini config, Log log)
	{
		this.memorymanager = memorymanager;
		this.threadanalyseennemi = threadanalyseennemi;
		this.threadTimer = threadTimer;
		this.scriptmanager = scriptmanager;
		this.pathfinding = pathfinding;
		this.table = table;
		this.config = config;
		this.log = log;
	}
	
	/**
	 * Méthode appelée à la fin du lanceur et qui exécute la meilleure stratégie (calculée dans threadStrategie)
	 */
	public void boucle_strategie()
	{
		scriptEnCours = prochainScript;
		versionScriptEnCours = versionProchainScriptEnnemi;
		
	}
	
	public void analyse_ennemi()
	{
		int[] duree_freeze = threadanalyseennemi.duree_freeze();
		
		// modificiation de la table en conséquence
	}

	public int getTTL()
	{
		return 0;
	}
	
	public float calculeNote(int score, int duree, int id)
	{
		return 0;
	}

	public NoteScriptVersion evaluation(int profondeur) throws ScriptException
	{
		return _evaluation(System.currentTimeMillis(), 0, profondeur);
	}
	
	private NoteScriptVersion _evaluation(long date, int duree_totale, int profondeur) throws ScriptException
	{
		if(profondeur == 0)
			return new NoteScriptVersion();
		else
		{
			table.supprimer_obstacles_perimes(date);
			NoteScriptVersion meilleur = new NoteScriptVersion(-1, null, -1);
			int duree_connaissances = getTTL();
			
			for(String nom_script : scriptmanager.getNomsScripts())
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
						NoteScriptVersion out = _evaluation(date + duree_script, duree_script, profondeur-1);
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
}

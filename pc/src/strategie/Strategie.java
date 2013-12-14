package strategie;

import pathfinding.Pathfinding;
import robot.RobotChrono;
import scripts.Script;
import scripts.ScriptManager;
import table.Table;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Classe qui prend les décisions et exécute les scripts
 * @author pf
 *
 */

public class Strategie implements Service {

	// Dépendances
	private MemoryManager memorymanager;
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

	
	public Strategie(ThreadTimer threadTimer, ScriptManager scriptmanager, Pathfinding pathfinding, Table table, Read_Ini config, Log log)
	{
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
	
	public float calculeNote(Script script, int id)
	{
		return 0;
	}

	/**
	 * A partir d'un état initial (table, robotchrono), calcule la meilleure combinaison de scripts modulo une certaine profondeur maximale
	 * @param table
	 * @param robotchrono
	 * @param pathfinding
	 * @param profondeur
	 * @return le couple (note, scripts), scripts étant la suite de scripts à effectuer
	 */
	public NoteScriptVersion evaluation(long date, MemoryManager memorymanager, Pathfinding pathfinding, int profondeur)
	{
		if(profondeur == 0)
			return new NoteScriptVersion();
		else
		{
			table.supprimer_obstacles_perimes(date);
			NoteScriptVersion meilleur = new NoteScriptVersion(-1, null, -1);
			
			for(String nom_script : scriptmanager.scripts)
				for(int id : scriptmanager.getId(nom_script))
				{
					Table cloned_table = memorymanager.getCloneTable(profondeur);
					RobotChrono cloned_robotchrono = memorymanager.getCloneRobotChrono(profondeur);
					try
					{
						Script script = scriptmanager.getScript(nom_script, cloned_table, cloned_robotchrono, pathfinding);
						long duree_script = script.calcule(id);
						float noteScript = calculeNote(script, id);
						NoteScriptVersion out = evaluation(date + duree_script, memorymanager, pathfinding, profondeur-1);
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
			return meilleur;
		}
	}
}

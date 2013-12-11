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
import factories.FactoryProduct;

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
	
	// TODO initialisations des variables = première action
	// Prochain script à exécuter si on est interrompu par l'ennemi
	public Script prochainScriptEnnemi;
	
	// Prochain script à exécuter si l'actuel se passe bien
	public Script prochainScript;

	
	public Strategie(Service memorymanager, Service threadTimer, Service scriptmanager, Service pathfinding, Service table, Service config, Service log)
	{
		this.memorymanager = (MemoryManager) memorymanager;
		this.threadTimer = (ThreadTimer) threadTimer;
		this.scriptmanager = (ScriptManager) scriptmanager;
		this.pathfinding = (Pathfinding) pathfinding;
		this.table = (Table) table;
		this.config = (Read_Ini) config;
		this.log = (Log) log;
		
	}
	
	/**
	 * Méthode appelée à la fin du lanceur et qui exécute la meilleure stratégie (calculée dans threadStrategie)
	 */
	public void boucle_strategie()
	{
		scriptEnCours = prochainScript;
	}
	
	public float calculeNote(Table cloned_table, RobotChrono cloned_robotchrono)
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
	public CoupleNoteScript evaluation(long date, Table table, RobotChrono robotchrono, Pathfinding pathfinding, int profondeur)
	{
		memorymanager.setModele((FactoryProduct)table);
		memorymanager.setModele((FactoryProduct)robotchrono);
		
		if(profondeur == 0)
			return new CoupleNoteScript();
		else
		{
			table.supprimer_obstacles_perimes(date);
			CoupleNoteScript meilleur = new CoupleNoteScript(-1, null);
			for(String nom_script : scriptmanager.scripts)
				for(int id : scriptmanager.getId(nom_script))
				{
					Table cloned_table = (Table) memorymanager.getClone("Table");
					RobotChrono cloned_robotchrono = (RobotChrono) memorymanager.getClone("RobotChrono");
					Script script = scriptmanager.getScript(nom_script, cloned_table, cloned_robotchrono, pathfinding);
					long duree_script = script.calcule(id);
					float noteScript = calculeNote(cloned_table, cloned_robotchrono);
					CoupleNoteScript out = evaluation(date + duree_script, cloned_table, cloned_robotchrono, pathfinding, profondeur-1);
					out.note += noteScript;

					if(out.note > meilleur.note)
					{
						meilleur.note = out.note;
						meilleur.script = script;
					}
				}
			return meilleur;
		}
	}
}

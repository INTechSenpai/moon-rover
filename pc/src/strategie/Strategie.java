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

	private MemoryManager memorymanager;
	private ThreadTimer threadTimer;
	private ScriptManager scriptmanager;
	private Pathfinding pathfinding;
	private Table table;
	private Read_Ini config;
	private Log log;
	
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
	
	public float calculeNote()
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
	public CoupleNoteScripts evaluation(Service table, Service robotchrono, Service pathfinding, int profondeur)
	{
		memorymanager.setModele((FactoryProduct)table);
		memorymanager.setModele((FactoryProduct)robotchrono);
		
		if(profondeur == 0)
			return new CoupleNoteScripts();
		else
		{
			CoupleNoteScripts meilleur = new CoupleNoteScripts(-1, null);
			for(String nom_script : scriptmanager.scripts)
				for(int id : scriptmanager.getId(nom_script))
				{
					Table cloned_table = (Table) memorymanager.getClone("Table");
					RobotChrono cloned_robotchrono = (RobotChrono) memorymanager.getClone("RobotChrono");
					Script script = scriptmanager.getScript(nom_script, cloned_table, cloned_robotchrono, pathfinding);
					script.calcule(id);
					float noteScript = calculeNote();
					CoupleNoteScripts out = evaluation(cloned_table, cloned_robotchrono, pathfinding, profondeur-1);
					out.note += noteScript;

					if(out.note > meilleur.note)
					{
						meilleur.note = out.note;
						meilleur.scripts = out.scripts;
						meilleur.scripts.add(script);
					}
				}
			return meilleur;
		}
	}
}

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

	private ThreadTimer threadTimer;
	private ScriptManager scriptmanager;
	private Pathfinding pathfinding;
	private Table table;
	private Read_Ini config;
	private Log log;
	
	public Strategie(Service factorystrategie, Service threadTimer, Service scriptmanager, Service pathfinding, Service table, Service config, Service log)
	{
		this.threadTimer = (ThreadTimer) threadTimer;
		this.scriptmanager = (ScriptManager) scriptmanager;
		this.pathfinding = (Pathfinding) pathfinding;
		this.table = (Table) table;
		this.config = (Read_Ini) config;
		this.log = (Log) log;
	}
	
	public CoupleNoteScripts evaluation(Service table, Service robotchrono, Service pathfinding, int profondeur)
	{
		FactoryStrategie factory = new FactoryStrategie(log);
	
		factory.LearnToClone((FactoryProduct)table);
		factory.LearnToClone((FactoryProduct)robotchrono);
		
		if(profondeur == 0)
			return new CoupleNoteScripts();
		else
		{
			for(String nom_script : scriptmanager.scripts)
			{
				for(int id : scriptmanager.getId(nom_script))
				{
					Table cloned_table = (Table) factory.MakeFromString("Table");
					RobotChrono cloned_robotchrono = (RobotChrono) factory.MakeFromString("RobotChrono");
					Script script = scriptmanager.getScript(nom_script, cloned_table, cloned_robotchrono, pathfinding);
					script.calcule(id);
					CoupleNoteScripts out = evaluation(cloned_table, cloned_robotchrono, pathfinding, profondeur-1);
				}
			}
		}
		return null;
	}
}

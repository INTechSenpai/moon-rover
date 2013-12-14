package threads;

import pathfinding.Pathfinding;
import robot.RobotChrono;
import robot.RobotVrai;
import strategie.MemoryManager;
import strategie.NoteScriptVersion;
import strategie.Strategie;
import table.Table;

/**
 * Thread qui calculera en continu la stratégie à adopter
 * @author pf
 *
 */

public class ThreadStrategie extends AbstractThread {

	// Dépendances
	private Strategie strategie;
	private Table table;
	private RobotVrai robotvrai;
	private RobotChrono robotchrono;
	private MemoryManager memorymanager;
	private Pathfinding pathfinding;

	ThreadStrategie(Strategie strategie, Table table, RobotVrai robotvrai, RobotChrono robotchrono, Pathfinding pathfinding, MemoryManager memorymanager)
	{
		this.strategie = strategie;
		this.table = table;
		this.robotvrai = robotvrai;
		this.robotchrono = robotchrono;
		this.pathfinding = pathfinding;
		this.memorymanager = memorymanager;
	}
	
	public void run()
	{
		int profondeur_max = 2;
		while(!stop_threads)
		{
			robotchrono.majRobotChrono(robotvrai);
			// Evaluation d'une stratégie de secours si ce script bug (en premier car plus urgent)
			Table tableBlocage = table;
			tableBlocage.creer_obstacle(robotvrai.getPosition()/*+distance*/);
			memorymanager.setModelTable(tableBlocage, profondeur_max);
			memorymanager.setModelRobotChrono(robotchrono, profondeur_max);
			NoteScriptVersion meilleurErreur = strategie.evaluation(System.currentTimeMillis(), memorymanager, pathfinding, profondeur_max);

			strategie.prochainScriptEnnemi = meilleurErreur.script;
			
			// Evaluation du prochain coup en supposant que celui-ci se passe sans problème

			synchronized(strategie.scriptEnCours)
			{
//				futureTable = strategie.scriptEnCours.futureTable(table, strategie.versionScriptEnCours);
//				futurRobotChrono = strategie.scriptEnCours.futurRobotChrono(robotchrono, strategie.versionScriptEnCours);
			}

			NoteScriptVersion meilleurProchain = strategie.evaluation(System.currentTimeMillis(), memorymanager, pathfinding, profondeur_max);
			
			strategie.prochainScript = meilleurProchain.script;
		}
	}
	
}

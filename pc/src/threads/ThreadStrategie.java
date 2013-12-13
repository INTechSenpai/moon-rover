package threads;

import pathfinding.Pathfinding;
import robot.RobotChrono;
import robot.RobotVrai;
import scripts.Script;
import strategie.NoteScriptVersion;
import strategie.Strategie;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

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
	private Pathfinding pathfinding;

	private Table futureTable;
	private RobotChrono futurRobotChrono;
	
	ThreadStrategie(Read_Ini config, Log log, Strategie strategie, Table table, RobotVrai robotvrai, RobotChrono robotchrono, Pathfinding pathfinding)
	{
		super(config, log);
		this.strategie = strategie;
		this.table = table;
		this.robotvrai = robotvrai;
		this.robotchrono = robotchrono;
		this.pathfinding = pathfinding;
	}
	
	public void run()
	{
		while(!stop_threads)
		{
			robotchrono.initialiserRobotChrono(robotvrai);
			// Evaluation d'une stratégie de secours si ce script bug (en premier car plus urgent)
			Table tableBlocage = table;
			tableBlocage.creer_obstacle(robotvrai.getPosition()/*+distance*/);
			NoteScriptVersion meilleurErreur = strategie.evaluation(System.currentTimeMillis(), table, robotchrono, pathfinding, 2);

			strategie.prochainScriptEnnemi = meilleurErreur.script;
			
			// Evaluation du prochain coup en supposant que celui-ci se passe sans problème

			synchronized(strategie.scriptEnCours)
			{
//				futureTable = strategie.scriptEnCours.futureTable(table, strategie.versionScriptEnCours);
//				futurRobotChrono = strategie.scriptEnCours.futurRobotChrono(robotchrono, strategie.versionScriptEnCours);
			}

			NoteScriptVersion meilleurProchain = strategie.evaluation(System.currentTimeMillis(), futureTable, futurRobotChrono, pathfinding, 2);
			
			strategie.prochainScript = meilleurProchain.script;
		}
	}
	
}

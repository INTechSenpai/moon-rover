package threads;

import pathfinding.Pathfinding;
import robot.RobotChrono;
import strategie.CoupleNoteScripts;
import strategie.Strategie;
import table.Table;
import container.Service;

/**
 * Thread qui calculera en continu la stratégie à adopter
 * @author pf
 *
 */

public class ThreadStrategie extends AbstractThread {

	private Strategie strategie;
	private Table table;
	private RobotChrono robotchrono;
	private Pathfinding pathfinding;
	ThreadStrategie(Service config, Service log, Service strategie, Service table, Service robotchrono, Service pathfinding)
	{
		super(config, log);
		this.strategie = (Strategie) strategie;
		this.table = (Table) table;
		this.robotchrono = (RobotChrono) robotchrono;
		this.pathfinding = (Pathfinding) pathfinding;
	}

	
	public void run()
	{
		while(!stop_threads)
		{
			CoupleNoteScripts meilleur = strategie.evaluation(table, robotchrono, pathfinding, 2);
		}
	}
	
}

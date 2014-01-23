package threads;

import exception.ScriptException;
import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
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

	ThreadStrategie(Strategie strategie, Table table, RobotVrai robotvrai, MemoryManager memorymanager)
	{
		this.strategie = strategie;
		this.table = table;
		this.robotvrai = robotvrai;
		this.robotchrono = new RobotChrono(config, log);
		this.memorymanager = memorymanager;
	}
	
	@Override
	public void run()
	{
		int profondeur_max = 2;
		while(!stop_threads)
		{
			log.debug("Lancement du thread de stratégie", this);
			strategie.analyse_ennemi();
			robotchrono.majRobotChrono(robotvrai);
			// Evaluation d'une stratégie de secours si ce script bug (en premier car plus urgent)
			Table tableBlocage = table;
			Vec2 centre_detection = new Vec2((float)(400 * Math.cos(robotvrai.getOrientation())), (float)(400 * Math.sin(robotvrai.getOrientation())));
			centre_detection.Plus(robotvrai.getPosition());
			tableBlocage.creer_obstacle(centre_detection);
			memorymanager.setModelTable(tableBlocage, profondeur_max);
			memorymanager.setModelRobotChrono(robotchrono, profondeur_max);
			NoteScriptVersion meilleurErreur = new NoteScriptVersion();
			try {
				meilleurErreur = strategie.evaluation(profondeur_max, 0);
			} catch (ScriptException e) {
				log.critical(e, this);
			}

			strategie.prochainScriptEnnemi = meilleurErreur.script;
			
			// Evaluation du prochain coup en supposant que celui-ci se passe sans problème

			if(strategie.scriptEnCours == null)
			{
				synchronized(strategie.scriptEnCours)
				{
//					futureTable = strategie.scriptEnCours.futureTable(table, strategie.versionScriptEnCours);
//					futurRobotChrono = strategie.scriptEnCours.futurRobotChrono(robotchrono, strategie.versionScriptEnCours);
				}
	
				NoteScriptVersion meilleurProchain = new NoteScriptVersion();
				try {
					meilleurProchain = strategie.evaluation(profondeur_max, 0);
				} catch (ScriptException e) {
					log.critical(e, this);
				}
				strategie.prochainScript = meilleurProchain.script;
			}
			
		}
	}
	
}

package threads;

import exception.ScriptException;
import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.MemoryManager;
import strategie.NoteScriptVersion;
import strategie.Strategie;
import table.Table;
import utils.Sleep;

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
	private ThreadTimer threadtimer;
	
	private int profondeur_max;

	ThreadStrategie(Strategie strategie, Table table, RobotVrai robotvrai, MemoryManager memorymanager, ThreadTimer threadtimer)
	{
		this.strategie = strategie;
		this.table = table;
		this.robotvrai = robotvrai;
		this.robotchrono = new RobotChrono(config, log);
		this.memorymanager = memorymanager;
		this.threadtimer = threadtimer;
		try {
			profondeur_max = Integer.parseInt(config.get("profondeur_max_arbre")) - 1;
		}
		catch(Exception e)
		{
			profondeur_max = 9;
			log.critical(e, this);
		}
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de stratégie", this);

		while(!threadtimer.match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Stoppage du thread de stratégie", this);
				return;
			}
			Sleep.sleep(200);
		}
		
		while(!stop_threads)
		{
			strategie.analyse_ennemi();

			// Evaluation d'une stratégie de secours si ce script bug
			maj_prochainScriptErreur();
			
			if(strategie.besoin_ProchainScript())
			{
				// Evaluation du prochain coup en supposant que celui-ci se passe sans problème
				maj_prochainScript();
			}
			
			Sleep.sleep(50);
			
		}
	}

	private void maj_prochainScriptErreur()
	{
		robotchrono.majRobotChrono(robotvrai);
		Table tableBlocage = table.clone();
		Vec2 centre_detection = new Vec2((float)(400 * Math.cos(robotvrai.getOrientation())), (float)(400 * Math.sin(robotvrai.getOrientation())));
		centre_detection.Plus(robotvrai.getPosition());
		tableBlocage.creer_obstacle(centre_detection);
		memorymanager.setModelTable(tableBlocage, profondeur_max);
		memorymanager.setModelRobotChrono(robotchrono, profondeur_max);
		NoteScriptVersion meilleurErreur = new NoteScriptVersion();
		try {
			meilleurErreur = strategie.evaluation(profondeur_max, 0);
		} catch (ScriptException e) {
			e.printStackTrace();
			log.critical(e, this);
		}

		strategie.setProchainScriptEnnemi(meilleurErreur);		
	}

	private void maj_prochainScript()
	{
		robotchrono.majRobotChrono(robotvrai);
		Table tableFuture = table.clone();
		NoteScriptVersion enCours = strategie.getScriptEnCours();
		// TODO: la durée est importante pour supprimer les obstacles périmés
		// La mise en cache est systématique car on ne cherche pas à récupérer le décompte mais seulement à modifier l'état de robotchrono et de table
		enCours.script.calcule(enCours.version, robotchrono, tableFuture, true);
		memorymanager.setModelTable(tableFuture, profondeur_max);
		memorymanager.setModelRobotChrono(robotchrono, profondeur_max);
		NoteScriptVersion meilleur = new NoteScriptVersion();
		try {
			meilleur = strategie.evaluation(profondeur_max, 0);
		} catch (ScriptException e) {
			e.printStackTrace();
			log.critical(e, this);
		}

		strategie.setProchainScript(meilleur);		
	}

}

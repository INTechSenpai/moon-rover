package threads;

import exception.ScriptException;
import robot.RobotChrono;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.MemoryManager;
import strategie.NoteScriptMetaversion;
import strategie.NoteScriptVersion;
import pathfinding.Pathfinding;
import strategie.Strategie;
import table.Table;
import utils.Sleep;



/**
 * Thread qui calculera en continu la stratégie à adopter
 * @author pf, Krissprolls
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
	private ThreadTimer threadtimer;
	
	private int profondeur_max;

	ThreadStrategie(Strategie strategie, Table table, RobotVrai robotvrai, MemoryManager memorymanager, ThreadTimer threadtimer, Pathfinding pathfinding)
	{
		this.strategie = strategie;
		this.table = table;
		this.robotvrai = robotvrai;
		this.robotchrono = new RobotChrono(config, log);
		this.memorymanager = memorymanager;
		this.pathfinding = pathfinding;
		this.threadtimer = threadtimer;
		maj_config();
		Thread.currentThread().setPriority(5);
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de stratégie", this);

		// attends que le match démarre
		while(!threadtimer.match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Stoppage du thread de stratégie", this);
				return;
			}
			
			// vérifie tout les dixièmes de seconde si le match a démarré
			Sleep.sleep(100);
		}
		
		// boucle principale de stratégie 
		while(!stop_threads)
		{
			// Evaluation d'une stratégie de secours si ce script bug
			if(evalueEnnemi())
				maj_prochainScriptErreur();
			
			// Evaluation du prochain coup en supposant que celui-ci se passe sans problème
			maj_prochainScript();
			
			Sleep.sleep(50);
			
		}
	}

	
	private void maj_prochainScriptErreur()
	{
		robotchrono.majRobotChrono(robotvrai);
		Table tableBlocage = table.clone();
		Vec2 centre_detection = new Vec2((int)(400 * Math.cos(robotvrai.getOrientation())), (int)(400 * Math.sin(robotvrai.getOrientation())));
		centre_detection.Plus(robotvrai.getPosition());
		tableBlocage.creer_obstacle(centre_detection);
		memorymanager.setModelTable(tableBlocage, profondeur_max);
		memorymanager.setModelRobotChrono(robotchrono, profondeur_max);
		NoteScriptMetaversion meilleurErreur = new NoteScriptMetaversion();
		try {
			meilleurErreur = strategie.evaluation(profondeur_max, 0);
		} catch (ScriptException e) {
			e.printStackTrace();
			log.critical(e, this);
		}

		float[] a = strategie.meilleurVersion(meilleurErreur.metaversion, meilleurErreur.script, robotchrono, tableBlocage, pathfinding);
		NoteScriptVersion meilleur_version = new NoteScriptVersion();
		meilleur_version.script = meilleurErreur.script;
		meilleur_version.version = (int)a[0];
		meilleur_version.note = a[1];
		strategie.setProchainScriptEnnemi(meilleur_version);		

	}

	private void maj_prochainScript()
	{
		robotchrono.majRobotChrono(robotvrai);
		Table tableFuture = table.clone();
//		NoteScriptVersion enCours = strategie.getScriptEnCours();
		// TODO: la durée est importante pour supprimer les obstacles périmés
		// La mise en cache est systématique car on ne cherche pas à récupérer le décompte mais seulement à modifier l'état de robotchrono et de table
//		enCours.script.calcule(enCours.version, robotchrono, tableFuture, true);
		memorymanager.setModelTable(tableFuture, profondeur_max);
		memorymanager.setModelRobotChrono(robotchrono, profondeur_max);
		NoteScriptMetaversion meilleur = new NoteScriptMetaversion();
		try {
			meilleur = strategie.evaluation(profondeur_max, 0);
		} catch (ScriptException e) {
			e.printStackTrace();
			log.critical(e, this);
		}
		float[] a = strategie.meilleurVersion(meilleur.metaversion, meilleur.script, robotchrono, tableFuture, pathfinding);
		NoteScriptVersion meilleur_version = new NoteScriptVersion();
		meilleur_version.script = meilleur.script;
		meilleur_version.version = (int)a[0];
		meilleur_version.note = a[1];
		strategie.setProchainScript(meilleur_version);		
	}

	private boolean evalueEnnemi()
	{
		// TODO
		return true;
	}
	
	public void maj_config()
	{
		try {
			profondeur_max = Integer.parseInt(config.get("profondeur_max_arbre")) - 1;
		}
		catch(Exception e)
		{
			profondeur_max = 9;
			e.printStackTrace();
		}
	}
}

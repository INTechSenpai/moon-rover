package threads;

import java.util.ArrayList;

import exceptions.strategie.PathfindingException;
import robot.RobotVrai;
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
	private MemoryManager memorymanager;

	ThreadStrategie(Strategie strategie, Table table, RobotVrai robotvrai, MemoryManager memorymanager, Pathfinding pathfinding)
	{
		this.strategie = strategie;
		this.memorymanager = memorymanager;
		maj_config();
		Thread.currentThread().setPriority(5);
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de stratégie", this);

		// attends que le match démarre
		while(!ThreadTimer.match_demarre)
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
			//TODO
			// Evaluation d'une stratégie de secours si ce script bug
	//		if(evalueEnnemi())
	//			maj_prochainScriptErreur();

			// Evaluation du prochain coup en supposant que celui-ci se passe sans problème
			maj_prochainScript();
			
			Sleep.sleep(50);
			
		}
	}

	
	
	private void maj_prochainScript()
	{

		NoteScriptMetaversion meilleur = new NoteScriptMetaversion();
		
		ArrayList<NoteScriptMetaversion> errorList = new ArrayList<NoteScriptMetaversion>();
		float[] a = null;
		while(a == null)
		{
			meilleur = strategie.evaluate(errorList);
			try
			{
				a = strategie.meilleurVersion(meilleur.metaversion, meilleur.script, memorymanager.getClone(0));
			}
			catch(PathfindingException e)
			{

				//log.debug("La branche " + meilleur + " renvoyée par l'arbre n'a pas de metaversion acessible, on relace l'arbre sans cette branche",this);
				errorList.add(meilleur);
				//e.printStackTrace();
			}
		}
		NoteScriptVersion meilleur_version = new NoteScriptVersion();
		meilleur_version.script = meilleur.script;
		meilleur_version.version = (int)a[0];
		meilleur_version.note = a[1];
		strategie.setProchainScript(meilleur_version);
		//log.debug("prochain script : " + meilleur_version,this);
	}
	
	public void maj_config()
	{
	}
}

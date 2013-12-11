package threads;

import robot.cartes.Capteur;
import robot.cartes.Deplacements;
import table.Table;
import container.Service;

/**
 * Thread qui s'occupe de la gestion du temps: début du match, péremption des obstacles
 * @author pf
 *
 */

public class ThreadTimer extends AbstractThread {

	// Dépendance
	private Table table;
	private Capteur capteur;
	private Deplacements deplacements;
	
	public boolean match_demarre = false;
	public boolean fin_match = false;
	public long date_debut;
	public long duree_match;
	
	ThreadTimer(Service config, Service log, Service table, Service capteur, Service deplacements)
	{
		super(config, log);
		this.table = (Table) table;
		this.capteur = (Capteur) capteur;
		this.deplacements = (Deplacements) deplacements;
		
		// facteur 1000 car temps_match est en secondes et duree_match en ms
		duree_match = 1000*Long.parseLong(this.config.config.getProperty("temps_match"));

	}
	public void run()
	{
		// Attente du démarrage du match
		while(!capteur.demarrage_match())
		{
			if(stop_threads)
			{
				log.debug("Arrêt du thread timer", this);
				return;
			}
			try {
				Thread.sleep(200);
			}
			catch(Exception e)
			{
				log.warning(e.toString(), this);
			}
		}
		log.debug("LE MATCH COMMENCE !", this);

		date_debut = System.currentTimeMillis();
		match_demarre = true;

		// Le match à démarré. Tous les 500ms, on retire les obstacles périmés
		while(System.currentTimeMillis() - date_debut < duree_match)
		{
			if(stop_threads)
			{
				log.debug("Arrêt du thread timer", this);
				return;
			}
			table.supprimer_obstacles_perimes();
			
			try {
				Thread.sleep(500);
			}
			catch(Exception e)
			{
				log.warning(e.toString(), this);
			}
		}
		
		// Le match est fini, désasservissement
		fin_match = true;

		deplacements.stopper();
		
		try {
			Thread.sleep(500);
		}
		catch(Exception e)
		{
			log.warning(e.toString(), this);
		}
		
		deplacements.desactiver_asservissement_rotation();
		deplacements.desactiver_asservissement_translation();
		deplacements.arret_final();

		log.debug("Fin du thread timer", this);
		
	}

	// TODO
	public boolean get_fin_match()
	{
		return false;
	}
	
}

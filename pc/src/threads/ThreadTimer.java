package threads;

import exception.SerialException;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import table.Table;
import utils.Sleep;

/**
 * Thread qui s'occupe de la gestion du temps: début du match, péremption des obstacles
 * C'est lui qui active les capteurs en début de match.
 * @author pf
 *
 */

public class ThreadTimer extends AbstractThread {

	// Dépendance
	private Table table;
	private Capteurs capteur;
	private Deplacements deplacements;
	
	public boolean match_demarre = false;
	public boolean fin_match = false;
	public long date_debut;
	public long duree_match = 90000;
	
	ThreadTimer(Table table, Capteurs capteur, Deplacements deplacements)
	{
		this.table = table;
		this.capteur = capteur;
		this.deplacements = deplacements;
		
		// facteur 1000 car temps_match est en secondes et duree_match en ms
		try {
			duree_match = 1000*Long.parseLong(config.get("temps_match"));
		}
		catch(Exception e)
		{
			log.warning(e, this);
		}

	}

	@Override
	public void run()
	{
		config.set("capteurs_on", false);
		capteur.maj_config();
		log.debug("Lancement du thread timer", this);
		// Attente du démarrage du match
		while(!capteur.demarrage_match() && !match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Arrêt du thread timer avant le début du match", this);
				return;
			}
			Sleep.sleep(50);
		}
		date_debut = System.currentTimeMillis();
		match_demarre = true;

		config.set("capteurs_on", true);
		capteur.maj_config();

		log.debug("LE MATCH COMMENCE !", this);


		// Le match à démarré. Tous les 500ms, on retire les obstacles périmés
		while(System.currentTimeMillis() - date_debut < duree_match)
		{
			if(stop_threads)
			{
				log.debug("Arrêt du thread timer avant la fin du match", this);
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

		try {
			deplacements.stopper();
		} catch (SerialException e) {
			e.printStackTrace();
		}
		
		Sleep.sleep(500);
		
		try {
			deplacements.desactiver_asservissement_rotation();
			deplacements.desactiver_asservissement_translation();
		} catch (SerialException e) {
			e.printStackTrace();
		}
		deplacements.arret_final();

		log.debug("Fin du thread timer", this);
		
	}
	
	public void maj_config()
	{
		// TODO
	}
	
}

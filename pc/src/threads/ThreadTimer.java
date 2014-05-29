package threads;

import exceptions.serial.SerialException;
import robot.cartes.Actionneurs;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import smartMath.Vec2;
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
	private Actionneurs actionneurs;
	
	public static boolean match_demarre = false;
	public static boolean fin_match = false;
	public static long date_debut;
	public static long duree_match = 90000;
	public static long temps_reserve_funny_action = 1000;
		
	ThreadTimer(Table table, Capteurs capteur, Deplacements deplacements, Actionneurs actionneurs)
	{
		this.table = table;
		this.capteur = capteur;
		this.deplacements = deplacements;
		this.actionneurs = actionneurs;
		
		maj_config();
		Thread.currentThread().setPriority(1);
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
		while(System.currentTimeMillis() - date_debut < duree_match - temps_reserve_funny_action)
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
		
		try
        {
			// on s'oriente pour tirer le fillet
            double[] infos = deplacements.get_infos_x_y_orientation();
            Vec2 position = new Vec2((int)infos[0], (int)infos[1]);
            Vec2 positionMammouth1 = new Vec2(-750, 2000);
            Vec2 positionMammouth2 = new Vec2(750, 2000);            
            double angle;
            if(position.SquaredDistance(positionMammouth1) < position.SquaredDistance(positionMammouth2))
                angle = Math.atan2(positionMammouth1.y - position.y, positionMammouth1.x - position.x);
            else
                angle = Math.atan2(positionMammouth2.y - position.y, positionMammouth2.x - position.x);
            deplacements.stopper();
            deplacements.tourner(angle-Math.PI/2); // le filet est sur le coté gauche
            
            // fin du match : désasser final
            try 
            {
                deplacements.desactiver_asservissement_rotation();
                deplacements.desactiver_asservissement_translation();
            } catch (SerialException e) {
                e.printStackTrace();
            }
            deplacements.arret_final();
            
            // tir de filet!
            Sleep.sleep(1500+temps_reserve_funny_action);
            actionneurs.lancerFilet();
            
        } catch (SerialException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		
		Sleep.sleep(500);
		
		
		
		log.debug("Fin du thread timer", this);
		
	}
	
	public long temps_restant()
	{
		return date_debut + duree_match - System.currentTimeMillis();
	}
	
	public void maj_config()
	{
		// facteur 1000 car temps_match est en secondes et duree_match en ms
		try {
			duree_match = 1000*Long.parseLong(config.get("temps_match"));
		}
		catch(Exception e)
		{
			log.warning(e, this);
		}
        try {
            temps_reserve_funny_action = 1000*Long.parseLong(config.get("temps_reserve_funny_action"));
        }
        catch(Exception e)
        {
            log.warning(e, this);
        }
	}
	
}

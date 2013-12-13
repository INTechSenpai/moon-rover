package threads;

import robot.RobotVrai;
import robot.cartes.Capteur;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Thread qui ajoute en continu les obstacles détectés par les capteurs
 * @author pf
 *
 */

class ThreadCapteurs extends AbstractThread {

	private RobotVrai robotvrai;
	private Capteur capteur;
	private Table table;
	private ThreadTimer threadTimer;
	
	// Valeurs par défaut s'il y a un problème de config
	private double tempo = 0;
	private int horizon_capteurs = 700;
	private int rayon_robot_adverse = 230;
	private int largeur_robot = 300;
	private int table_x = 3000;
	private int table_y = 2000;
	private int capteurs_frequence = 5;
	
	ThreadCapteurs(Read_Ini config, Log log, RobotVrai robotvrai, ThreadTimer threadTimer, Table table, Capteur capteur)
	{
		super(config, log);
		this.robotvrai = robotvrai;
		this.threadTimer = threadTimer;
		this.table = table;
		this.capteur = capteur;
	}
	
	public void run()
	{
		int date_dernier_ajout = 0;
		boolean marche_arriere = false;
		try
		{
			tempo = Double.parseDouble(config.config.getProperty("capteurs_temporisation_obstacles"));
			horizon_capteurs = Integer.parseInt(config.config.getProperty("horizon_capteurs"));
			rayon_robot_adverse = Integer.parseInt(config.config.getProperty("rayon_robot_adverse"));
			largeur_robot = Integer.parseInt(config.config.getProperty("largeur_robot"));
			table_x = Integer.parseInt(config.config.getProperty("table_x"));
			table_y = Integer.parseInt(config.config.getProperty("table_y"));
			capteurs_frequence = Integer.parseInt(config.config.getProperty("capteurs_frequence"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
		
		log.debug("Lancement du thread de capteurs", this);
	
		while(!threadTimer.match_demarre)
			if(stop_threads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}
		
		log.debug("Activation des capteurs", this);
		while(!threadTimer.fin_match)
		{
			if(stop_threads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}
			
			marche_arriere = !marche_arriere;

			int distance = capteur.mesurer(marche_arriere);
			if(distance >= 0 && distance < horizon_capteurs)
			{
				int distance_inter_robots = distance + rayon_robot_adverse + largeur_robot/2;
				double theta = robotvrai.getOrientation();
				if(marche_arriere)
					theta += Math.PI;
				Vec2 position = robotvrai.getPosition().Plus(new Vec2((float)distance_inter_robots * (float)Math.cos(theta), (float)distance_inter_robots * (float)Math.sin(theta)));

				// on vérifie qu'un obstacle n'a pas été ajouté récemment
				if(System.currentTimeMillis() - date_dernier_ajout > tempo)
					// si la position est bien sur la table (histoire de pas détecter un arbitre)
					if(position.x > -table_x/2 && position.y > 0 && position.x < table_x/2 && position.y < table_y)
						table.creer_obstacle(position);
						date_dernier_ajout = (int)System.currentTimeMillis();
				
			}
			try
			{
			Thread.sleep((long)1/capteurs_frequence);
			}
			catch(Exception e)
			{
				log.critical("Erreur sleep: "+e.toString(), this);
			}
			
		}
        log.debug("Fin du thread de capteurs", this);
		
	}
	
}

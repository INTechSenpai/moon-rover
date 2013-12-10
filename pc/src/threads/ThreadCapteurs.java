package threads;

import robot.RobotVrai;
import robot.cartes.Capteur;
import smartMath.Vec2;
import table.Table;
import container.Service;

public class ThreadCapteurs extends AbstractThread {

	RobotVrai robotvrai;
	Capteur capteur;
	Table table;
	ThreadTimer threadTimer;
	
	ThreadCapteurs(Service config, Service log, Service robotvrai, Service threadTimer, Service table, Service capteur)
	{
		super(config, log);
		this.robotvrai = (RobotVrai) robotvrai;
		this.threadTimer = (ThreadTimer) threadTimer;
		this.table = (Table) table;
		this.capteur = (Capteur) capteur;
	}
	
	public void run()
	{
		int date_dernier_ajout = 0;
		boolean marche_arriere = false;
		double tempo = Double.parseDouble(config.config.getProperty("capteurs_temporisation_obstacles"));
		int horizon_capteurs = Integer.parseInt(config.config.getProperty("horizon_capteurs"));
		int rayon_robot_adverse = Integer.parseInt(config.config.getProperty("rayon_robot_adverse"));
		int largeur_robot = Integer.parseInt(config.config.getProperty("largeur_robot"));
		int table_x = Integer.parseInt(config.config.getProperty("table_x"));
		int table_y = Integer.parseInt(config.config.getProperty("table_y"));
		int capteurs_frequence = Integer.parseInt(config.config.getProperty("capteurs_frequence"));
		
		log.debug("Lancement du thread de capteurs", this);
	
		while(!threadTimer.match_demarre)
			if(stop_threads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}
		
		log.debug("Activation des capteurs", this);
		while(!threadTimer.get_fin_match())
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

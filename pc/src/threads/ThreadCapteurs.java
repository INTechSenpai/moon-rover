package threads;

import pathfinding.Pathfinding;
import robot.RobotVrai;
import robot.cartes.Capteurs;
import smartMath.Vec2;
import table.Table;

/**
 * Thread qui ajoute en continu les obstacles détectés par les capteurs
 * @author pf
 *
 */

class ThreadCapteurs extends AbstractThread {

	private RobotVrai robotvrai;
	private Capteurs capteur;
	private Table table;
	private Pathfinding pathfinding;
	private ThreadTimer threadTimer;
	
	// Valeurs par défaut s'il y a un problème de config
	private double tempo = 0;
	private int horizon_capteurs = 700;
	private int rayon_robot_adverse = 230;
	private int largeur_robot = 300;
	private int table_x = 3000;
	private int table_y = 2000;
	private int capteurs_frequence = 5;
	
	ThreadCapteurs(RobotVrai robotvrai, Pathfinding pathfinding, ThreadTimer threadTimer, Table table, Capteurs capteur)
	{
		super(config, log);
		this.robotvrai = robotvrai;
		this.pathfinding = pathfinding;
		this.threadTimer = threadTimer;
		this.table = table;
		this.capteur = capteur;
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de capteurs", this);
		int date_dernier_ajout = 0;
//		boolean marche_arriere = false;
		try
		{
			tempo = Double.parseDouble(config.get("capteurs_temporisation_obstacles"));
			horizon_capteurs = Integer.parseInt(config.get("horizon_capteurs"));
			rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
			largeur_robot = Integer.parseInt(config.get("largeur_robot"));
			table_x = Integer.parseInt(config.get("table_x"));
			table_y = Integer.parseInt(config.get("table_y"));
			capteurs_frequence = Integer.parseInt(config.get("capteurs_frequence"));
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
			
//			marche_arriere = !marche_arriere;

			int distance = capteur.mesurer(false);
			if(distance >= 0 && distance < horizon_capteurs)
			{
				int distance_inter_robots = distance + rayon_robot_adverse + largeur_robot/2;
				double theta = robotvrai.getOrientation();
//				if(marche_arriere)
//					theta += Math.PI;
				Vec2 position = robotvrai.getPosition().PlusNewVector(new Vec2((float)distance_inter_robots * (float)Math.cos(theta), (float)distance_inter_robots * (float)Math.sin(theta)));

				// on vérifie qu'un obstacle n'a pas été ajouté récemment
				if(System.currentTimeMillis() - date_dernier_ajout > tempo)
					// si la position est bien sur la table (histoire de pas détecter un arbitre)
					if(position.x > -table_x/2 && position.y > 0 && position.x < table_x/2 && position.y < table_y)
						table.creer_obstacle(position);
						date_dernier_ajout = (int)System.currentTimeMillis();
				
				pathfinding.update();
			}
			sleep((long)1/capteurs_frequence);
			
		}
        log.debug("Fin du thread de capteurs", this);
		
	}
	
}

package threads;

import robot.RobotVrai;
import robot.cartes.Capteurs;
import smartMath.Vec2;
import table.Table;
import utils.Sleep;

/**
 * Thread qui ajoute en continu les obstacles détectés par les capteurs
 * @author pf, Krissprolls
 *
 */

class ThreadCapteurs extends AbstractThread {

	private RobotVrai robotvrai;
	private Capteurs capteur;
	private Table table;
	
	// Valeurs par défaut s'il y a un problème de config
	private double tempo = 0;
	private int horizon_capteurs = 700;
	private int rayon_robot_adverse = 230;
	private int largeur_robot = 300;
	private int table_x = 3000;
	private int table_y = 2000;
	private int capteurs_frequence = 5;
	
	ThreadCapteurs(RobotVrai robotvrai, Table table, Capteurs capteur)
	{
		super(config, log);
		this.robotvrai = robotvrai;
		this.table = table;
		this.capteur = capteur;
		Thread.currentThread().setPriority(2);
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de capteurs", this);
		int date_dernier_ajout = 0;
//		boolean marche_arriere = false;
		maj_config();
		
		while(!ThreadTimer.match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}
			Sleep.sleep(50);
		}
		
		log.debug("Activation des capteurs", this);
		while(!ThreadTimer.fin_match)
		{
			if(stop_threads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}

			int distance = capteur.mesurer();
			log.debug("US: "+distance, this);
			if (distance > 0 && distance < 70)
				log.debug("Câlin !", this);
			
			if(distance >= 40 && distance < horizon_capteurs)
			{
				int distance_inter_robots = distance + rayon_robot_adverse + largeur_robot/2;
				int distance_brute = distance + largeur_robot/2;
				double theta = robotvrai.getOrientation();
//				if(marche_arriere)
//					theta += Math.PI;
				// On ne prend pas en compte le rayon du robot adverse dans la position brute. Il s'agit en fait du point effectivement vu
				// Utilisé pour voir si l'obstacle n'est justement pas un robot adverse.
				Vec2 position_brute = robotvrai.getPosition().PlusNewVector(new Vec2((int)((float)distance_brute * (float)Math.cos(theta)), (int)((float)distance_brute * (float)Math.sin(theta)))); // position du point détecté
				Vec2 position = robotvrai.getPosition().PlusNewVector(new Vec2((int)(distance_inter_robots * Math.cos(theta)), (int)((float)distance_inter_robots * (float)Math.sin(theta)))); // centre supposé de l'obstacle détecté

				// si la position est bien sur la table (histoire de pas détecter un arbitre)
				if(position.x-200 > -table_x/2 && position.y > 200 && position.x+200 < table_x/2 && position.y+200 < table_y)
					// on vérifie qu'un obstacle n'a pas été ajouté récemment
					if(System.currentTimeMillis() - date_dernier_ajout > tempo)
					{
						if(!table.obstacle_existe(position_brute))
						{
							table.creer_obstacle(position);
							date_dernier_ajout = (int)System.currentTimeMillis();
							log.debug("Nouvel obstacle en "+position, this);
						}
						else	
						    log.debug("L'objet vu est un obstacle fixe.", this);
					}

			}
			
			Sleep.sleep((long)(1000./capteurs_frequence));
			
		}
        log.debug("Fin du thread de capteurs", this);
		
	}
	
	public void maj_config()
	{
			tempo = Double.parseDouble(config.get("capteurs_temporisation_obstacles"));
			horizon_capteurs = Integer.parseInt(config.get("horizon_capteurs"));
			rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
			largeur_robot = Integer.parseInt(config.get("largeur_robot"));
			table_x = Integer.parseInt(config.get("table_x"));
			table_y = Integer.parseInt(config.get("table_y"));
			capteurs_frequence = Integer.parseInt(config.get("capteurs_frequence"));
	}
	
}

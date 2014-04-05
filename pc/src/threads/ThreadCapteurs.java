package threads;

import pathfinding.Pathfinding;
import robot.RobotVrai;
import robot.cartes.Capteurs;
import smartMath.Vec2;
import table.Table;
import table.Tree;
import table.Torch;
import table.Fire;
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
		
		while(!threadTimer.match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Stoppage du thread capteurs", this);
				return;
			}
			Sleep.sleep(50);
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



			//int distance = capteur.mesurer(false);
			int distance_infrarouge = capteur.mesurer_infrarouge();
			int distance_ultrason = capteur.mesurer_ultrason();
			
			//Ici on interprètera distance_infrarouge
			boolean obs_infr = (distance_infrarouge < 500); //Booléen car le capteur infrarouge n'est pas assez fiable pour qu'on puisse se servir des distances
			if(obs_infr == false && distance_ultrason >= 0 && distance_ultrason <horizon_capteurs)
				
				//La condition signifie qu'on vioit des trucs en haut mais pas en bas
			{
				float obsX,obsY;//position de l'obstacle détecté
				//on ne détecte qu'en haut. Il faut vérifier si ce qu'on détecte est un arbre (en regardant la position et l'orientation du robot). Si
				//oui, on n'en tient pas compte, si non, c'est un obstacle.
				obsX = (float)(robotvrai.getPosition().x + Math.cos(robotvrai.getOrientation()));
				obsY = (float)(robotvrai.getPosition().y + Math.sin(robotvrai.getOrientation()));
				Vec2 pos  = new Vec2(obsX,obsY);
				//Il faudrait modifier table pour qu'on ait accès aux positions des arbres.
				//150 est le rayon des arbres
				Tree[] lArbres = table.getListTree();
				int j = 0 ;//C'est un compteur qui s'incrémente quand c'est pas un arbre
				//On regarde si là où le robot a détecté un obstacle, il y a un arbre.
				for(int i = 0; i<lArbres.length; i++)
				{
					if (lArbres[i].getPosition().SquaredDistance(pos) > 50*150)
					{
						j = j+1;
					}
					else
					{
						log.debug("On a detecte un arbre en "+pos, this);
						break;
					}
				}
				if(j == lArbres.length) //C'est alors que le robot n'a détecté aucun arbre
				{
					//On a sans doute détecté le robot adverse
					table.creer_obstacle(pos);
					date_dernier_ajout = (int)System.currentTimeMillis();
					log.debug("Nouvel obstacle en "+pos, this);
				}
				
			}
			
			else if(obs_infr == true && distance_ultrason >= 0 && distance_ultrason <horizon_capteurs)
			{
				//On détecte un truc en face de nous qui est un obstacle de haut en bas
				int distance_inter_robots = distance_ultrason + rayon_robot_adverse + largeur_robot/2;
				double theta = robotvrai.getOrientation();
				Vec2 position = robotvrai.getPosition().PlusNewVector(new Vec2((float)distance_inter_robots * (float)Math.cos(theta), (float)distance_inter_robots * (float)Math.sin(theta)));
				//position du robot adverse détecté

				if(System.currentTimeMillis() - date_dernier_ajout > tempo)
					// si la position est bien sur la table (histoire de pas détecter un arbitre)
					if(position.x > -table_x/2 && position.y > 0 && position.x < table_x/2 && position.y < table_y)
					{
						table.creer_obstacle(position);
						date_dernier_ajout = (int)System.currentTimeMillis();
						log.debug("Nouvel obstacle en "+position, this);
					}
				
				pathfinding.update(table);
			}
			else if(obs_infr == true)
			{
				//on ne détecte qu'en bas, normalement ca veux dire qu'il y a un feu debout ou une torche, un foyer ou un mur.
				//On vérifie en fonction de la position si c'est un mur ou un foyer. Puis si non, on regarde si regartde vers la position initiale
				//d'un feu debout. Si oui, on considère que c'est un feu et on peux taper dedans. Si non, c'est une torche mobile, et on l'évite, en 
				//actualisant sa position. (En considérant que les torches ne quittent pas leur demie table par exemple)
				
				//Il faudra voir accès aux positions des obstacles sur la table, il faudra éviter les doublons de méthodes;
				//Vec2 pos = table. robotvrai.getPosition()robotvrai.getOrientation();
				// Il faut avoir accès aux positions des feux connus, des foyers, des torches et des murs
				//Il faut aussi déterminer le rayon d'action de l'infrarouge pour limiter la zone de recherche
				//Mais tout ça va prendre du temps et risque de d'être inadapté au final
				
				
				float obsX,obsY;//position de l'obstacle détecté
				//on ne détecte qu'en haut. Il faut vérifier si ce qu'on détecte est un arbre (en regardant la position et l'orientation du robot). Si
				//oui, on n'en tient pas compte, si non, c'est un obstacle.
				obsX = (float)(robotvrai.getPosition().x + Math.cos(robotvrai.getOrientation()));
				obsY = (float)(robotvrai.getPosition().y + Math.sin(robotvrai.getOrientation()));
				Vec2 pos  = new Vec2(obsX,obsY);
				int j = 0,k = 0, l = 0 ;
				Torch[] lTorch = table.getListTorch();
				Fire[] lFire = table.getListFire();
				//On regarde si là où le robot a détecté un obstacle, il y a un arbre.
				for(int i = 0; i<lTorch.length+lFire.length; i++)
				{
					if (lTorch[i].getPosition().SquaredDistance(pos) > 100*100)
					{
						j = j+1;//C'est qu'on n'a pas détecté de torche
					}
					else
					{
						log.debug("On a detecte une torche en "+pos, this);
						break;
					}
					if(lFire[i+lTorch.length].getPosition().SquaredDistance(pos) > 100*100)
					{
						k = k+1;//C'est qu'on n'a pas détecté de feu
					}
					else
					{
						log.debug("On a detecte une torche en "+pos, this);
						break;
					}
			
				}
				if(j+k == lFire.length+lTorch.length) //C'est alors que le robot n'a détecté aucun arbre
				{
					table.creer_obstacle(pos);
					date_dernier_ajout = (int)System.currentTimeMillis();
					log.debug("Nouvel obstacle en "+pos, this);
				}
				
			}
			
			/*
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
					{
						table.creer_obstacle(position);
						date_dernier_ajout = (int)System.currentTimeMillis();
						log.debug("Nouvel obstacle en "+position, this);
					}
				
				pathfinding.update();
			}*/
			Sleep.sleep((long)1/capteurs_frequence);
			
		}
        log.debug("Fin du thread de capteurs", this);
		
	}
	
}

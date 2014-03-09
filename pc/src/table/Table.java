package table;

import java.util.ArrayList;
import java.util.Iterator;

import robot.Orientation;
import smartMath.Vec2;
import container.Service;
import exception.ConfigException;
import utils.*;

public class Table implements Service {

	// On met cette variable en static afin que, dans deux instances dupliquées, elle ne redonne pas les mêmes nombres
	private static int indice = 1;

	private Tree arrayTree[] = new Tree[4];
	private Torch arrayTorch[] = new Torch[2];
	private Fire arrayFire[] = new Fire[10];

	private ArrayList<Obstacle> listObstacles = new ArrayList<Obstacle>();
	private static ArrayList<Obstacle> listObstaclesFixes = new ArrayList<Obstacle>();
	private ObstacleCirculaire[] robots_adverses = new ObstacleCirculaire[2];
	
	private int hashFire;
	private int hashTree;
	private int hashObstacles;
	
	private Fresco[] list_fresco_pos = new Fresco[3];
	private boolean[] list_fresco_hanged = new boolean[3];

	// Dépendances
	private Log log;
	private Read_Ini config;
	
	public Table(Log log, Read_Ini config)
	{
		this.log = log;
		this.config = config;
		
		initialise();
	}
	
	public void initialise()
	{
		// Initialisation des feux
		arrayFire[0] = new Fire(new Vec2(1485,1200), 0, 0, Orientation.XPLUS, Colour.YELLOW);
		arrayFire[1] = new Fire(new Vec2(1100,900), 1, 0, Orientation.YPLUS, Colour.YELLOW);
		arrayFire[2] = new Fire(new Vec2(600,1400), 2, 0, Orientation.XPLUS, Colour.YELLOW);
		arrayFire[3] = new Fire(new Vec2(600,400), 6, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[4] = new Fire(new Vec2(200,15), 7, 0, Orientation.YPLUS, Colour.YELLOW);
		arrayFire[5] = new Fire(new Vec2(-200,15), 8, 0, Orientation.YPLUS, Colour.RED);
		arrayFire[6] = new Fire(new Vec2(-600,1400), 9, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[7] = new Fire(new Vec2(-600,400), 13, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[8] = new Fire(new Vec2(-1100,900), 14, 0, Orientation.YPLUS, Colour.RED);
		arrayFire[9] = new Fire(new Vec2(-1485,1200), 15, 0, Orientation.XPLUS, Colour.YELLOW);

		// Initialisation des arbres
		arrayTree[0] = new Tree(new Vec2(1500,700));
		arrayTree[1] = new Tree(new Vec2(800,0));
		arrayTree[2] = new Tree(new Vec2(-800,0));
		arrayTree[3] = new Tree(new Vec2(-1500,700));

		// Initialisation des torches
		Fire feu0 = new Fire(new Vec2(600,900), 3, 1, Orientation.GROUND, Colour.YELLOW);
		Fire feu1 = new Fire(new Vec2(600,900), 4, 2, Orientation.GROUND, Colour.RED);
		Fire feu2 = new Fire(new Vec2(600,900), 5, 3, Orientation.GROUND, Colour.YELLOW);
		arrayTorch[0] = new Torch(new Vec2(600,900), feu0, feu1, feu2);

		Fire feu3 = new Fire(new Vec2(-600,900), 10, 1, Orientation.GROUND, Colour.RED);
		Fire feu4 = new Fire(new Vec2(-600,900), 11, 2, Orientation.GROUND, Colour.YELLOW);
		Fire feu5 = new Fire(new Vec2(-600,900), 12, 3, Orientation.GROUND, Colour.RED);
		arrayTorch[1] = new Torch(new Vec2(-600,900), feu3, feu4, feu5); 
		
		// Ajout des torches mobiles
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(600,900), 80));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(-600,900), 80));
		
		// Ajout des foyers
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(1500,0), 250));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(0,950), 150));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(-1500,0), 250));

		// Ajout bacs
		listObstaclesFixes.add(new ObstacleRectangulaire(new Vec2(400,1700), 700, 300));
		listObstaclesFixes.add(new ObstacleRectangulaire(new Vec2(-1100,1700), 700, 300));

		// Ajout des arbres
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(1500,700), 150));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(800,0), 150));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(-800,0), 150));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(-1500,700), 150));

		int rayon_robot_adverse = 230;
			try {
				rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
			} catch (NumberFormatException | ConfigException e) {
				e.printStackTrace();
			}
		robots_adverses[0] = new ObstacleCirculaire(new Vec2(0,0), rayon_robot_adverse);
		robots_adverses[1] = new ObstacleCirculaire(new Vec2(0,0), rayon_robot_adverse);
		
		hashFire = 0;
		hashTree = 0;
		hashObstacles = 0;
		
		//Gestion des fresques
		list_fresco_pos[0] = new Fresco(new Vec2(0,0));
		list_fresco_pos[1] = new Fresco(new Vec2(0,0));
		list_fresco_pos[2]= new Fresco(new Vec2(0,0));
		//false -> aucune fresque
		//true -> fresque accrochée
		list_fresco_hanged[0] = false;
		list_fresco_hanged[1] = false;
		list_fresco_hanged[2] = false;
		
	}
	
	/*
	 * Gestions des obstacles
	 */
	
	public void creer_obstacle(final Vec2 position)
	{
		Vec2 position_sauv = position.clone();
		int rayon_robot_adverse = 0;
		long duree = 0;
			try {
				rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
			} catch (NumberFormatException | ConfigException e) {
				e.printStackTrace();
			}
			try {
				duree = Integer.parseInt(config.get("duree_peremption_obstacles"));
			} catch (NumberFormatException | ConfigException e) {
				e.printStackTrace();
			}
		
		Obstacle obstacle = new ObstacleProximite(position_sauv, rayon_robot_adverse, System.currentTimeMillis()+duree);
		synchronized(listObstacles)
		{
			listObstacles.add(obstacle);
		}
		hashObstacles = indice++;
	}

	/**
	 * Appel fait lors de l'anticipation, supprime les obstacles périmés à une date future
	 * @param date
	 */
	public void supprimer_obstacles_perimes(long date)
	{
		Iterator<Obstacle> iterator = listObstacles.iterator();
		synchronized(listObstacles)
		{
			while ( iterator.hasNext() )
			{
			    Obstacle obstacle = iterator.next();
			    if (obstacle instanceof ObstacleProximite && ((ObstacleProximite) obstacle).death_date <= date)
			    {
			        iterator.remove();
					hashObstacles = indice++;
			    }
			}	
		}
	}
	
	/**
	 * Appel fait par le thread timer, supprime les obstacles périmés
	 */
	public void supprimer_obstacles_perimes()
	{
		supprimer_obstacles_perimes(System.currentTimeMillis());
	}

	/**
	 * Renvoie si un obstacle est à une distance inférieur à "distance" du point "centre_detection"
	 * @param centre_detection
	 * @param distance
	 * @return
	 */
	public boolean obstaclePresent(final Vec2 centre_detection, int distance)
	{
		Iterator<Obstacle> iterator = listObstacles.iterator();
		while ( iterator.hasNext() )
		{
		    Obstacle obstacle = iterator.next();
		    if (obstacle.position.SquaredDistance(centre_detection) < distance*distance)
		    	return true;
		}	
		iterator = listObstaclesFixes.iterator();
		while ( iterator.hasNext() )
		{
		    Obstacle obstacle = iterator.next();
		    if (obstacle.position.SquaredDistance(centre_detection) < distance*distance)
		    	return true;
		}
		
		return robots_adverses[0].position.SquaredDistance(centre_detection) < distance*distance
				|| robots_adverses[1].position.SquaredDistance(centre_detection) < distance*distance;
	}	

	/**
	 * Utilisé par le thread de laser
	 * @param i
	 * @param position
	 */
    public void deplacer_robot_adverse(int i, Vec2 position)
    {
    	robots_adverses[i].position = position.clone();
    }
	
    /**
     * Utilisé par le thread de stratégie
     * @return
     */
    public Vec2[] get_positions_ennemis()
    {
    	Vec2[] positions =  new Vec2[2];
    	positions[0] = robots_adverses[0].position.clone();
    	positions[1] = robots_adverses[1].position.clone();
    	return positions;
    }
    
	// Feux
	
	public void pickFire (int id)
	{
		arrayFire[id].pickFire();
		hashFire = indice++;
	}

	public int nearestFire (Vec2 position)
	{
		int min = 0;
		for (int i = 1; i < 10; i++)
			if (arrayFire[i].getPosition().SquaredDistance(position) < arrayFire[min].getPosition().SquaredDistance(position))
				min = i;
		return min;
	}
	
	public void putFire (int id)
	{
		arrayFire[id].ejectFire();
		hashFire = indice++;
	}
	
	public float distanceFire(Vec2 position, int i)
	{
		return position.distance(arrayFire[i].position);
	}
	
	// Arbres
	
	public int nearestTree (Vec2 position)
	{
		int min = 0;
		for (int i = 0; i < 4; i++)
			if (arrayTree[i].getPosition().SquaredDistance(position) < arrayTree[min].getPosition().SquaredDistance(position))
				min = i;
		return min;
	}
	
	public float distanceTree(Vec2 position, int i)
	{
		return position.distance(arrayTree[i].position);
	}

	public void pickTree (int id)
	{
		arrayTree[id].setTaken();
		hashTree = indice++;
	}
	
	public int nbrLeftTree(int id)
	{
		return arrayTree[id].nbrLeft();
	}
	
	public int nbrRightTree(int id)
	{
		return arrayTree[id].nbrRight();
	}
	
	public int nbrTotalTree(int tree_id)
	{
		return arrayTree[tree_id].nbrTotal();
	}
	
	public boolean isTreeTaken(int tree_id)
	{
		return arrayTree[tree_id].isTaken();
	}
	
	//Torches
	
	public int nearestTorch (Vec2 position)
	{
		if(arrayTorch[0].getPosition().SquaredDistance(position) < arrayTorch[1].getPosition().SquaredDistance(position))
			return 0;
		else
			return 1;
	}

	public float distanceTorch(Vec2 position, int i)
	{
		return position.distance(arrayTorch[i].position);
	}

	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void clone(Table ct)
	{
		if(!equals(ct))
		{
			if(ct.hashFire != hashFire)
			{
				for(int i = 0; i < 10; i++)
					arrayFire[i].clone(ct.arrayFire[i]);
				ct.hashFire = hashFire;
			}
	
			if(ct.hashTree != hashTree)
			{
				for(int i = 0; i < 4; i++)		
					arrayTree[i].clone(ct.arrayTree[i]);
				ct.hashTree = hashTree;
			}
	
			if(ct.hashObstacles != hashObstacles)
			{
				ct.listObstacles.clear();
				for(Obstacle item: listObstacles)
					ct.listObstacles.add(item.clone());
				ct.hashObstacles = hashObstacles;
			}
		}
	}
	
	public Table clone()
	{
		Table cloned_table = new Table(log, config);
		clone(cloned_table);
		return cloned_table;
	}

	/**
	 * Utilisé par les tests unitaires uniquement. Vérifie que les hash sont bien mis à jour
	 * @return
	 */
	public int hashTable()
	{
		return hashFire + hashTree + hashObstacles;
	}

	/**
	 * Utilisé pour les tests
	 * @param other
	 * @return
	 */
	public boolean equals(Table other)
	{
		return 	hashFire == other.hashFire
				&& hashTree == other.hashTree
				&& hashObstacles == other.hashObstacles;
	}
	
	/**
	 * Utilisé pour les tests
	 * @return le nombre d'obstacles mobiles détectés
	 */
	public int nb_obstacles()
	{
		return listObstacles.size();
	}
	
	public int nearestFreeFresco(Vec2 position)
	{
		int min = 0;
		for (int i = 0; i < list_fresco_hanged.length ; i++)
			if (!(list_fresco_hanged[i]) && list_fresco_pos[i].getPosition().SquaredDistance(position) < list_fresco_pos[min].getPosition().SquaredDistance(position))
				min = i;
		return min;
	}
	public void appendFresco(int i)
	//ça ajoute une fresque par rapport à la position
	//on utilisera nearestFrescoFree pour trouver i
	{
		list_fresco_hanged[i] = true;
	}
	public float distanceFresco(Vec2 position, int i)
	{
		return position.distance(list_fresco_pos[i].getPosition());		
	}
	//Il faudra faire gaffe à la différence entre les distance et les squaredDistance quand on les compare avec des constantes ! Achtung !!!
	
}


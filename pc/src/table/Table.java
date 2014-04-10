package table;

import java.util.ArrayList;
import java.util.Iterator;

import robot.Cote;
import smartMath.Vec2;
import container.Service;
import exception.ConfigException;
import utils.*;

public class Table implements Service {

	// On met cette variable en static afin que, dans deux instances dupliquées, elle ne redonne pas les mêmes nombres
	private static int indice = 1;

	private Tree arrayTree[] = new Tree[4];
	private Torch arrayTorch[] = new Torch[2];
	private Fire arrayFire[] = new Fire[6];
	private Fire arrayFixedFire[] = new Fire[4];

	// TODO Obstacles fixes (circulaires) pour support de feux en bordure
	
	private ArrayList<ObstacleCirculaire> listObstacles = new ArrayList<ObstacleCirculaire>();
	private static ArrayList<Obstacle> listObstaclesFixes = new ArrayList<Obstacle>();
	private ObstacleBalise[] robots_adverses = new ObstacleBalise[2];
	
	private int hashFire;
	private int hashTree;
	private int hashObstacles;
	private int hashEnnemis;
	
	private Fresco[] list_fresco_pos = new Fresco[3];
	private boolean[] list_fresco_hanged = new boolean[3];

	private int rayon_robot_adverse = 200;
	private long duree = 0;

	// Dépendances
	private Log log;
	private Read_Ini config;
	
	public Table(Log log, Read_Ini config)
	{
		this.log = log;
		this.config = config;
		maj_config();
		initialise();
	}
	
	public void initialise()
	{
		// Initialisation des feux
		// TODO vérifier couleur. Torche fixe?
		arrayFire[0] = new Fire(new Vec2(1100,900), 1, 0, Colour.RED);	// ok
		arrayFire[1] = new Fire(new Vec2(600,1400), 2, 0, Colour.YELLOW); // OK
		arrayFire[2] = new Fire(new Vec2(600,400), 6, 0, Colour.RED); // ok
		arrayFire[3] = new Fire(new Vec2(-600,1400), 9, 0, Colour.YELLOW); //ok
		arrayFire[4] = new Fire(new Vec2(-600,400), 13, 0, Colour.RED); // ok
		arrayFire[5] = new Fire(new Vec2(-1100,900), 14, 0, Colour.YELLOW); // ok

		arrayFixedFire[0] = new Fire(new Vec2(1485,1200), 0, 0, Colour.YELLOW);
		arrayFixedFire[1] = new Fire(new Vec2(200,15), 7, 0, Colour.YELLOW);
		arrayFixedFire[2] = new Fire(new Vec2(-200,15), 8, 0, Colour.RED);
		arrayFixedFire[3] = new Fire(new Vec2(-1485,1200), 15, 0, Colour.YELLOW);
		
		// Initialisation des arbres
		arrayTree[0] = new Tree(new Vec2(1500,700));
		arrayTree[1] = new Tree(new Vec2(800,0));
		arrayTree[2] = new Tree(new Vec2(-800,0));
		arrayTree[3] = new Tree(new Vec2(-1500,700));
		
		// Initialisation des torches
/*		Fire feu0 = new Fire(new Vec2(600,900), 3, 1, Colour.YELLOW);
		Fire feu1 = new Fire(new Vec2(600,900), 4, 2, Colour.RED);
		Fire feu2 = new Fire(new Vec2(600,900), 5, 3, Colour.YELLOW);
		arrayTorch[0] = new Torch(new Vec2(600,900), feu0, feu1, feu2);

		Fire feu3 = new Fire(new Vec2(-600,900), 10, 1, Colour.RED);
		Fire feu4 = new Fire(new Vec2(-600,900), 11, 2, Colour.YELLOW);
		Fire feu5 = new Fire(new Vec2(-600,900), 12, 3, Colour.RED);
		arrayTorch[1] = new Torch(new Vec2(-600,900), feu3, feu4, feu5); 
*/

		arrayTorch[0] = new Torch(new Vec2(600,900));
		arrayTorch[1] = new Torch(new Vec2(-600,900)); 

		// Ajout des torches mobiles
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(600,900), 80));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(-600,900), 80));
		
		// Ajout des foyers
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(1500,0), 250));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(0,950), 150));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(-1500,0), 250));

		// Ajout bacs
		listObstaclesFixes.add(new ObstacleRectangulaire(new Vec2(400,2000), 700, 300));
		listObstaclesFixes.add(new ObstacleRectangulaire(new Vec2(-1100,2000), 700, 300));

		// Ajout des arbres
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(1500,700), 150));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(800,0), 150));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(-800,0), 150));
		listObstaclesFixes.add(new ObstacleCirculaire(new Vec2(-1500,700), 150));
		
		robots_adverses[0] = new ObstacleBalise(new Vec2(-1000, -1000), rayon_robot_adverse, new Vec2(0, 0));
		robots_adverses[1] = new ObstacleBalise(new Vec2(-1000, -1000), rayon_robot_adverse, new Vec2(0, 0));
		
		hashFire = 0;
		hashTree = 0;
		hashObstacles = 0;
		hashEnnemis = 0;
		
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
	
	/**
	 * Utilisé par le pathfinding. Retourne uniquement les obstacles temporaires.
	 * @return
	 */
	public ArrayList<ObstacleCirculaire> getListObstacles()
	{
		return listObstacles;
	}
	
	/**
	 * Utilisé par le pathfinding. Retourne uniquement les obstacles fixes.
	 * @return
	 */
	public ArrayList<Obstacle> getListObstaclesFixes()
	{
		return listObstaclesFixes;
	}
	
	/**
	 * Renvoie un code selon la présence ou non des torches mobiles
	 * 0: les deux torches sont là
	 * 1: la torche de gauche a disparue
	 * 2: la torche de droite a disparue
	 * 3: les deux torches sont absentes
	 * @return ce code
	 */
	public int codeTorches()
	{
		int out = 0;
		if(arrayTorch[0].isDisparue())
			out++;
		out <<= 1;
		if(arrayTorch[1].isDisparue())
			out++;
		return out;
	}
	
	public synchronized void creer_obstacle(final Vec2 position)
	{
		Vec2 position_sauv = position.clone();
		
		ObstacleProximite obstacle = new ObstacleProximite(position_sauv, rayon_robot_adverse, System.currentTimeMillis()+duree);
		log.warning("Obstacle créé, rayon = "+rayon_robot_adverse+", centre = "+position, this);
		listObstacles.add(obstacle);
		hashObstacles = indice++;
	}

	/**
	 * Appel fait lors de l'anticipation, supprime les obstacles périmés à une date future
	 * @param date
	 */
	public synchronized void supprimer_obstacles_perimes(long date)
	{
		Iterator<ObstacleCirculaire> iterator = listObstacles.iterator();
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
	
	/**
	 * Appel fait par le thread timer, supprime les obstacles périmés
	 */
	public void supprimer_obstacles_perimes()
	{
		supprimer_obstacles_perimes(System.currentTimeMillis());
	}

	/**
	 * Renvoie true si un obstacle est à une distance inférieur à "distance" du point "centre_detection"
	 * @param centre_detection
	 * @param distance
	 * @return
	 */
	public boolean obstaclePresent(final Vec2 centre_detection, int distance)
	{
		for(Obstacle obstacle: listObstacles)
		{
			// On regarde si l'intersection des cercles est vide
			if(obstacle instanceof ObstacleCirculaire && obstacle.position.SquaredDistance(centre_detection) < (distance+((ObstacleCirculaire)obstacle).radius)*(distance+((ObstacleCirculaire)obstacle).radius))
				return true;
			else if(!(obstacle instanceof ObstacleCirculaire))
			{
				// Normalement, les obstacles non fixes sont toujours circulaires
				log.warning("Etrange, un obstacle non circulaire... actualiser \"obstaclePresent\" dans Table", this);
				if(obstacle.position.SquaredDistance(centre_detection) < distance*distance)
			    	return true;		    
			}
		}
		
		return robots_adverses[0].position.SquaredDistance(centre_detection) < distance*distance
				|| robots_adverses[1].position.SquaredDistance(centre_detection) < distance*distance;
	}	

	/**
	 * Utilisé par le thread de laser
	 * @param i
	 * @param position
	 */
    public synchronized void deplacer_robot_adverse(int i, final Vec2 position)
    {
    	robots_adverses[i].position = position.clone();
    	hashEnnemis = indice++;
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
	
	public synchronized void pickFire (int id)
	{
		arrayFire[id].pickFire();
		hashFire = indice++;
	}

	public int nearestUntakenFire (final Vec2 position)
	{
		// On ne prend pas en compte les feux dans les torches
		int min = 0;
		for (int i = 1; i < 6; i++)
			if (!arrayFire[i].isTaken() && arrayFire[i].getPosition().SquaredDistance(position) < arrayFire[min].getPosition().SquaredDistance(position))
				min = i;
		return min;
	}
	
	public synchronized void putFire (int id)
	{
		arrayFire[id].ejectFire();
		hashFire = indice++;
	}
	
	public float distanceFire(final Vec2 position, int i)
	{
		return position.distance(arrayFire[i].position);
	}
	
	public float angleFire(final Vec2 position, int i)
	{
		return (float) Math.atan2(position.y - arrayFire[i].getPosition().y, position.x - arrayFire[i].getPosition().x);
	}
	
	public Colour getFireColour(int i)
	{
		return arrayFire[i].getColour();
	}
	public Fire[] getListFire()
	{
		return arrayFire;
	}
	
	// Arbres
	
	public int nearestUntakenTree (final Vec2 position)
	{
		int min = 0;
		for (int i = 0; i < 4; i++)
			if (!arrayTree[i].isTaken() && arrayTree[i].getPosition().SquaredDistance(position) < arrayTree[min].getPosition().SquaredDistance(position))
				min = i;
		return min;
	}
	
	public float distanceTree(final Vec2 position, int i)
	{
		return position.distance(arrayTree[i].position);
	}

	public synchronized void pickTree (int id)
	{
		arrayTree[id].setTaken();
		hashTree = indice++;
	}
	
	public void setFruitNoir(int id, int pos_fruit_noir)
	{
		//La nomenclature des positions des fruits noirs provient de la description de la classe Tree
		
		arrayTree[id].getArrayFruit()[pos_fruit_noir] = new Fruit(false);
		
		
		/*
		System.out.println("On initialise l'arbre numéro "+id);
		
		for(Fruit f : arrayTree[id].getArrayFruit())
		{
			System.out.println(f.isGood());
		}
*/
		
	}
	
	public int nbrTree(int id, Cote cote)
	{
		if(cote == Cote.DROIT)
			return arrayTree[id].nbrRight();
		else
			return arrayTree[id].nbrLeft();
	}
	
	public int nbrTotalTree(int tree_id)
	{
		return arrayTree[tree_id].nbrTotal();
	}
	
	public boolean isTreeTaken(int tree_id)
	{
		return arrayTree[tree_id].isTaken();
	}
	public Tree[] getListTree()
		{
			return arrayTree;
		}
	
	//Torches
	
	public int nearestTorch (final Vec2 position)
	{
		if(arrayTorch[0].getPosition().SquaredDistance(position) < arrayTorch[1].getPosition().SquaredDistance(position))
			return 0;
		else
			return 1;
	}

	public float distanceTorch(final Vec2 position, int i)
	{
		return position.distance(arrayTorch[i].position);
	}

	public void torche_disparue(Cote cote)
	{
		if(cote == Cote.DROIT)
			arrayTorch[0].setDisparue();
		else
			arrayTorch[1].setDisparue();
	}
	
	//La table
	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void clone(Table ct)
	{
		if(!equals(ct))
		{
			// Pour les torches, un hash ralentirait plus qu'autre chose
			arrayTorch[0].clone(ct.arrayTorch[0]);
			arrayTorch[1].clone(ct.arrayTorch[1]);
			
			if(ct.hashFire != hashFire)
			{
				for(int i = 0; i < 6; i++)
					arrayFire[i].clone(ct.arrayFire[i]);
				for(int i = 0; i < 4; i++)
					arrayFixedFire[i].clone(ct.arrayFixedFire[i]);
				ct.hashFire = hashFire;
			}
	
			if(ct.hashTree != hashTree)
			{
				for(int i = 0; i < 4; i++)		
					arrayTree[i].clone(ct.arrayTree[i]);
				ct.hashTree = hashTree;
			}

			if(ct.hashEnnemis != hashEnnemis)
			{
				robots_adverses[0].clone(robots_adverses[0]);
				robots_adverses[1].clone(robots_adverses[1]);
				ct.hashEnnemis = hashEnnemis;
			}

			if(ct.hashObstacles != hashObstacles)
			{
				ct.listObstacles.clear();
				for(ObstacleCirculaire item: listObstacles)
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
		return (((hashEnnemis*100 + hashFire)*100 + hashTree)*100 + hashObstacles)*4+codeTorches();
	}

	/**
	 * Utilisé pour les tests
	 * @param other
	 * @return
	 */
	public boolean equals(Table other)
	{
		return 	other != null
				&& hashFire == other.hashFire
				&& hashTree == other.hashTree
				&& hashObstacles == other.hashObstacles;
	}
	
	/**
	 * Utilisé pour les tests
	 * @return le nombre ed'obstacles mobiles détectés
	 */
	public int nb_obstacles()
	{
		return listObstacles.size();
	}
	//Fresco
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
	
	public boolean dans_obstacle(Vec2 pos, Obstacle obstacle)
		{
	 		if(obstacle instanceof ObstacleRectangulaire)
	 		{
	 			Vec2 position_obs = obstacle.getPosition();
				return !(pos.x<((ObstacleRectangulaire)obstacle).getLongueur()+position_obs.x && position_obs.x < pos.x && position_obs.y <pos.y && pos.y < position_obs.y+((ObstacleRectangulaire)obstacle).getLargeur());
	
	 		}			
	 		// sinon, c'est qu'il est circulaire
			return   !(pos.distance(obstacle.getPosition()) < ((ObstacleCirculaire)obstacle).getRadius());
	
	 	}
	
	public void maj_config()
	{
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
	}
	
	/**
	 * Indique si un obstacle de centre proche de la position indiquée existe.
	 * Cela permet de ne pas détecter en obstacle mobile des obstacles fixes (comme les arbres).
	 * De plus, ça allège le nombre d'obstacles.
	 * @param position
	 * @return
	 */
	public boolean obstacle_existe(Vec2 position) {
		for(Obstacle o: listObstacles)
			if(obstacle_existe(position, o))
				return true;
		for(Obstacle o: listObstaclesFixes)
			if(obstacle_existe(position, o))
				return true;
		return false;
	}
	
	private boolean obstacle_existe(Vec2 position, Obstacle o)
	{
		// Obstacle circulaire
		if(o instanceof ObstacleCirculaire && position.SquaredDistance(o.position) <= (1.2*((ObstacleCirculaire)o).getRadius())*1.2*((ObstacleCirculaire)o).getRadius())
			return true;
		// Obstacle rectangulaire
		else if(o instanceof ObstacleRectangulaire && ((ObstacleRectangulaire)o).SquaredDistance(position) <= 100*100)
			return true;
		// Autre obstacle
		else if(position.SquaredDistance(o.position) <= 100)
			return true;
		return false;
	}
}


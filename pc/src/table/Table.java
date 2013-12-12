package table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import robot.Orientation;
import smartMath.Vec2;
import strategie.MemoryManagerProduct;
import container.Service;
import utils.*;

public class Table implements Service, MemoryManagerProduct {

	// On met cette variable en static afin que, dans deux instances dupliquées, elle ne redonne pas les mêmes nombres
	private static Random rand = new Random();

	private Fire arrayFire[] = new Fire[16];
	private Tree arrayTree[] = new Tree[4];
	private Fireplace arrayFireplace[]= new Fireplace[3];
	private Torch arrayTorch[] = new Torch[10];

	private ArrayList<Obstacle> listObstacles = new ArrayList<Obstacle>();

	private int hashFire;
	private int hashTree;
	private int hashFirePlace;
	private int hashTorch;
	private int hashObstacles;

	// Dépendances
	private Log log;
	private Read_Ini config;
	
	public Table(Log log, Read_Ini config)
	{
		this.log = log;
		this.config = config;
	}
	
	public void initialise()
	{
		arrayFire[0] = new Fire(new Vec2(1485,1200), 0, 0, Orientation.XPLUS, Colour.YELLOW);
		arrayFire[1] = new Fire(new Vec2(1100,900), 1, 0, Orientation.YPLUS, Colour.YELLOW);
		arrayFire[2] = new Fire(new Vec2(600,1400), 2, 0, Orientation.XPLUS, Colour.YELLOW);
		arrayFire[3] = new Fire(new Vec2(600,900), 3, 1, Orientation.GROUND, Colour.YELLOW);
		arrayFire[4] = new Fire(new Vec2(600,900), 4, 2, Orientation.GROUND, Colour.RED);
		arrayFire[5] = new Fire(new Vec2(600,900), 5, 3, Orientation.GROUND, Colour.YELLOW);
		arrayFire[6] = new Fire(new Vec2(600,400), 6, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[7] = new Fire(new Vec2(200,15), 7, 0, Orientation.YPLUS, Colour.YELLOW);
		arrayFire[8] = new Fire(new Vec2(-200,15), 8, 0, Orientation.YPLUS, Colour.RED);
		arrayFire[9] = new Fire(new Vec2(-600,1400), 9, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[10] = new Fire(new Vec2(-600,900), 10, 1, Orientation.GROUND, Colour.RED);
		arrayFire[11] = new Fire(new Vec2(-600,900), 11, 2, Orientation.GROUND, Colour.YELLOW);
		arrayFire[12] = new Fire(new Vec2(-600,900), 12, 3, Orientation.GROUND, Colour.RED);
		arrayFire[13] = new Fire(new Vec2(-600,400), 13, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[14] = new Fire(new Vec2(-1100,900), 14, 0, Orientation.YPLUS, Colour.RED);
		arrayFire[15] = new Fire(new Vec2(-1485,1200), 15, 0, Orientation.XPLUS, Colour.YELLOW);
		//on passe à l'initialisation des arbres
		arrayTree[0] = new Tree(new Vec2(1500,700), 0);
		arrayTree[1] = new Tree(new Vec2(800,0), 1);
		arrayTree[2] = new Tree(new Vec2(-800,0), 2);
		arrayTree[3] = new Tree(new Vec2(-1500,700), 3);
		//initialisation des foyers
		arrayFireplace[0] = new Fireplace(new Vec2(1500,0), 250) ;
		arrayFireplace[1] = new Fireplace(new Vec2(0,950), 150) ;
		arrayFireplace[2] = new Fireplace(new Vec2(-1500,0), 250) ;
		//initialisation des torches
		arrayTorch[0] = new Torch(new Vec2(600,900), 0, true, 80) ; //0 et 1 sont les torches mobiles
		arrayTorch[1] = new Torch(new Vec2(-600,900), 1, true, 80) ;  //de 2 à 9, chaque torche est un des piliers des torches fixes
		arrayTorch[2] = new Torch(new Vec2(1489,1258), 2, false, 11) ;
		arrayTorch[3] = new Torch(new Vec2(1489,1142), 3, false, 11) ;
		arrayTorch[4] = new Torch(new Vec2(258,11), 4, false, 11) ;
		arrayTorch[5] = new Torch(new Vec2(142,11), 5, false, 11) ;
		arrayTorch[6] = new Torch(new Vec2(-142,11), 6, false, 11) ;
		arrayTorch[7] = new Torch(new Vec2(-258,11), 7, false, 11) ;
		arrayTorch[8] = new Torch(new Vec2(-1489,1258), 8, false, 11) ;
		arrayTorch[9] = new Torch(new Vec2(-1489,1142), 9, false, 11) ;

		// TODO placer dans arrayObstacles les obstacles fixes (foyers, bac, torches, ...)
		hashFire = rand.nextInt();
		hashTree = rand.nextInt();
		hashFirePlace = rand.nextInt();
		hashTorch = rand.nextInt();
		hashObstacles = rand.nextInt();

	}
	
	public void creer_obstacle(Vec2 position)
	{
		int rayon_robot_adverse = 0;
		long duree = 0;
		try {
			rayon_robot_adverse = Integer.parseInt(config.config.getProperty("rayon_robot_adverse"));
			duree = Integer.parseInt(config.config.getProperty("duree_peremption_obstacles"));
		}
		catch(Exception e)
		{
			this.log.critical(e, this);
		}
		
		Obstacle obstacle = new ObstacleProximite(position, rayon_robot_adverse, System.currentTimeMillis()+duree);
		listObstacles.add(obstacle);
		hashObstacles = rand.nextInt();
	}

	/**
	 * Appel fait lors de l'anticipation, supprime les obstacles périmés à une date future
	 * @param date
	 */
	public void supprimer_obstacles_perimes(long date)
	{
		Iterator<Obstacle> iterator = listObstacles.iterator();
		while ( iterator.hasNext() ) {
		    Obstacle obstacle = iterator.next();
		    if (obstacle instanceof ObstacleProximite && ((ObstacleProximite) obstacle).death_date <= date)
		    {
		        iterator.remove();
		        rand.nextInt();
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
	
	// TODO Guy
	// Feux
		// récupérer feu (id) : void
		// feu le plus proche (void) : feu
		// poser feu (id) : void
	
	// Arbre
		// récupérer arbre (id) : void
		// combien gauche (id) : int
		// combien droit (id) : int
		// points d'entrée (void) : int[] ([id, id] ou [id] si un seul)
			// syntaxe tableau: int entryPoints[] = new int[2]; (ou 1!)
	
	// Torche
		// torche la plus proche (Vec2) : torche (distance euclidienne)
	
	// Feux
	
	public void pickFire (int id)
	{
		arrayFire[id].pickFire();
	}


	public Fire nearestFire (Vec2 position)
	{
		int min = 0;
		for (int i = 0; i < 10; i++)
		{
			if (arrayFire[i].getPosition().SquaredDistance(position) < arrayFire[min].getPosition().SquaredDistance(position))
			{
				min = i;
			}
		}
		return arrayFire[min];
	}


	public MemoryManagerProduct clone(MemoryManagerProduct cloned_table) {
		((Table)cloned_table).initialise(arrayFire, arrayTree, arrayFireplace, arrayTorch, listObstacles, hashFire, hashTree, hashFirePlace, hashTorch, hashObstacles);
		return cloned_table;
	}
	
	public MemoryManagerProduct clone()
	{
		Table cloned_table = new Table(log, config);
		return clone(cloned_table);
	}

	// TODO changera probablement à l'avenir
	/**
	 * Méthode d'initialisation d'une table, utilisé par clone()
	 */
	public void initialise(Fire arrayFire[], Tree arrayTree[], Fireplace arrayFireplace[], Torch arrayTorch[], ArrayList<Obstacle> listObstacles, int hashFire, int hashTree, int hashFirePlace, int hashTorch, int hashObstacles)
	{
		if(this.hashFire != hashFire)
		{
			for(int i = 0; i < 16; i++)		
				this.arrayFire[i] = arrayFire[i].clone();
			this.hashFire = hashFire;
		}

		if(this.hashTree != hashTree)
		{
			for(int i = 0; i < 4; i++)		
				this.arrayTree[i] = arrayTree[i].clone();
			this.hashTree = hashTree;
		}

		if(this.hashFirePlace != hashFirePlace)
		{
			for(int i = 0; i < 3; i++)		
				this.arrayFireplace[i] = arrayFireplace[i].clone();
			this.hashFirePlace = hashFirePlace;
		}

		if(this.hashTorch != hashTorch)
		{
			for(int i = 0; i < 10; i++)		
				this.arrayTorch[i] = arrayTorch[i].clone();
			this.hashTorch = hashTorch;
		}

		if(this.hashObstacles != hashObstacles)
		{
			for(Obstacle item: listObstacles)
				this.listObstacles.add(item.clone());
			this.hashObstacles = hashObstacles;
		}
	}

	public String getNom() {
		return "Table";
	}

}


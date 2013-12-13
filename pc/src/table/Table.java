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
	private static int indice = 1;

	private Tree arrayTree[] = new Tree[4];
	private Torch arrayTorch[] = new Torch[2];
	private Fire arrayFire[] = new Fire[10];

	private ArrayList<Obstacle> listObstacles = new ArrayList<Obstacle>();

	private int hashFire;
	private int hashTree;
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
		arrayTree[0] = new Tree(0);
		arrayTree[1] = new Tree(1);
		arrayTree[2] = new Tree(2);
		arrayTree[3] = new Tree(3);

		// Initialisation des torches
		Fire feu0 = new Fire(new Vec2(600,900), 3, 1, Orientation.GROUND, Colour.YELLOW);
		Fire feu1 = new Fire(new Vec2(600,900), 4, 2, Orientation.GROUND, Colour.RED);
		Fire feu2 = new Fire(new Vec2(600,900), 5, 3, Orientation.GROUND, Colour.YELLOW);
		arrayTorch[0] = new Torch(new Vec2(600,900), 0, feu0, feu1, feu2);

		Fire feu3 = new Fire(new Vec2(-600,900), 10, 1, Orientation.GROUND, Colour.RED);
		Fire feu4 = new Fire(new Vec2(-600,900), 11, 2, Orientation.GROUND, Colour.YELLOW);
		Fire feu5 = new Fire(new Vec2(-600,900), 12, 3, Orientation.GROUND, Colour.RED);
		arrayTorch[1] = new Torch(new Vec2(-600,900), 1, feu3, feu4, feu5); 

		// Ajout des torches mobiles
		listObstacles.add(new ObstacleCirculaire(new Vec2(600,900), 80));
		listObstacles.add(new ObstacleCirculaire(new Vec2(-600,900), 80));
		
		// Ajout des foyers
		listObstacles.add(new ObstacleCirculaire(new Vec2(1500,0), 250));
		listObstacles.add(new ObstacleCirculaire(new Vec2(0,950), 150));
		listObstacles.add(new ObstacleCirculaire(new Vec2(-1500,0), 250));

		// TODO bacs obstacles
		listObstacles.add(new ObstacleRectangulaire(new Vec2(400,1700), 700, 300));
		listObstacles.add(new ObstacleRectangulaire(new Vec2(-1100,1700), 700, 300));

		// Ajout des arbres
		listObstacles.add(new ObstacleCirculaire(new Vec2(1500,700), 150));
		listObstacles.add(new ObstacleCirculaire(new Vec2(800,0), 150));
		listObstacles.add(new ObstacleCirculaire(new Vec2(-800,0), 150));
		listObstacles.add(new ObstacleCirculaire(new Vec2(-1500,700), 150));

		hashFire = 0;
		hashTree = 0;
		hashTorch = 0;
		hashObstacles = 0;
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
		hashObstacles = indice++;
	}

	/**
	 * Appel fait lors de l'anticipation, supprime les obstacles périmés à une date future
	 * @param date
	 */
	public void supprimer_obstacles_perimes(long date)
	{
		Iterator<Obstacle> iterator = listObstacles.iterator();
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
	
	// Feux
	
	public void pickFire (int id)
	{
		arrayFire[id].pickFire();
		hashFire = indice++;
	}


	public Fire nearestFire (Vec2 position)
	{
		int min = 0;
		for (int i = 0; i < 16; i++)
			if (arrayFire[i].getPosition().SquaredDistance(position) < arrayFire[min].getPosition().SquaredDistance(position))
				min = i;
		return arrayFire[min];
	}
	
	public void putFire (int id)
	{
		arrayFire[id].ejectFire();
		hashFire = indice++;
	}
	
	// Arbres
	
	public void pickTree (int id)
	{

		arrayTree[id].setTaken();
		hashTree = indice++;
	}
	
	public int nbrLeft (int id)
	{
		return arrayTree[id].nbrLeft();
	}
	
	public int nbrRight (int id)
	{
		return arrayTree[id].nbrRight();
	}
	
	public int[] entryPoint(boolean rightSide)
	{
		int c;
		if (rightSide) {
			c = 2;
		} else {
			c = 0;
		}
		if (arrayTree[c].isTaken()) {
			int[] tab = {c+1};
			return tab;
		} else {
			if (arrayTree[c+1].isTaken()) {
				int[] tab = {c};
				return tab;
			} else {
				int[] tab = {c,c+1};
				return tab;
			}
		}
	}
	
	//Torches
	
	public Torch nearestTorch (Vec2 position)
	{
		int min = 0;
		for (int i = 0; i < 10; i++)
			if (arrayTorch[i].getPosition().SquaredDistance(position) < arrayFire[min].getPosition().SquaredDistance(position))
				min = i;
		return arrayTorch[min];
	}
			
	public MemoryManagerProduct clone(MemoryManagerProduct cloned_table) {
		((Table)cloned_table).initialise(arrayFire, arrayTree, arrayTorch, listObstacles, hashFire, hashTree, hashTorch, hashObstacles);
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
	public void initialise(Fire arrayFire[], Tree arrayTree[], Torch arrayTorch[], ArrayList<Obstacle> listObstacles, int hashFire, int hashTree, int hashTorch, int hashObstacles)
	{
		if(this.hashFire != hashFire)
		{
			for(int i = 0; i < 10; i++)		
				this.arrayFire[i] = arrayFire[i].clone();
			this.hashFire = hashFire;
		}

		if(this.hashTree != hashTree)
		{
			for(int i = 0; i < 4; i++)		
				this.arrayTree[i] = arrayTree[i].clone();
			this.hashTree = hashTree;
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


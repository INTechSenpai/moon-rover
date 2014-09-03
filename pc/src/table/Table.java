package table;

import java.util.ArrayList;

import smartMath.Vec2;
import table.obstacles.GestionObstacles;
import table.obstacles.Obstacle;
import table.obstacles.ObstacleCirculaire;
import container.Service;
import enums.Colour;
import enums.Cote;
import utils.*;

public class Table implements Service {

	// On met cette variable en static afin que, dans deux instances dupliquées, elle ne redonne pas les mêmes nombres
	private static int indice = 1;

	private GestionObstacles gestionobstacles;
	
	private Tree arrayTree[] = new Tree[4];
	private Torch arrayTorch[] = new Torch[2];
	private Fire arrayFire[] = new Fire[6];
	private Fire arrayFixedFire[] = new Fire[4];

	private boolean leftMammothHit = false;
	private boolean rightMammothHit = false;
	
	private int hashFire;
	private int hashTree;
	
	private Fresco[] list_fresco_pos = new Fresco[3];
	private boolean[] list_fresco_hanged = new boolean[3];

	// Dépendances
	private Log log;
	private Read_Ini config;
	
	public Table(Log log, Read_Ini config)
	{
		this.log = log;
		this.config = config;
		this.gestionobstacles = new GestionObstacles(log, config);
		maj_config();
		initialise();
	}
	
	/**
	 * Permet de signaler à la table où sont les fruits noirs. Par défaut, la table
	 * considère que tous les fruits sont bons.
	 * @param fruitsNoirs
	 */
	public void setFruitsNoirs(int[] fruitsNoirs)
	{
		//Pas bien car ça ne prend pas en compte la symétrie
        arrayTree[0] = new Tree(new Vec2(1500,700), fruitsNoirs[0]);
        arrayTree[1] = new Tree(new Vec2(800,0), fruitsNoirs[1]);
        arrayTree[2] = new Tree(new Vec2(-800,0), fruitsNoirs[2]);
        arrayTree[3] = new Tree(new Vec2(-1500,700), fruitsNoirs[3]);
	}
	
	public void initialise()
	{
	    /**
	     * Demander la position des fruits noirs
	     */
	    
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
		arrayTree[0] = new Tree(new Vec2(1500,700), -1);
		arrayTree[1] = new Tree(new Vec2(800,0), -1);
		arrayTree[2] = new Tree(new Vec2(-800,0), -1);
		arrayTree[3] = new Tree(new Vec2(-1500,700), -1);
				
		//initialisation des mamouths
		leftMammothHit = false;
		rightMammothHit = false;

		arrayTorch[0] = new Torch(new Vec2(600,900));
		arrayTorch[1] = new Torch(new Vec2(-600,900)); 
		
		hashFire = 0;
		hashTree = 0;
		
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
	 * Obstacles
	 */
	
    public ArrayList<ObstacleCirculaire> getListObstacles()
    {
        return gestionobstacles.getListObstacles();
    }

    public ArrayList<Obstacle> getListObstaclesFixes()
    {
        return gestionobstacles.getListObstaclesFixes(codeTorches());
    }

    public void creer_obstacle(final Vec2 position)
    {
        gestionobstacles.creer_obstacle(position);
    }
    
    // TODO: bien appelé par stratégie?
    public void supprimerObstaclesPerimes(long date)
    {
        gestionobstacles.supprimerObstaclesPerimes(date);
    }

    /**
     * Appel fait par le thread timer, supprime les obstacles périmés
     */
    public void supprimer_obstacles_perimes()
    {
        supprimerObstaclesPerimes(System.currentTimeMillis());
    }

    public boolean obstaclePresent(final Vec2 centre_detection, int distance)
    {
        return gestionobstacles.obstaclePresent(centre_detection, distance);
    }
    
    public void deplacer_robot_adverse(int i, final Vec2 position)
    {
        gestionobstacles.deplacer_robot_adverse(i, position);
    }

    public Vec2[] get_positions_ennemis()
    {
        return gestionobstacles.get_positions_ennemis();
    }

    public int nb_obstacles()
    {
        return gestionobstacles.nb_obstacles();
    }

    public boolean obstacle_existe(Vec2 position)
    {
        return gestionobstacles.obstacle_existe(position, codeTorches());
    }
    
    public boolean dans_obstacle(Vec2 pos, Obstacle obstacle)
    {
        return gestionobstacles.dans_obstacle(pos, obstacle);
    }


	// Feux
	
	public synchronized void pickFire (int id)
	{
		arrayFire[id].pickFire();
		hashFire = indice++;
	}
	public synchronized void pickFixedFire(int id)
	{
		arrayFixedFire[id].pickFire();
		hashFire = indice++;
	}
	public boolean isTakenFire(int id)
	{
		/*
		 * Pour les feux debouts au milieu du terrain (i.e. ni au bord, ni dans une torche)
		 */
		return arrayFire[id].isTaken();
	}
	public boolean isTakenFixedFire(int id)
	{
		return arrayFixedFire[id].isTaken();
	}
	public int furthestUntakenFire (final Vec2 position)
	{
		// On ne prend pas en compte les feux dans les torches
		int max = 0;
		for (int i = 1; i < 6; i++)
			if (!arrayFire[i].isTaken() && arrayFire[i].getPosition().SquaredDistance(position) > arrayFire[max].getPosition().SquaredDistance(position))
				max = i;
		return max;
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
	public int nearestUntakenFixedFire (final Vec2 position)
	{
		// On ne prend pas en compte les feux dans les torches
		int min = 0;
		for (int i = 1; i < 4; i++)
			if (!arrayFixedFire[i].isTaken() && arrayFixedFire[i].getPosition().SquaredDistance(position) < arrayFixedFire[min].getPosition().SquaredDistance(position))
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
	public float distanceFixedFire(final Vec2 position, int i)
	{
		return position.distance(arrayFixedFire[i].position);
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
	public Fire[] getListFixedFire()
	{
		return arrayFixedFire;
	}
	public void setProbaFire(int i, float proba )
	{
		arrayFire[i].setProbaFaitParEnnemi(proba);
	}
	public float setProbaFire(int i)
	{
		return arrayFire[i].getProbaFaitParEnnemi();
	}
	public void setProbaFixedFire(int i, float proba )
	{
		arrayFixedFire[i].setProbaFaitParEnnemi(proba);
	}
	public float getProbaFixedFire(int i)
	{
		return arrayFixedFire[i].getProbaFaitParEnnemi();
	}
	
	// Arbres
	public int furthestUntakenTree (final Vec2 position)
	{
		int max = 0;
		for (int i = 0; i < 4; i++)
			if (!arrayTree[i].isTaken() && arrayTree[i].getPosition().SquaredDistance(position) > arrayTree[max].getPosition().SquaredDistance(position))
				max = i;
		return max;
	}
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
	public void setFruitNoir(int id, int pos_fruit_noir)
	{
		//La nomenclature des positions des fruits noirs provient de la description de la classe Tree
		
		arrayTree[id].getArrayFruit()[pos_fruit_noir] = new Fruit(false);
			
	}

	public synchronized void pickTree (int id)
	{
		arrayTree[id].setTaken();
		hashTree = indice++;
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
	public void setProbaTree(int i, float proba )
	{
		arrayTree[i].setProbaFaitParEnnemi(proba);
	}
	public float getProbaTree(int i)
	{
		return arrayTree[i].getProbaFaitParEnnemi();
	}
	//Torches
	/**
	 * Renvoie un code selon la présence ou non des torches mobiles
	 * 3: les deux torches sont là
	 * 2: la torche de gauche a disparue
	 * 1: la torche de droite a disparue
	 * 0: les deux torches sont absentes
	 * @return ce code
	 */
	public int codeTorches()
	{
		int out = 0;
		if(!arrayTorch[0].isDisparue())
			out++;
		out <<= 1;
		if(!arrayTorch[1].isDisparue())
			out++;
		return out;
	}

    private int codeMammouth()
    {
        int out = 0;
        if(leftMammothHit)
            out++;
        out <<= 1;
        if(rightMammothHit)
            out++;
        return out;
    }

    public synchronized void pickTorch (int id)
    {
        arrayTorch[id].pickTorch();
        hashFire = indice++;
    }
    public boolean isTorchTaken(int id)
    {
    	return arrayTorch[id].isTaken();
    }
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
	
	public Vec2 getPositionTorche(Cote cote)
	{
		if(cote == Cote.DROIT)
			return arrayTorch[0].position.clone();
		else
			return arrayTorch[1].position.clone();
	}

	public int getRayonTorche(Cote cote)
	{
		if(cote == Cote.DROIT)
			return arrayTorch[0].rayon;
		else
			return arrayTorch[1].rayon;
	}
	public void modifierProbaTorch(int i, float proba )
	{
		arrayTorch[i].setProbaFaitParEnnemi(proba);
	}
	public float prendreProbaTorch(int i)
	{
		return arrayTorch[i].getProbaFaitParEnnemi();
	}
	public Torch[] getListTorch()
	{
		return arrayTorch;
	}

	//La table
	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct)
	{
        if(!equals(ct))
		{
            // pas de hash
            ct.leftMammothHit = this.leftMammothHit;
            ct.rightMammothHit = this.rightMammothHit;
			
			if(ct.hashFire != hashFire)
			{
				for(int i = 0; i < 2; i++)
				{
					arrayFire[i].clone(ct.arrayFire[i]);
                    arrayTorch[i].clone(ct.arrayTorch[i]);
                    arrayFixedFire[i].clone(ct.arrayFixedFire[i]);
				}
				for(int i = 2; i < 4; i++)
				{
                    arrayFire[i].clone(ct.arrayFire[i]);
					arrayFixedFire[i].clone(ct.arrayFixedFire[i]);
				}
                for(int i = 4; i < 6; i++)
                {
                    arrayFire[i].clone(ct.arrayFire[i]);
                }
				ct.hashFire = hashFire;
			}
	
			if(ct.hashTree != hashTree)
			{
				for(int i = 0; i < 4; i++)	
				    arrayTree[i].clone(ct.arrayTree[i]);
				ct.hashTree = hashTree;
			}

			if(!gestionobstacles.equals(ct.gestionobstacles))
			    gestionobstacles.copy(ct.gestionobstacles);
		}
	}
	
	public Table clone()
	{
		Table cloned_table = new Table(log, config);
		copy(cloned_table);
		return cloned_table;
	}

	/**
	 * Utilisé par les tests unitaires uniquement. Vérifie que les hash sont bien mis à jour
	 * @return
	 */
	public long hashTable()
	{	    
		return ((((((((((gestionobstacles.hash()<<6) + hashFire)<<6) + hashTree)<<6)))<<2)+codeMammouth())<<2)+codeTorches();
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
                && leftMammothHit == other.leftMammothHit
                && rightMammothHit == other.rightMammothHit
                && arrayTorch[0].isDisparue() == other.arrayTorch[0].isDisparue()
                && arrayTorch[1].isDisparue() == other.arrayTorch[1].isDisparue()
                && gestionobstacles.hash() == other.gestionobstacles.hash();
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
	
	public void setProbaFresco(int i, float proba )
	{
		list_fresco_pos[i].setProbaFaitParEnnemi(proba);
	}
	
	public float getProbaFresco(int i)
	{
		return list_fresco_pos[i].getProbaFaitParEnnemi();
	}
	@Override
	public void maj_config()
	{
	}
	
	/**
	 * @return the leftMammothHit
	 */
	public boolean isLeftMammothHit() {
		return leftMammothHit;
	}

	/**
	 * @param leftMammothHit the leftMammothHit to set
	 */
	public void setLeftMammothHit(boolean leftMammothHit) {
		this.leftMammothHit = leftMammothHit;
	}

	/**
	 * @return the rightMammothHit
	 */
	public boolean isRightMammothHit() {
		return rightMammothHit;
	}

	/**
	 * @param rightMammothHit the rightMammothHit to set
	 */
	public void setRightMammothHit(boolean rightMammothHit) {
		this.rightMammothHit = rightMammothHit;
	}
	

}


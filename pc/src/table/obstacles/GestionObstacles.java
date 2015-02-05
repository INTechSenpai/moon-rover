package table.obstacles;

import java.util.ArrayList;

import smartMath.Vec2;
import utils.Log;
import utils.Read_Ini;

/**
 * Traite tout ce qui concerne la gestion des obstacles.
 * @author pf, marsu
 *
 */

public class GestionObstacles
{
    @SuppressWarnings("unused")
    private Log log;
    @SuppressWarnings("unused")
	private Read_Ini config;

    private ArrayList<ObstacleCirculaire> listObstacles = new ArrayList<ObstacleCirculaire>();
  
    public GestionObstacles(Log log, Read_Ini config)
    {
        this.log = log;
        this.config = config;
        
        maj_config();
    }
    
    public void maj_config()
    {
    }
    

    public void copy(GestionObstacles other)
    {
    }
    
    

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
    public ArrayList<Obstacle> getListObstaclesFixes(int codeTorches)
    {
    	// TODO
        return new ArrayList<Obstacle>();
    }
    
    

    public synchronized void creer_obstacle(final Vec2 position)
    {
    	// TODO
    }

    /**
     * Appel fait lors de l'anticipation, supprime les obstacles périmés à une date future
     * @param date
     */
    public synchronized void supprimerObstaclesPerimes(long date)
    {
    	// Et pouf !
    	// TODO
    }
    

    /**
     * Renvoie true si un obstacle est à une distance inférieur à "distance" du point "centre_detection"
     * @param centre_detection
     * @param distance
     * @return
     */
    public boolean obstaclePresent(final Vec2 centre_detection, int distance)
    {
    	//TODO
    	return true;
    }   

    /**
     * Change le position d'un robot adverse
     * @param i numéro du robot
     * @param position nouvelle position du robot
     */
    public synchronized void deplacer_robot_adverse(int i, final Vec2 position)
    {
    	//TODO
    }
    
    /**
     * Utilisé par le thread de stratégie
     * @return
     */
    public Vec2[] get_positions_ennemis()
    {
    	// TODO
        return  new Vec2[1];
    }
    
    
    /**
     * Utilisé pour les tests
     * @return le nombre ed'obstacles mobiles détectés
     */
    public int nb_obstacles()
    {
        return listObstacles.size();
    }
    
    
    public boolean dans_obstacle(Vec2 pos, Obstacle obstacle)
    {

    	//TODO !
    	return true;

    }
    
    
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquÃ©e existe.
     * @param position
     */
    public synchronized boolean obstacle_existe(Vec2 position)
    {
    	//TODO
    	boolean IDontKnow = false;
        return IDontKnow;
    	
    }
    
    /**
     *  Cette instance est elle dans le mÃªme Ã©tat que other ?
     *  @param other
     */
    public boolean equals(GestionObstacles other)
    {
    	//TODO
    	boolean IDontKnow = false;
        return IDontKnow;
    }
    

}

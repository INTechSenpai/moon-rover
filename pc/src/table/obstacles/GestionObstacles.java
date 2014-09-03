package table.obstacles;

import java.util.ArrayList;
import java.util.Iterator;

import smartMath.Vec2;
import utils.Log;
import utils.Read_Ini;

/**
 * Traite tout ce qui concerne la gestion des obstacles.
 * @author pf
 *
 */

public class GestionObstacles
{
    // On met cette variable en static afin que, dans deux instances dupliquées, elle ne redonne pas les mêmes nombres
    private static int indice = 1;
    private Log log;
    private Read_Ini config;

    private ArrayList<ObstacleCirculaire> listObstacles = new ArrayList<ObstacleCirculaire>();
    private static ArrayList<ArrayList<Obstacle>> listObstaclesFixes = null;
    private ObstacleBalise[] robots_adverses = new ObstacleBalise[2];
  
    private int hashObstacles;
    private int hashEnnemis;

    private int rayon_robot_adverse = 200;
    private long duree = 0;

    public GestionObstacles(Log log, Read_Ini config)
    {
        this.log = log;
        this.config = config;

        hashObstacles = 0;
        hashEnnemis = 0;

        if(listObstaclesFixes == null)
        {
            listObstaclesFixes = new ArrayList<ArrayList<Obstacle>>();
            for(int i = 0; i < 4; i++)
                listObstaclesFixes.add(new ArrayList<Obstacle>());
            
            // Ajout des foyers
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(1500,0), 250));
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(0,950), 150));
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(-1500,0), 250));

            // Ajout des supports de balise
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(1500,1000), 50));
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(-1500,1000), 50));

            // Ajout bacs
            listObstaclesFixes.get(0).add(new ObstacleRectangulaire(new Vec2(400,2000), 700, 300));
            listObstaclesFixes.get(0).add(new ObstacleRectangulaire(new Vec2(-1100,2000), 700, 300));
            
            // Ajout des bordures
            listObstaclesFixes.get(0).add(new ObstacleRectangulaire(new Vec2(-1500,0), 3000, 1));
            listObstaclesFixes.get(0).add(new ObstacleRectangulaire(new Vec2(-1500,2000), 1, 2000));
            listObstaclesFixes.get(0).add(new ObstacleRectangulaire(new Vec2(-1500,2000), 3000, 1));
            listObstaclesFixes.get(0).add(new ObstacleRectangulaire(new Vec2(1500,2000), 1, 2000));
    
            // Ajout des arbres
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(1500,700), 150));
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(800,0), 150));
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(-800,0), 150));
            listObstaclesFixes.get(0).add(new ObstacleCirculaire(new Vec2(-1500,700), 150));
    
            // Recopie dans les autres listes d'obstacles fixes
            listObstaclesFixes.get(1).addAll(listObstaclesFixes.get(0));
            listObstaclesFixes.get(2).addAll(listObstaclesFixes.get(0));
            listObstaclesFixes.get(3).addAll(listObstaclesFixes.get(0));
            
            // Torches mobiles
            listObstaclesFixes.get(1).add(new ObstacleCirculaire(new Vec2(-600,900), 80));
            listObstaclesFixes.get(2).add(new ObstacleCirculaire(new Vec2(600,900), 80));
            listObstaclesFixes.get(3).add(new ObstacleCirculaire(new Vec2(-600,900), 80));
            listObstaclesFixes.get(3).add(new ObstacleCirculaire(new Vec2(600,900), 80));
        }
        
        robots_adverses[0] = new ObstacleBalise(new Vec2(-1000, -1000), rayon_robot_adverse, new Vec2(0, 0));
        robots_adverses[1] = new ObstacleBalise(new Vec2(-1000, -1000), rayon_robot_adverse, new Vec2(0, 0));
        
        maj_config();
    }
    
    public void maj_config()
    {
        rayon_robot_adverse = Integer.parseInt(config.get("rayon_robot_adverse"));
        duree = Integer.parseInt(config.get("duree_peremption_obstacles"));
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
        return listObstaclesFixes.get(codeTorches);
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
    public synchronized void supprimerObstaclesPerimes(long date)
    {
        Iterator<ObstacleCirculaire> iterator = listObstacles.iterator();
        while ( iterator.hasNext() )
        {
            Obstacle obstacle = iterator.next();
            if (obstacle instanceof ObstacleProximite && ((ObstacleProximite) obstacle).death_date <= date)
            {
                System.out.println("Suppression d'un obstacle de proximité: "+obstacle);
                iterator.remove();
                hashObstacles = indice++;
            }
        }   
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
    
    
    /**
     * Utilisé pour les tests
     * @return le nombre ed'obstacles mobiles détectés
     */
    public int nb_obstacles()
    {
        return listObstacles.size();
    }

    public void copy(GestionObstacles other)
    {
        if(other.hashEnnemis != hashEnnemis)
        {
            robots_adverses[0].clone(robots_adverses[0]);
            robots_adverses[1].clone(robots_adverses[1]);
            other.hashEnnemis = hashEnnemis;
        }

        if(other.hashObstacles != hashObstacles)
        {
            other.listObstacles.clear();
            for(ObstacleCirculaire item: listObstacles)
                other.listObstacles.add(item.clone());
            other.hashObstacles = hashObstacles;
        }

    }
    
    /**
     * Indique si un obstacle fixe de centre proche de la position indiquée existe.
     * Cela permet de ne pas détecter en obstacle mobile des obstacles fixes (comme les arbres).
     * De plus, ça allège le nombre d'obstacles.
     * @param position
     * @return
     */
    public synchronized boolean obstacle_existe(Vec2 position, int codeTorches) {
//      Iterator<ObstacleCirculaire> iterator = listObstacles.iterator();
//      while(iterator.hasNext())
//          if(obstacle_existe(position, iterator.next()))
//              return true;
        Iterator<Obstacle> iterator2 = listObstaclesFixes.get(codeTorches).iterator();
        while(iterator2.hasNext())
        {
            Obstacle o = iterator2.next();
            if(obstacle_existe(position, o))
            {
                System.out.println("Obstacle: "+o);
                return true;
            }
        }
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

    public boolean dans_obstacle(Vec2 pos, Obstacle obstacle)
    {
        if(obstacle instanceof ObstacleRectangulaire)
        {
            Vec2 position_obs = obstacle.getPosition();
            return !(pos.x<((ObstacleRectangulaire)obstacle).getLongueur_en_x()+position_obs.x && position_obs.x < pos.x && position_obs.y <pos.y && pos.y < position_obs.y+((ObstacleRectangulaire)obstacle).getLongueur_en_y());

        }           
        // sinon, c'est qu'il est circulaire
        return   !(pos.distance(obstacle.getPosition()) < ((ObstacleCirculaire)obstacle).getRadius());

    }
    
    public int hash()
    {
        return (hashObstacles<<6) + hashEnnemis;
    }

    public boolean equals(GestionObstacles other)
    {
        return hashObstacles == other.hashObstacles
               && hashEnnemis == other.hashEnnemis;
    }
    

}

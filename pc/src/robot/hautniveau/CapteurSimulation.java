package robot.hautniveau;

import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import enums.Colour;

/**
 * C'est comme un capteur... sauf que c'est simulé.
 * @author pf
 *
 */
public class CapteurSimulation implements Service
{
    
    private Read_Ini config;
    private Table table;
    private int largeur_robot;
    
    public CapteurSimulation(Log log, Read_Ini config, Table table)
    {
        this.config = config;
        this.table = table;
        maj_config();
    }

    @Override
    public void maj_config()
    {
        largeur_robot = Integer.parseInt(config.get("largeur_robot"));
    }

    // La couleur est simulée. Normalement, vu la disposition des couleurs, cela devrait se faire assez bien.
    public Colour getColour(Vec2 position, double orientation)
    {
        double orientation_utilisee;

        // TODO vérifier que c'est bien la largeur
        Vec2 avant_robot = new Vec2((int)(largeur_robot/2 * Math.cos(orientation)), (int)(largeur_robot/2 * Math.sin(orientation)));
        avant_robot.Plus(position);
        
        int i = table.nearestUntakenFire(avant_robot.clone());
        float distance = table.distanceFire(avant_robot.clone(), i);

        // Si on est plus à 5cm de la position normale debout, c'est que le feu est tombé.
        // Dans ce cas, on regarde où on a vu le feu, et on en déduit de quel côté il est tombé.
        if(distance > 50)
            orientation_utilisee = table.angleFire(avant_robot.clone(), i);
        // Sinon, c'est que le feu est encore debout. On regarde alors de quel côté on vient.
        else
            orientation_utilisee = orientation;
        
        Colour a_priori = table.getFireColour(i);
        if(orientation_utilisee >= Math.PI/4 && orientation_utilisee <= 5*Math.PI/4)
            return a_priori;
        else
            return inverserCouleur(a_priori);
    }
    
    private Colour inverserCouleur(Colour colour)
    {
        if(colour == Colour.RED)
            return Colour.YELLOW;
        else
            return Colour.RED;
    }    

}

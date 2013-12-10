package robot.cartes;

import robot.serial.Serial;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Classe des capteurs, qui communique avec la carte capteur
 * @author PF
 */

public class Capteur implements Service {

	// Dépendances
	private Read_Ini config;
	private Log log;
	private Serial serie;

    private int nb_capteurs_infrarouge_avant = 1;
    private int nb_capteurs_infrarouge_arriere = 1;
    private int nb_capteurs_ultrason_avant = 1;
    private int nb_capteurs_ultrason_arriere = 1;

	public Capteur(Service config, Service log, Service serie)
	{
		this.config = (Read_Ini)config;
		this.log = (Log)log;
		this.serie = (Serial)serie;
	}
    
    public int mesurer(boolean marche_arriere)
    {
    	if(marche_arriere)
    	{
    		return 3000; // TODO
    	}
    	else
    	{
    		return 3000; // TODO
    	}
    }
	
    public boolean demarrage_match()
    {
    	return false; // TODO
    }
    
}

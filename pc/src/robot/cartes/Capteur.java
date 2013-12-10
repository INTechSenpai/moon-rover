package robot.cartes;

import java.util.Arrays;

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

	private final int nb_capteurs_infrarouge_avant = 1;
    private final int nb_capteurs_infrarouge_arriere = 1;
    private final int nb_capteurs_ultrason_avant = 1;
    private final int nb_capteurs_ultrason_arriere = 1;
    
	public Capteur(Service config, Service log, Service serie)
	{
		this.config = (Read_Ini)config;
		this.log = (Log)log;
		this.serie = (Serial)serie;
	}
    
	/**
	 * Retourne la valeur la plus optimiste des capteurs dans la direction voulue
	 * @param marche_arriere
	 * @return la valeur la plus optimiste des capteurs
	 */
    public int mesurer(boolean marche_arriere)
    {
		String[] ultrasons;
		String[] infrarouges;
		int[] distances;
		
    	if(marche_arriere)
    	{
    		distances = new int[nb_capteurs_ultrason_arriere+nb_capteurs_infrarouge_arriere];
    		ultrasons = serie.communiquer("us_arr", nb_capteurs_ultrason_arriere);
    		infrarouges  = serie.communiquer("ir_arr", nb_capteurs_infrarouge_arriere);
    		for(int i = 0; i < nb_capteurs_ultrason_arriere; i++)
    			distances[i] = Integer.parseInt(ultrasons[i]);
    		for(int i = 0; i < nb_capteurs_infrarouge_arriere; i++)
    			distances[nb_capteurs_ultrason_arriere+i] = Integer.parseInt(infrarouges[i]);
    	}
    	else
    	{
    		distances = new int[nb_capteurs_ultrason_avant+nb_capteurs_infrarouge_avant];
    		ultrasons = serie.communiquer("us_av", nb_capteurs_ultrason_avant);
    		infrarouges  = serie.communiquer("ir_av", nb_capteurs_infrarouge_avant);
    		for(int i = 0; i < nb_capteurs_ultrason_avant; i++)
    			distances[i] = Integer.parseInt(ultrasons[i]);
    		for(int i = 0; i < nb_capteurs_infrarouge_avant; i++)
    			distances[nb_capteurs_ultrason_avant+i] = Integer.parseInt(infrarouges[i]);
    	}
    	
    	Arrays.sort(distances); // le dernier élément d'un tableau trié par ordre croissant est le plus grand
    	return distances[distances.length-1];
    }
	
    public boolean demarrage_match()
    {
    	 return serie.communiquer("j", 1)[0] == "0";
    }
    
}

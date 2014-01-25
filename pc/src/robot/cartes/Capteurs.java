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

public class Capteurs implements Service {

	// Dépendances
	private Log log;
	private Serial serie;

	private final int nb_capteurs_infrarouge_avant = 1;
//    private final int nb_capteurs_infrarouge_arriere = 0;
    private final int nb_capteurs_ultrason_avant = 1;
//    private final int nb_capteurs_ultrason_arriere = 0;
    
	public Capteurs(Read_Ini config, Log log, Serial serie)
	{
		this.log = log;
		this.serie = serie;
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
		
		try{
/*	    	if(marche_arriere)
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
	    	{*/
	    		distances = new int[nb_capteurs_ultrason_avant+nb_capteurs_infrarouge_avant];
	    		ultrasons = serie.communiquer("us_av", nb_capteurs_ultrason_avant);
	    		infrarouges  = serie.communiquer("ir_av", nb_capteurs_infrarouge_avant);
	    		for(int i = 0; i < nb_capteurs_ultrason_avant; i++)
	    			distances[i] = Integer.parseInt(ultrasons[i]);
	    		for(int i = 0; i < nb_capteurs_infrarouge_avant; i++)
	    			distances[nb_capteurs_ultrason_avant+i] = Integer.parseInt(infrarouges[i]);
//	    	}
	    	
	    	Arrays.sort(distances); // le dernier élément d'un tableau trié par ordre croissant est le plus grand
	    	int distance = distances[distances.length-1];
	    	if(distance < 0)
	    		return 3000;
	    	return distance;
		}
		catch(Exception e)
		{
			log.critical(e.toString(), this);
			return 3000; // valeur considérée comme infinie
		}
    }
	
    public boolean demarrage_match()
    {
    	 return Integer.parseInt(serie.communiquer("j", 1)[0]) == 0;
    }
 
    // TODO
    public boolean isThereFire()
    {
    	return false;
    }

    // TODO
    public boolean isFireRed()
    {
    	return false;
    }

}

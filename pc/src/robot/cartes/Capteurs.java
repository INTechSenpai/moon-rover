package robot.cartes;

import java.util.Arrays;

import robot.serial.Serial;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exceptions.serial.SerialException;

/**
 * Classe des capteurs, qui communique avec la carte capteur
 * @author PF
 */

public class Capteurs implements Service {

	// Dépendances
	private Log log;
	private Serial serie;
	private Read_Ini config;

	private boolean capteurs_on = true;

	private final int nb_capteurs_infrarouge_avant = 1;
//    private final int nb_capteurs_infrarouge_arriere = 0;
    private final int nb_capteurs_ultrason_avant = 1;
//    private final int nb_capteurs_ultrason_arriere = 0;
    
	public Capteurs(Read_Ini config, Log log, Serial serie)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		maj_config();
	}
	
	public void maj_config()
	{
		capteurs_on = Boolean.parseBoolean(config.get("capteurs_on"));
	}

	/**
	-	 * Retourne la valeur la plus optimiste des capteurs de type capteur dans 
	-	 * la direction voulue
	-	 * Par rapport à la fonction suivante, c'est mieux de renvoyer séparément 
	-	 * les données des capteurs qund c'est pas du même type.
	-	 * @param capteur (soit "ir", soit "us")
	-	 * @return la valeur la plus optimiste des capteurs
	-	 */
		public int mesurer_ultrason()
		{
			return mesurer("us_av", nb_capteurs_ultrason_avant);
		}

		public int mesurer_infrarouge()
		{
			return mesurer("ir_av", nb_capteurs_infrarouge_avant);
		}

		private int mesurer(String protocole, int nb)
		{
			if(!capteurs_on)
	    		return 3000;
			
			String[] distances_string;
			int[] distances;
			
			try{
				distances = new int[nb];
				distances_string = serie.communiquer(protocole, nb);

	    		for(int i = 0; i < nb; i++)
	    			distances[i] = Integer.parseInt(distances_string[i]);
	    		
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
    
	/**
	 * Retourne la valeur la plus optimiste des capteurs dans la direction voulue
	 * @param marche_arriere
	 * @return la valeur la plus optimiste des capteurs
	 */
    public int mesurer()
    {
    	if(!capteurs_on)
    		return 3000;
		String[] ultrasons;
		String[] infrarouges;
		int[] distances;
		
		try{
    		distances = new int[nb_capteurs_ultrason_avant+nb_capteurs_infrarouge_avant];
    		ultrasons = serie.communiquer("us_av", nb_capteurs_ultrason_avant);
    		infrarouges  = serie.communiquer("ir_av", nb_capteurs_infrarouge_avant);
    		for(int i = 0; i < nb_capteurs_ultrason_avant; i++)
    			distances[i] = Integer.parseInt(ultrasons[i]);
    		for(int i = 0; i < nb_capteurs_infrarouge_avant; i++)
    			distances[nb_capteurs_ultrason_avant+i] = Integer.parseInt(infrarouges[i]);
	    	
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
//    	log.debug(serie.communiquer("j", 1)[0], this);
    	try {
    		return Integer.parseInt(serie.communiquer("j", 1)[0]) != 0;
    	}
    	catch(Exception e)
    	{
    		log.critical("Aucune réponse du jumper", this);
    		return false;
    	}
    }
 
    public boolean isThereFireGauche()
    {
		try {
			return Integer.parseInt(serie.communiquer("cg", 1)[0]) != 0;
		} catch (NumberFormatException | SerialException e) {
			e.printStackTrace();
		}
		return false;
    }

    public boolean isThereFireMilieu()
    {
        
        try {
            return Integer.parseInt(serie.communiquer("cm", 1)[0]) != 0;
        } catch (NumberFormatException | SerialException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isThereFireDroit()
    {
		try {
			return Integer.parseInt(serie.communiquer("cd", 1)[0]) != 0;
		} catch (NumberFormatException | SerialException e) {
			e.printStackTrace();
		}
		return false;
    }

    // TODO protocoles
    public boolean isFireRedGauche()
    {
        // TODO
/*		try {
			return Integer.parseInt(serie.communiquer("ifr", 1)[0]) == 1;
		} catch (NumberFormatException | SerialException e) {
			e.printStackTrace();
		}*/
		return false;
    }

    public boolean isFireRedDroit()
    {
        // TODO
/*		try {
			return Integer.parseInt(serie.communiquer("ifr", 1)[0]) == 1;
		} catch (NumberFormatException | SerialException e) {
			e.printStackTrace();
		}*/
		return false;
    }

    
    
}

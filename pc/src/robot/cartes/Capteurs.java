package robot.cartes;

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

	public Capteurs(Read_Ini config, Log log, Serial serie)
	{
		this.log = log;
		this.config = config;
		this.serie = serie;
		updateConfig();
	}
	
	public void updateConfig()
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
		public int mesurer()
		{
			if(!capteurs_on)
	    		return 3000;
			
			String[] distances_string;
			int[] distances;
			
			try{

				distances = new int[1];
				distances_string = serie.communiquer("us", 1);

    			distances[0] = Integer.parseInt(distances_string[0]);
	    		
    		    return distances[0];
	    		
/*		    	Arrays.sort(distances); // le dernier élément d'un tableau trié par ordre croissant est le plus grand
		    	int distance = distances[distances.length-1];
		    	
		    	if(distance < 0)
		    		return 3000;
		    	return distance;*/
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

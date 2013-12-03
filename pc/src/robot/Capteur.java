package robot;

/**
 * Classe des capteurs, qui communique avec la carte capteur
 * @author PF
 */

public class Capteur {

    private int nb_capteurs_infrarouge_avant = 1;
    private int nb_capteurs_infrarouge_arriere = 1;
    private int nb_capteurs_ultrason_avant = 1;
    private int nb_capteurs_ultrason_arriere = 1;

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

package utils;

/**
 * Classe de callback. Contient la fonction et ses arguments à appeler.
 * @author pf
 */

public class Callback {

	private boolean done = false;
	private boolean unique;
	
	public Callback(/*fonction, arguments, unique*/) {}
	
	public void appeler()
	{
		if(!unique || !done)
		{
			//appelle de la fonction
			done = true;
		}
	}
	
}

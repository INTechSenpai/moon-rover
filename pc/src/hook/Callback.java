package hook;

/**
 * Classe de callback. Contient la fonction et ses arguments à appeler.
 * @author pf
 */

public class Callback {

	private boolean done = false;
	private boolean unique;
	private Executable methode;
	
	public Callback(Executable methode, boolean unique)
	{
		
	}
	
	public void appeler()
	{
		if(!unique || !done)
		{
			methode.execute();
			done = true;
		}
	}
	
}

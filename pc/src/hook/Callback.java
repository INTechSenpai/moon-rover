package hook;

/**
 * Classe de callback. Contient la fonction et ses arguments à appeler.
 * @author pf
 */

public class Callback {

	private boolean done = false;
	private boolean unique;
	public Executable methode;
	
	/**
	 * Constructeur d'un callback avec 2 paramètres: la méthode et si elle doit être exécutée une seule fois
	 * @param methode
	 * @param unique
	 */
	public Callback(Executable methode, boolean unique)
	{
		this.methode = methode;
		this.unique = unique;
	}
	
	/**
	 * Constructeur d'un callback avec 1 paramètre, la méthode. Par défaut, celle-ci est exécutée une seule fois.
	 * @param methode
	 */
	public Callback(Executable methode)
	{
		this.methode = methode;
		unique = true;
	}
	
	/**
	 * Le callback appelle la méthode, si elle n'est pas unique ou si elle n'est pas déjà faite
	 * @return
	 */
	public boolean appeler()
	{
		if(!(supprimable()))
		{
            done = true;
			return methode.execute();
		}
		return false;
	}
	
	public boolean supprimable()
	{
	    return unique && done;
	}
	
}

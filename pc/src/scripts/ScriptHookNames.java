package scripts;

/**
 * Les noms des scripts de hooks.
 * @author pf
 *
 */

public enum ScriptHookNames {
	EXEMPLE(false, 1),
	FUNNY_ACTION(false, 0);
	
	private boolean canIDoIt; // ce booléan dépend du robot!
	// si on a deux robots, ils ne pourront pas faire la même chose...
	
	// Le numéro du capteur qui déclenche ce script
	private int nbCapteur;
	
	ScriptHookNames(boolean canIDoIt, int nbCapteur)
	{
		this.canIDoIt = canIDoIt;
		this.nbCapteur = nbCapteur;
	}
	
	public int getNbCapteur()
	{
		return nbCapteur;
	}
	
	public boolean canIDoIt()
	{
		return canIDoIt;
	}

}

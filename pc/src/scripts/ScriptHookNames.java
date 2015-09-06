package scripts;

/**
 * Les noms des scripts de hooks.
 * @author pf
 *
 */

public enum ScriptHookNames {
	EXEMPLE(false, 1),
	FUNNY_ACTION(false, 0);
	
	public final boolean canIDoIt; // ce booléan dépend du robot!
	// si on a deux robots, ils ne pourront pas faire la même chose...
	
	// Le numéro du capteur qui déclenche ce script
	public final int nbCapteur;
	
	private ScriptHookNames(boolean canIDoIt, int nbCapteur)
	{
		this.canIDoIt = canIDoIt;
		this.nbCapteur = nbCapteur;
	}

}

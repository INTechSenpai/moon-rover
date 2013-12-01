package utils;

/**
 * Interface utilisée pour passer des méthodes à Callback.
 * Il faut créer une classe implémentant cette interface par méthode d'intérêt.
 * Il y a alors, dans l'implémentation, des attributs pour les arguments qui sont initialisés par le constructeur,
 * de manière à ce que execute reste sans argument.
 * @author pf
 */

public interface Executable {

	/**
	 * La méthode qui sera exécutée par le hook
	 */
	public void execute();
	
}

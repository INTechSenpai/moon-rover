
/**
 * Classe abstraite dont hériteront les différents scripts
 * @author pf
 */

abstract class Script {

	/**
	 * Exécute vraiment un script
	 */
	public void agit()
	{
		try
		{
			execute();
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
		finally
		{
			termine();
		}
		
	}
	
	/**
	 * Calcule le temps d'exécution de ce script (grâce à robotChrono)
	 * @return le temps d'exécution
	 */
	public int calcule()
	{
		return 0;
	}
	

	/**
	 * Renvoie le tableau des versions d'un script
	 * @return le tableau des versions possibles
	 */
	abstract int[] version();

	/**
	 * Retourne la position d'entrée associée à la version id
	 * @param id de la version
	 * @return la position du point d'entrée
	 */
	abstract Position point_entree(int id);
	
	/**
	 * Renvoie le score que peut fournir un script
	 * @return le score
	 */
	abstract int score();
	
	/**
 	 * Donne le poids du script, utilisé pour calculer sa note
	 * @return le poids
	 */
	abstract int poids();

	/**
	 * Exécute le script
	 */
	abstract protected void execute();

	/**
	 * Méthode toujours appelée à la fin du script (via un finally). Repli des actionneurs.
	 */
	abstract protected void termine();

}

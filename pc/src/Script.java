
/**
 * Classe abstraite dont hériteront les différents scripts
 * @author pf
 */

abstract class Script {

	public void agit()
	{
		
	}
	
	public int calcule()
	{
		return 0;
	}

	abstract void version();
	abstract void point_entree();
	abstract void score();
	abstract void poids();
	
}

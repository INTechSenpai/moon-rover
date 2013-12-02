package hook;

import smartMath.Vec2;

/**
 * Classe qui permet de gérer plus facilement les hooks. Service.
 * @author pf
 *
 */

public class HookGenerator {

	/**
	 * Retourne un hook de position suivant les paramètres donnés
	 * @param position
	 * @param tolerance (facultatif, par défaut tolerance de la config)
	 * @param effectuer_symetrie (facultatif, par défaut false)
	 * @return
	 */
	
	public Hook hook_position(Vec2 position, int tolerance, boolean effectuer_symetrie)
	{
		return new HookPosition(position, tolerance, effectuer_symetrie);
	}
	public Hook hook_position(Vec2 position, int tolerance)
	{
		return hook_position(position, tolerance, false);
	}
	public Hook hook_position(Vec2 position)
	{
		return hook_position(position, 50 /*remplacer par config*/, false);
	}
	public Hook hook_position(Vec2 position, boolean effectuer_symetrie)
	{
		return hook_position(position, 50 /*remplacer par config*/, effectuer_symetrie);
	}
	
}

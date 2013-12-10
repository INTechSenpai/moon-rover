package hook;

import container.Service;
import robot.serial.Serial;
import smartMath.Vec2;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe qui permet de gérer plus facilement les hooks. Service.
 * @author pf
 *
 */

public class HookGenerator implements Service {

	/**
	 * Retourne un hook de position suivant les paramètres donnés
	 * @param position
	 * @param tolerance (facultatif, par défaut tolerance de la config)
	 * @param effectuer_symetrie (facultatif, par défaut false)
	 * @return
	 */
	
	private Read_Ini config;
	private Log log;

	public HookGenerator(Service config, Service log)
	{
		this.config = (Read_Ini)config;
		this.log = (Log)log;
	}
	
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

package hook;

import container.Service;
import robot.cartes.Capteur;
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
	private Capteur capteur;

	private int tolerance_position = 20;
	
	public HookGenerator(Read_Ini config, Log log, Capteur capteur)
	{
		this.config = config;
		this.log = log;
		this.capteur = capteur;
		try {
		tolerance_position = Integer.parseInt(this.config.get("hooks_tolerance_mm"));
		}
		catch(Exception e)
		{
			log.warning(e, this);
		}
		
	}
	
	public Hook hook_position(Vec2 position, int tolerance, boolean effectuer_symetrie)
	{
		return new HookPosition(config, log, position, tolerance, effectuer_symetrie);
	}
	public Hook hook_position(Vec2 position, int tolerance)
	{
		return hook_position(position, tolerance, false);
	}
	public Hook hook_position(Vec2 position)
	{
		return hook_position(position, tolerance_position, false);
	}
	public Hook hook_position(Vec2 position, boolean effectuer_symetrie)
	{
		return hook_position(position, tolerance_position, effectuer_symetrie);
	}

	public Hook hook_feu()
	{
		return new HookFeu(config, log, capteur);
	}
	
}

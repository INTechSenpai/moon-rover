package hook;

import container.Service;
import exception.ConfigException;
import robot.Cote;
import robot.cartes.Capteurs;
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
	private Capteurs capteur;

	private int tolerance_position = 20;
	String couleur;
	
	public HookGenerator(Read_Ini config, Log log, Capteurs capteur)
	{
		this.config = config;
		this.log = log;
		this.capteur = capteur;
		try {
			couleur = config.get("couleur");
		} catch (ConfigException e1) {
			e1.printStackTrace();
		}
		try {
		tolerance_position = Integer.parseInt(this.config.get("hooks_tolerance_mm"));
		}
		catch(Exception e)
		{
			log.warning(e, this);
		}
		
	}

	/*
	 * Hook de position
	 */
	
	public Hook hook_position(Vec2 position, int tolerance)
	{
		return new HookPosition(config, log, position, tolerance, couleur=="rouge");
	}
	public Hook hook_position(Vec2 position)
	{
		return hook_position(position, tolerance_position);
	}
	
	/*
	 * Hook d'abscisse
	 */
	
	public Hook hook_abscisse(float abscisse, int tolerance)
	{
		return new HookAbscisse(config, log, abscisse, tolerance, couleur=="rouge");
	}
	public Hook hook_abscisse(float abscisse)
	{
		return hook_abscisse(abscisse, tolerance_position);
	}
	
	/*
	 * Hook de feu
	 */

	public Hook hook_feu(Cote cote)
	{
		return new HookFeu(config, log, capteur, cote);
	}

}

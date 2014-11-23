package hook.types;

import hook.Hook;
import container.Service;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Config;

/**
 * Classe qui permet de gérer plus facilement les hooks. Service.
 * @author pf
 *
 */

public class HookGenerator implements Service
{

	/**
	 * Retourne un hook de position suivant les paramètres donnés
	 * @param position
	 * @param tolerance (facultatif, par défaut tolerance de la config)
	 * @param effectuer_symetrie (facultatif, par défaut false)
	 * @return
	 */
	
	private Config config;
	private Log log;
	private GameState<RobotReal> real_state;

	private int tolerance_position = 20;
	String couleur;
	
	public HookGenerator(Config config, Log log, GameState<RobotReal> real_state)
	{
		this.config = config;
		this.log = log;
		this.real_state = real_state;
		updateConfig();
	}

	public void updateConfig()
	{
		couleur = config.get("couleur");
		tolerance_position = Integer.parseInt(this.config.get("hooks_tolerance_mm"));		
	}
	
	/*
	 * Hook de position
	 */
	
	public Hook hook_position(Vec2 position, int tolerance)
	{
		return new HookPosition(config, log, real_state, position, tolerance, couleur=="yellow");
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
		return new HookX(config, log, real_state, abscisse, tolerance, couleur=="yellow");
	}
	
	public Hook hook_abscisse(float abscisse)
	{
		return hook_abscisse(abscisse, tolerance_position);
	}
	
    public Hook hook_abscisse_droite(float abscisse, float tolerance)
    {
        if(couleur=="yellow")
            return new HookXisLesser(config, log, real_state, abscisse, tolerance, couleur=="yellow");
        return new HookXisGreater(config, log, real_state, abscisse, tolerance, couleur=="yellow");
    }

    public Hook hook_abscisse_gauche(float abscisse, float tolerance)
    {
        if(couleur=="yellow")
            return new HookXisGreater(config, log, real_state, abscisse, tolerance, couleur=="yellow");
        return new HookXisLesser(config, log, real_state, abscisse, tolerance, couleur=="yellow");
    }

    /*
     * Hook d'ordonnée
     */
    
    public Hook hook_ordonnee(float ordonnee, int tolerance)
    {
        return new HookY(config, log, real_state, ordonnee, tolerance);
    }
    public Hook hook_ordonnee(float ordonnee)
    {
        return hook_ordonnee(ordonnee, tolerance_position);
    }
    public Hook hook_ordonnee_haut(float ordonnee)
    {
        return new HookYisGreater(config, log, real_state, ordonnee, couleur=="yellow");
    }


}

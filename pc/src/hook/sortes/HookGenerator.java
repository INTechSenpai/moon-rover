package hook.sortes;

import hook.Hook;
import container.Service;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;

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
	
	private Read_Ini config;
	private Log log;
	private GameState<RobotVrai> real_state;

	private int tolerance_position = 20;
	String couleur;
	
	public HookGenerator(Read_Ini config, Log log, GameState<RobotVrai> real_state)
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
		return new HookPosition(config, log, real_state, position, tolerance, couleur=="rouge");
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
		return new HookAbscisse(config, log, real_state, abscisse, tolerance, couleur=="rouge");
	}
	
	public Hook hook_abscisse(float abscisse)
	{
		return hook_abscisse(abscisse, tolerance_position);
	}
	
    public Hook hook_abscisse_droite(float abscisse)
    {
        if(couleur=="rouge")
            return new HookAbscisseGauche(config, log, real_state, abscisse, couleur=="rouge");
        return new HookAbscisseDroite(config, log, real_state, abscisse, couleur=="rouge");
    }

    public Hook hook_abscisse_gauche(float abscisse)
    {
        if(couleur=="rouge")
            return new HookAbscisseDroite(config, log, real_state, abscisse, couleur=="rouge");
        return new HookAbscisseGauche(config, log, real_state, abscisse, couleur=="rouge");
    }

    /*
     * Hook d'ordonnée
     */
    
    public Hook hook_ordonnee(float ordonnee, int tolerance)
    {
        return new HookOrdonnee(config, log, real_state, ordonnee, tolerance, couleur=="rouge");
    }
    public Hook hook_ordonnee(float ordonnee)
    {
        return hook_ordonnee(ordonnee, tolerance_position);
    }
    public Hook hook_ordonnee_haut(float ordonnee)
    {
        return new HookOrdonneeHaut(config, log, real_state, ordonnee, couleur=="rouge");
    }


}

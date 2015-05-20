package robot;

import robot.stm.STMcard;
import utils.Log;
import utils.Config;
import utils.Sleep;
import utils.Vec2;
import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.methods.ThrowsChangeDirection;
import hook.types.HookDemiPlan;

import java.util.ArrayList;

import permissions.ReadOnly;
import planification.LocomotionArc;
import exceptions.ScriptHookException;
import exceptions.UnableToMoveException;

/**
 * Effectue le lien entre le code et la réalité (permet de parler aux stm, d'interroger les capteurs, etc.)
 * @author pf, marsu
 *
 */

public class RobotReal extends Robot
{
//	private Table table;
	private STMcard stm;
	
	private HookDemiPlan hookTrajectoireCourbe;

	// Constructeur
	public RobotReal(STMcard stm, Log log)
 	{
		super(log);
		this.stm = stm;
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	public void updateConfig(Config config)
	{
		super.updateConfig(config);
	}
	
	
	public void desactiver_asservissement_rotation()
	{
		stm.disableRotationalFeedbackLoop();
	}

	public void desactiver_asservissement_translation()
	{
		stm.disableTranslationalFeedbackLoop();
	}

	public void activer_asservissement_rotation()
	{
		stm.enableRotationalFeedbackLoop();
	}

	public void recaler()
	{
	    set_vitesse(Speed.READJUSTMENT);
	    stm.readjust();
	}
	
	/**
	 * Avance d'une certaine distance donnée en mm (méthode bloquante), gestion des hooks
	 * @throws UnableToMoveException 
	 * @throws FinMatchException 
	 * @throws ScriptHookException 
	 */
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException
	{
		// Il est nécessaire d'ajouter le hookFinMatch avant chaque appel de stm qui prenne un peu de temps (avancer, tourner, ...)
		hooks.add(hookFinMatch);
		stm.moveLengthwise(distance, hooks, mur);
	}	

	/**
	 * Modifie la vitesse de translation
	 * @param Speed : l'une des vitesses indexées dans enums.
	 * @throws FinMatchException 
	 * 
	 */
	@Override
	public void set_vitesse(Speed vitesse)
	{
		stm.setTranslationalSpeed(vitesse);
        stm.setRotationalSpeed(vitesse);
		log.debug("Modification de la vitesse: "+vitesse);
	}
	
	/*
	 * ACTIONNEURS
	 */
	
	/* 
	 * GETTERS & SETTERS
	 */
	@Override
	public void setPosition(Vec2<ReadOnly> position)
	{
	    stm.setPosition(position);
	}
	
    @Override
	public Vec2<ReadOnly> getPosition()
	{
	    return stm.getPosition();
	}
    
	@Override
	public void setOrientation(double orientation)
	{
	    stm.setOrientation(orientation);
	}

    @Override
    public double getOrientation()
    {
        return stm.getOrientation();
    }

    /**
	 * Méthode sleep utilisée par les scripts
     * @throws FinMatchException 
	 */
	@Override	
	public void sleep(long duree, ArrayList<Hook> hooks)
	{
		Sleep.sleep(duree);
	}

    @Override
    public void stopper()
    {
        try {
			stm.immobilise();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void tourner(double angle) throws UnableToMoveException
    {
    	ArrayList<Hook> hooks = new ArrayList<Hook>();
		hooks.add(hookFinMatch);
		stm.turn(angle, hooks);
    }
    
    @Override
    public void suit_chemin(ArrayList<LocomotionArc> chemin, ArrayList<Hook> hooks) throws UnableToMoveException, ScriptHookException
    {
		hooks.add(hookFinMatch);
        stm.followPath(chemin, hookTrajectoireCourbe, hooks, DirectionStrategy.getDefaultStrategy());
    }
    
	@Override
    public RobotChrono cloneIntoRobotChrono()
    {
    	RobotChrono rc = new RobotChrono(log);
    	copy(rc);
    	return rc;
    }
    
    // Cette copie est un peu plus lente que les autres car il y a un appel série
    // Néanmoins, on ne fait cette copie qu'une fois par arbre.
    @Override
    public void copy(RobotChrono rc)
    {
        super.copy(rc);
		Vec2.copy(getPosition(), rc.position);
		rc.orientation = getOrientation();
    }

	@Override
    public int getTempsDepuisDebutMatch()
    {
    	return (int)(System.currentTimeMillis() - dateDebutMatch);
    }
	
	public boolean isEnemyHere() {
		return stm.isEnemyHere(); // TODO: ne pas demander à déplacements mais à gridspace
	}
	
	public void closeSerialConnections()
	{
		stm.close();
	}

	public void initActuatorLocomotion()
	{
		// TODO (avec règlement)
	}

	public void setHookTrajectoireCourbe(HookDemiPlan hookTrajectoireCourbe)
	{
		Executable action = new ThrowsChangeDirection();
		hookTrajectoireCourbe.ajouter_callback(new Callback(action));
		this.hookTrajectoireCourbe = hookTrajectoireCourbe;
	}

}

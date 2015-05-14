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
import planification.astar.arc.SegmentTrajectoireCourbe;
import exceptions.ChangeDirectionException;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.SerialConnexionException;
import exceptions.UnableToMoveException;
import exceptions.WallCollisionDetectedException;

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
	public RobotReal(STMcard stm, Config config, Log log)
 	{
		super(config, log);
		this.stm = stm;
		updateConfig();
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	public void updateConfig()
	{
		super.updateConfig();
		stm.updateConfig();
		log.updateConfig();
	}
	
	
	public void desactiver_asservissement_rotation() throws FinMatchException
	{
		try {
			stm.disableRotationalFeedbackLoop();
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}
	}

	public void desactiver_asservissement_translation() throws FinMatchException
	{
		try {
			stm.disableTranslationalFeedbackLoop();
		} catch (SerialConnexionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void activer_asservissement_rotation() throws FinMatchException
	{
		try {
			stm.enableRotationalFeedbackLoop();
		} catch (SerialConnexionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void recaler() throws FinMatchException
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
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws UnableToMoveException, FinMatchException
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
	public void set_vitesse(Speed vitesse) throws FinMatchException
	{
        try {
			stm.setTranslationalSpeed(vitesse);
	        stm.setRotationalSpeed(vitesse);
		} catch (SerialConnexionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("Modification de la vitesse: "+vitesse);
	}
	
	/*
	 * ACTIONNEURS
	 */
	
	/* 
	 * GETTERS & SETTERS
	 */
	@Override
	public void setPosition(Vec2<ReadOnly> position) throws FinMatchException
	{
	    stm.setPosition(position);
	}
	
    @Override
	public Vec2<ReadOnly> getPosition() throws FinMatchException
	{
	    return stm.getPosition();
	}
    
	@Override
	public void setOrientation(double orientation) throws FinMatchException
	{
	    stm.setOrientation(orientation);
	}

    @Override
    public double getOrientation() throws FinMatchException
    {
        return stm.getOrientation();
    }

    /**
	 * Méthode sleep utilisée par les scripts
     * @throws FinMatchException 
	 */
	@Override	
	public void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException
	{
		Sleep.sleep(duree);
		for(Hook hook: hooks)
			try {
				hook.evaluate();
			} catch (ScriptHookException e) {
				// Impossible d'avoir des scripts hook pendant un sleep
				e.printStackTrace();
			} catch (WallCollisionDetectedException e) {
				// Impossible pendant un sleep
				e.printStackTrace();
			} catch (ChangeDirectionException e) {
				// Impossible pendant un sleep
				e.printStackTrace();
			}
	}

    @Override
    public void stopper() throws FinMatchException
    {
        try {
			stm.immobilise();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void tourner(double angle) throws UnableToMoveException, FinMatchException
    {
    	ArrayList<Hook> hooks = new ArrayList<Hook>();
		hooks.add(hookFinMatch);
		stm.turn(angle, hooks);
    }
    
    @Override
    public void suit_chemin(ArrayList<SegmentTrajectoireCourbe> chemin, ArrayList<Hook> hooks) throws UnableToMoveException, FinMatchException, ScriptHookException
    {
		hooks.add(hookFinMatch);
        stm.followPath(chemin, hookTrajectoireCourbe, hooks, DirectionStrategy.getDefaultStrategy());
    }
    
	@Override
    public RobotChrono cloneIntoRobotChrono()
    {
    	RobotChrono rc = new RobotChrono(config, log);
    	copy(rc);
    	return rc;
    }
    
    // Cette copie est un peu plus lente que les autres car il y a un appel série
    // Néanmoins, on ne fait cette copie qu'une fois par arbre.
    @Override
    public void copy(RobotChrono rc)
    {
        super.copy(rc);
        try {
			Vec2.copy(getPosition(), rc.position);
			rc.orientation = getOrientation();
		} catch (FinMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
    public int getTempsDepuisDebutMatch()
    {
    	return (int)(System.currentTimeMillis() - Config.getDateDebutMatch());
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

import SmartMath.Vec2;

/**
 *  Service de déplacements bas niveau. Méthodes non bloquantes.
 *  Pour les déplacements intelligents, voir RobotVrai
 * @author PF
 */

public class Deplacements {

	private int PWMmoteurGauche = 0;
	private int PWMmoteurDroit = 0;
	private int erreur_rotation = 0;
	private int erreur_translation = 0;
	private int derivee_erreur_rotation = 0;
	private int derivee_erreur_translation = 0;
	
	/**
	 * Constructeur
	 */
	public Deplacements()
	{
		
	}

	/**
	 * TODO
	 * @param PWMmoteurGauche
	 * @param PWMmoteurDroit
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @return
	 */
	public boolean gestion_blocage(int PWMmoteurGauche, int PWMmoteurDroit, int derivee_erreur_rotation, int derivee_erreur_translation)
	{
		return false;
	}

	/** 
	 * TODO
	 * @param erreur_rotation
	 * @param erreur_translation
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @return
	 */
	public boolean update_enMouvement(int erreur_rotation, int erreur_translation, int derivee_erreur_rotation, int derivee_erreur_translation)
	{
		return false;
	}
	
	/** 
	 * TODO
	 * Fait avancer le robot. Méthode non bloquante
	 * @param distance
	 */
	public void avancer(int distance)
	{
		
	}

	/** 
	 * TODO
	 * Fait tourner le robot. Méthode non bloquante
	 * @param angle
	 */
	public void tourner(int angle)
	{
		
	}
	
	/**
	 * TODO
	 * Arrête le robot
	 */
	public void stopper()
	{
		
	}
	
	/**
	 * Ecrase la position x du robot au niveau de la carte
	 * @param x
	 */
	public void set_x(int x)
	{
		
	}

	/**
	 * Ecrase la position y du robot au niveau de la carte
	 * @param y
	 */
	public void set_y(int y)
	{
		
	}
	
	/**
	 * Ecrase l'orientation du robot au niveau de la carte
	 * @param orientation
	 */
	public void set_orientation(int orientation)
	{
		
	}
	
	/**
	 * Active l'asservissement en translation du robot
	 */
	public void activer_asservissement_translation()
	{
		
	}

	/**
	 * Désactive l'asservissement en rotation du robot
	 */
	public void desactiver_asservissement_rotation()
	{
		
	}

	/**
	 * Désactive l'asservissement en translation du robot
	 */
	public void desactiver_asservissement_translation()
	{
		
	}

	/**
	 * Active l'asservissement en rotation du robot
	 */
	public void activer_asservissement_rotation()
	{
		
	}

	/**
	 * Modifie la vitesse en translation
	 * @param pwm_max
	 */
	public void set_vitesse_translation(int pwm_max)
	{
		
	}

	/**
	 * Modifie la vitesse en rotation
	 * @param pwm_max
	 */
	public void set_vitesse_rotation(int pwm_max)
	{
		
	}

	/**
	 * Met à jour PWMmoteurGauche, PWMmoteurDroit, erreur_rotation, erreur_translation, derivee_erreur_rotation, derivee_erreur_translation
	 */
	public void maj_infos_stoppage_enMouvement()
	{
		
	}

	/**
	 * 
	 * @return
	 */
	public Vec2 get_infos_x_y_orientation()
	{
		return new Vec2(0,0);
	}

	/**
	 * Arrêt de la série
	 */
	public void arret_final()
	{
		
	}
	
}

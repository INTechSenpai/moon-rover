package robot.cartes;

import java.util.Hashtable;

import robot.serial.Serial;
import utils.*;
import container.Service;

/**
 *  Service de déplacements bas niveau. Méthodes non bloquantes.
 *  Pour les déplacements intelligents, voir RobotVrai
 * @author PF
 */

public class Deplacements implements Service {

	// Dépendances
	private Log log;
	private Serial serie;

	private Hashtable<String, Integer> infos_stoppage_enMouvement;
		
	private long debut_timer_blocage;
	
    private boolean enCoursDeBlocage = false;

    /**
	 * Constructeur
	 */
	public Deplacements(Log log, Serial serie)
	{
		this.log = log;
		this.serie = serie;
		
		infos_stoppage_enMouvement = new Hashtable<String, Integer>();
		infos_stoppage_enMouvement.put("PWMmoteurGauche", 0);
		infos_stoppage_enMouvement.put("PWMmoteurDroit", 0);
		infos_stoppage_enMouvement.put("erreur_rotation", 0);
		infos_stoppage_enMouvement.put("erreur_translation", 0);
		infos_stoppage_enMouvement.put("derivee_erreur_rotation", 0);
		infos_stoppage_enMouvement.put("derivee_erreur_translation", 0);

	}

	/**
	 * Renvoie vrai si le robot bloque (c'est-à-dire que les moteurs forcent mais que le robot ne bouge pas). Blocage automatique au bout de 500ms
	 * @param PWMmoteurGauche
	 * @param PWMmoteurDroit
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @return
	 */
	public boolean gestion_blocage(int PWMmoteurGauche, int PWMmoteurDroit, int derivee_erreur_rotation, int derivee_erreur_translation)
	{
		boolean blocage = false;
		boolean moteur_force = Math.abs(PWMmoteurGauche) > 40 || Math.abs(PWMmoteurDroit) > 40;
		boolean bouge_pas = derivee_erreur_rotation == 0 && derivee_erreur_translation == 0;

		if(bouge_pas && moteur_force)
		{
			if(enCoursDeBlocage)
			{
				if(System.currentTimeMillis() - debut_timer_blocage > 500)
				{
					log.warning("le robot a dû s'arrêter suite à un patinage.", this);
					stopper();
					blocage = true;
				}
			}
			else
			{
				debut_timer_blocage = System.currentTimeMillis();
				enCoursDeBlocage  = true;
			}
		}
		else
			enCoursDeBlocage = false;

		return blocage;
		
	}

	/** 
	 * Utilisé uniquement par le thread de mise à jour. Regarde si le robot bouge effectivement.
	 * @param erreur_rotation
	 * @param erreur_translation
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @return
	 */
	public boolean update_enMouvement(int erreur_rotation, int erreur_translation, int derivee_erreur_rotation, int derivee_erreur_translation)
	{
		boolean rotation_stoppe = Math.abs(erreur_rotation) < 105;
		boolean translation_stoppe = Math.abs(erreur_translation) < 100;
		boolean bouge_pas = Math.abs(derivee_erreur_rotation) < 100 && Math.abs(derivee_erreur_translation) < 100;

		return !(rotation_stoppe && translation_stoppe && bouge_pas);
	}
	
	/** 
	 * Fait avancer le robot. Méthode non bloquante
	 * @param distance
	 */
	public void avancer(int distance)
	{
		String chaines[] = {"d", Integer.toString(distance)};
		serie.communiquer(chaines, 0);
	}

	/** 
	 * Fait tourner le robot. Méthode non bloquante
	 * @param angle
	 */
	public void tourner(int angle)
	{
		String chaines[] = {"t", Integer.toString(angle)};
		serie.communiquer(chaines, 0);		
	}
	
	/**
	 * Arrête le robot
	 */
	public void stopper()
	{
		serie.communiquer("stop", 0);
	}
	
	/**
	 * Ecrase la position x du robot au niveau de la carte
	 * @param x
	 */
	public void set_x(int x)
	{
		String chaines[] = {"cx", Integer.toString(x)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Ecrase la position y du robot au niveau de la carte
	 * @param y
	 */
	public void set_y(int y)
	{
		String chaines[] = {"cy", Integer.toString(y)};
		serie.communiquer(chaines, 0);	
	}
	
	/**
	 * Ecrase l'orientation du robot au niveau de la carte
	 * @param orientation
	 */
	public void set_orientation(int orientation)
	{
		String chaines[] = {"co", Integer.toString(orientation)};
		serie.communiquer(chaines, 0);
	}
	
	/**
	 * Active l'asservissement en translation du robot
	 */
	public void activer_asservissement_translation()
	{
		serie.communiquer("ct1", 0);
	}

	/**
	 * Désactive l'asservissement en rotation du robot
	 */
	public void desactiver_asservissement_rotation()
	{
		serie.communiquer("cr1", 0);
	}

	/**
	 * Désactive l'asservissement en translation du robot
	 */
	public void desactiver_asservissement_translation()
	{
		serie.communiquer("ct0", 0);
	}

	/**
	 * Active l'asservissement en rotation du robot
	 */
	public void activer_asservissement_rotation()
	{
		serie.communiquer("cr0", 0);
	}

	/**
	 * Modifie la vitesse en translation
	 * @param pwm_max
	 */
	public void set_vitesse_translation(int pwm_max)
	{
		double kp, kd;
		if(pwm_max > 120)
		{
			kp = 0.8;
			kd = 22.0;
		}
		else if(pwm_max > 55)
		{
			kp = 0.8;
			kd = 16.0;
		}
		else
		{
			kp = 0.6;
			kd = 10.0;
		}
		
		String chaines[] = {"ctv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);			
	}

	/**
	 * Modifie la vitesse en rotation
	 * @param pwm_max
	 */
	public void set_vitesse_rotation(int pwm_max)
	{
		double kp, kd;
		if(pwm_max > 155)
		{
			kp = 1.0;
			kd = 23.0;
		}
		else if(pwm_max > 90)
		{
			kp = 1.0;
			kd = 19.0;
		}
		else
		{
			kp = 0.8;
			kd = 15.0;
		}
		
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Met à jour PWMmoteurGauche, PWMmoteurDroit, erreur_rotation, erreur_translation, derivee_erreur_rotation, derivee_erreur_translation
	 */
	public Hashtable<String, Integer> maj_infos_stoppage_enMouvement()
	{
		String[] infos_string = serie.communiquer("?info", 4);
		int[] infos_int = new int[4];

		for(int i = 0; i < 4; i++)
			infos_int[i] = Integer.parseInt(infos_string[i]);
		
		int deriv_erreur_rot = infos_int[2] - infos_stoppage_enMouvement.get("erreur_rotation");
		int deriv_erreur_tra = infos_int[3] - infos_stoppage_enMouvement.get("erreur_translation");
		
        infos_stoppage_enMouvement.put("PWMmoteurGauche", infos_int[0]);
        infos_stoppage_enMouvement.put("PWMmoteurDroit", infos_int[1]);
        infos_stoppage_enMouvement.put("erreur_rotation", infos_int[2]);
        infos_stoppage_enMouvement.put("erreur_translation", infos_int[3]);
        infos_stoppage_enMouvement.put("derivee_erreur_rotation", deriv_erreur_rot);
        infos_stoppage_enMouvement.put("derivee_erreur_translation", deriv_erreur_tra);

	return infos_stoppage_enMouvement;
	}

	/**
	 * Renvoie x, y et orientation du robot
	 * @return un tableau de 3 cases: [x, y, orientation]
	 */
	public int[] get_infos_x_y_orientation()
	{
		String[] infos_string = serie.communiquer("?xyo", 3);
		int[] infos_int = new int[3];

		for(int i = 0; i < 3; i++)
			infos_int[i] = Integer.parseInt(infos_string[i]);

		return infos_int;
	}

	/**
	 * Arrêt de la série
	 */
	public void arret_final()
	{
		serie.close();
	}
	
}

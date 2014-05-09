package robot.cartes;

import java.util.Hashtable;

import robot.serial.Serial;
import utils.*;
import container.Service;
import exceptions.deplacements.BlocageException;
import exceptions.serial.SerialException;

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
	
	public void maj_config()
	{
	}	
	
	/**
	 * Renvoie vrai si le robot bloque (c'est-à-dire que les moteurs forcent mais que le robot ne bouge pas). Blocage automatique au bout de 500ms
	 * @param PWMmoteurGauche
	 * @param PWMmoteurDroit
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @throws BlocageException 
	 */
	public void gestion_blocage() throws BlocageException
	{
		int PWMmoteurGauche = infos_stoppage_enMouvement.get("PWMmoteurGauche");
		int PWMmoteurDroit = infos_stoppage_enMouvement.get("PWMmoteurDroit");
		int derivee_erreur_rotation = infos_stoppage_enMouvement.get("derivee_erreur_rotation");
		int derivee_erreur_translation = infos_stoppage_enMouvement.get("derivee_erreur_translation");
		
		boolean moteur_force = Math.abs(PWMmoteurGauche) > 40 || Math.abs(PWMmoteurDroit) > 40;
		boolean bouge_pas = derivee_erreur_rotation == 0 && derivee_erreur_translation == 0;

		if(bouge_pas && moteur_force)
		{
			if(enCoursDeBlocage)
			{
                // la durée de tolérance au patinage est fixée ici 
				if(System.currentTimeMillis() - debut_timer_blocage > 200)
				{
					log.warning("le robot a dû s'arrêter suite à un patinage.", this);
					try {
						stopper();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					throw new BlocageException();
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

	}

	/** 
	 * Regarde si le robot bouge effectivement.
	 * @param erreur_rotation
	 * @param erreur_translation
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @return
	 */
	public boolean update_enMouvement()
	{
		int erreur_rotation = infos_stoppage_enMouvement.get("erreur_rotation");
		int erreur_translation = infos_stoppage_enMouvement.get("erreur_translation");
		int derivee_erreur_rotation = infos_stoppage_enMouvement.get("derivee_erreur_rotation");
		int derivee_erreur_translation = infos_stoppage_enMouvement.get("derivee_erreur_translation");
		
		boolean rotation_stoppe = Math.abs(erreur_rotation) < 105;
		boolean translation_stoppe = Math.abs(erreur_translation) < 100;
		boolean bouge_pas = Math.abs(derivee_erreur_rotation) < 100 && Math.abs(derivee_erreur_translation) < 100;

		return !(rotation_stoppe && translation_stoppe && bouge_pas);
	}
	
	/** 
	 * Fait avancer le robot. Méthode non bloquante
	 * @param distance
	 */
	public void avancer(double distance) throws SerialException
	{
		String chaines[] = {"d", Double.toString(distance)};
		serie.communiquer(chaines, 0);
	}

	/** 
	 * Fait tourner le robot. Méthode non bloquante
	 * @param angle
	 */
	public void tourner(double angle) throws SerialException
	{
		String chaines[] = {"t", Double.toString(angle)};
		serie.communiquer(chaines, 0);		
	}
	
	/**
	 * Arrête le robot
	 */
	public void stopper() throws SerialException
	{
        desactiver_asservissement_translation();
        desactiver_asservissement_rotation();
		serie.communiquer("stop", 0);
        activer_asservissement_translation();
        activer_asservissement_rotation();
	}
	
	/**
	 * Ecrase la position x du robot au niveau de la carte
	 * @param x
	 */
	public void set_x(int x) throws SerialException
	{
		String chaines[] = {"cx", Integer.toString(x)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Ecrase la position y du robot au niveau de la carte
	 * @param y
	 */
	public void set_y(int y) throws SerialException
	{
		String chaines[] = {"cy", Integer.toString(y)};
		serie.communiquer(chaines, 0);	
	}
	
	/**
	 * Ecrase l'orientation du robot au niveau de la carte
	 * @param orientation
	 */
	public void set_orientation(double orientation) throws SerialException
	{
		String chaines[] = {"co", Double.toString(orientation)};
		serie.communiquer(chaines, 0);
	}
	
	/**
	 * Active l'asservissement en translation du robot
	 */
	public void activer_asservissement_translation() throws SerialException
	{
		serie.communiquer("ct1", 0);
	}

	/**
	 * Active l'asservissement en rotation du robot
	 */
	public void activer_asservissement_rotation() throws SerialException
	{
		serie.communiquer("cr1", 0);
	}

	/**
	 * Désactive l'asservissement en translation du robot
	 */
	public void desactiver_asservissement_translation() throws SerialException
	{
		serie.communiquer("ct0", 0);
	}

	/**
	 * Désactive l'asservissement en rotation du robot
	 */
	public void desactiver_asservissement_rotation() throws SerialException
	{
		serie.communiquer("cr0", 0);
	}

	/**
	 * Modifie la vitesse en translation
	 * @param pwm_max
	 */
	public void set_vitesse_translation(int pwm_max) throws SerialException
	{
		double kp, kd;
		if(pwm_max >= 170)
		{
			kp = 0.45;
			kd = 12.5;
		}
		else if(pwm_max >= 150)
		{
			kp = 0.45;
			kd = 11.5;
		}
		else if(pwm_max >= 120)
		{
			kp = 0.45;
			kd = 9.0;
		}
		else if(pwm_max >= 90)
		{
			kp = 0.45;
			kd = 12.5;
		}
		else if(pwm_max >= 60)
		{
			kp = 0.5;
			kd = 4.0;
		}
		else
		{
			kp = 1.15;
			kd = 3.0;
		}
		
		String chaines[] = {"ctv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);			
	}

	/**
	 * Modifie la vitesse en rotation
	 * @param pwm_max
	 */
	public void set_vitesse_rotation(int pwm_max) throws SerialException
	{
		double kp, kd;
		if(pwm_max > 155)
		{
			kp = 1.0;
			kd = 35.0;
		}
		else if(pwm_max > 115)
		{
			kp = 0.85;
			kd = 20.0;
		}
		else if(pwm_max > 90)
		{
			kp = 0.8;
			kd = 15.0;
		}
		else
		{
			kp = 0.6;
			kd = 14.0;
		}
		
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}
	
	public void change_const_translation(double kp, double kd, int pwm_max) throws SerialException
	{
		String chaines[] = {"ctv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}
	
	public void change_const_rotation(double kp, double kd, int pwm_max) throws SerialException
	{
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Met à jour PWMmoteurGauche, PWMmoteurDroit, erreur_rotation, erreur_translation, derivee_erreur_rotation, derivee_erreur_translation
	 */
	public void maj_infos_stoppage_enMouvement() throws SerialException
	{
		String[] infos_string = serie.communiquer("?infos", 4);
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

	}

	/**
	 * Renvoie x, y et orientation du robot
	 * @return un tableau de 3 cases: [x, y, orientation]
	 */
	public double[] get_infos_x_y_orientation() throws SerialException
	{
		String[] infos_string = serie.communiquer("?xyo", 3);
		double[] infos_double = new double[3];
		
		for(int i = 0; i < 3; i++)
		    infos_double[i] = Double.parseDouble(infos_string[i]);

		return infos_double;
	}

	/**
	 * Arrêt de la série
	 */
	public void arret_final()
	{
		serie.close();
	}
	
}

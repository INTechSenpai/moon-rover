package robot.cards;

import java.util.Hashtable;

import robot.serial.SerialConnexion;
import utils.*;
import container.Service;
import exceptions.Locomotion.BlockedException;
import exceptions.serial.SerialConnexionException;

/**
 *  Service de déplacements bas niveau. Méthodes non bloquantes.
 *  Pour les déplacements intelligents, voir RobotVrai
 * @author PF
 */

public class Locomotion implements Service
{

	// Dépendances
	private Log log;
	private SerialConnexion serie;

	private Hashtable<String, Integer> infos_stoppage_enMouvement;
		
	private long debut_timer_blocage;
	
    private boolean enCoursDeBlocage = false;

    /**
	 * Constructeur
	 */
	public Locomotion(Log log, SerialConnexion serie)
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
	
	public void updateConfig()
	{
	}	
	
	/**
	 * Renvoie vrai si le robot bloque (c'est-à-dire que les moteurs forcent mais que le robot ne bouge pas). Blocage automatique au bout de 500ms
	 * @param PWMmoteurGauche
	 * @param PWMmoteurDroit
	 * @param derivee_erreur_rotation
	 * @param derivee_erreur_translation
	 * @throws BlockedException 
	 */
	public void leverExeptionSiPatinage() throws BlockedException
	{
		int PWMmoteurGauche = infos_stoppage_enMouvement.get("PWMmoteurGauche");
		int PWMmoteurDroit = infos_stoppage_enMouvement.get("PWMmoteurDroit");
		int derivee_erreur_rotation = infos_stoppage_enMouvement.get("derivee_erreur_rotation");
		int derivee_erreur_translation = infos_stoppage_enMouvement.get("derivee_erreur_translation");
		
		// on décrète que les moteurs forcent si la puissance qu'ils demandent est trop grande
		boolean moteur_force = Math.abs(PWMmoteurGauche) > 40 || Math.abs(PWMmoteurDroit) > 40;
		
		// on décrète que le robot est immobile si l'écart entre la position demandée et la position actuelle est constant
		boolean bouge_pas = Math.abs(derivee_erreur_rotation) <= 10 && Math.abs(derivee_erreur_translation) <= 10;

		// si on patine
		if(bouge_pas && moteur_force)
		{
			// si on patinais déja auparavant, on fait remonter le patinage au code de haut niveau (via BlocageExeption)
			if(enCoursDeBlocage)
			{
                // la durée de tolérance au patinage est fixée ici (200ms)
				// mais cette fonction n'étant appellée qu'a une fréquance de l'ordre du Hertz ( la faute a une saturation de la série)
				// le robot mettera plus de temps a réagir ( le temps de réaction est égal au temps qui sépare 2 appels successifs de cette fonction)
				if((System.currentTimeMillis() - debut_timer_blocage) > 200)
				{
					log.warning("le robot a dû s'arrêter suite à un patinage.", this);
					try {
						stopper();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					throw new BlockedException();
				}
			}

			// si on détecte pour la première fois le patinage, on continue de forcer
			else
			{
				debut_timer_blocage = System.currentTimeMillis();
				enCoursDeBlocage  = true;
			}
		}
		// si tout va bien
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
	public boolean isRobotMoving()
	{
		// obtient les infos de l'asservissement
		int erreur_rotation = infos_stoppage_enMouvement.get("erreur_rotation");
		int erreur_translation = infos_stoppage_enMouvement.get("erreur_translation");
		int derivee_erreur_rotation = infos_stoppage_enMouvement.get("derivee_erreur_rotation");
		int derivee_erreur_translation = infos_stoppage_enMouvement.get("derivee_erreur_translation");
		
		// ces 2 booléens checkent la précision de l'asser. Ce n'est pas le rôle de cette fonction, 
		// et peut causer des bugs (erreurs d'aquitement) de java si l'asser est mla fait
		/*
		System.out.println("erreur_rotation : "+erreur_rotation);
		System.out.println("erreur_translation : "+erreur_translation);
		System.out.println("derivee_erreur_rotation': "+derivee_erreur_rotation);
		System.out.println("derivee_erreur_translation': "+derivee_erreur_translation);
		*/
		
		//donc, on vire !
		// VALEURS A REVOIR
		boolean rotation_stoppe = Math.abs(erreur_rotation) <= 60;
		boolean translation_stoppe = Math.abs(erreur_translation) <= 60;
		boolean bouge_pas = Math.abs(derivee_erreur_rotation) <= 20 && Math.abs(derivee_erreur_translation) <= 20;
		return !(rotation_stoppe && translation_stoppe && bouge_pas);
	}
	
	/** 
	 * Fait avancer le robot. Méthode non bloquante
	 * @param distance
	 */
	public void avancer(double distance) throws SerialConnexionException
	{
		String chaines[] = {"d", Double.toString(distance)};
		serie.communiquer(chaines, 0);
	}

	/** 
	 * Fait tourner le robot. Méthode non bloquante
	 * @param angle
	 */
	public void turn(double angle) throws SerialConnexionException
	{
		String chaines[] = {"t", Double.toString(angle)};
		serie.communiquer(chaines, 0);		
	}
	
	/**
	 * Arrête le robot
	 */
	public void stopper() throws SerialConnexionException
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
	public void set_x(int x) throws SerialConnexionException
	{
		String chaines[] = {"cx", Integer.toString(x)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Ecrase la position y du robot au niveau de la carte
	 * @param y
	 */
	public void set_y(int y) throws SerialConnexionException
	{
		String chaines[] = {"cy", Integer.toString(y)};
		serie.communiquer(chaines, 0);	
	}
	
	/**
	 * Ecrase l'orientation du robot au niveau de la carte
	 * @param orientation
	 */
	public void set_orientation(double orientation) throws SerialConnexionException
	{
		String chaines[] = {"co", Double.toString(orientation)};
		serie.communiquer(chaines, 0);
	}
	
	/**
	 * Active l'asservissement en translation du robot
	 */
	public void activer_asservissement_translation() throws SerialConnexionException
	{
		serie.communiquer("ct1", 0);
	}

	/**
	 * Active l'asservissement en rotation du robot
	 */
	public void activer_asservissement_rotation() throws SerialConnexionException
	{
		serie.communiquer("cr1", 0);
	}

	/**
	 * Désactive l'asservissement en translation du robot
	 */
	public void desactiver_asservissement_translation() throws SerialConnexionException
	{
		serie.communiquer("ct0", 0);
	}

	/**
	 * Désactive l'asservissement en rotation du robot
	 */
	public void desactiver_asservissement_rotation() throws SerialConnexionException
	{
		serie.communiquer("cr0", 0);
	}

	/**
	 * Modifie la vitesse en translation
	 * @param pwm_max
	 */
	public void set_vitesse_translation(int pwm_max) throws SerialConnexionException
	{
		double kp, kd;
		if(pwm_max >= 195)
		{
			kp = 0.55;
			kd = 27.0;
		}
		else if(pwm_max >= 165)
		{
			kp = 0.52;
			kd = 17.0;
		}
		else if(pwm_max >= 145)
		{
			kp = 0.52;
			kd = 17.0;
		}
		else if(pwm_max >= 115)
		{
			kp = 0.45;
			kd = 12.0;
		}
		else if(pwm_max >= 85)
		{
			kp = 0.45;
			kd = 12.5;
		}
		else if(pwm_max >= 55)
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
	public void set_vitesse_rotation(int pwm_max) throws SerialConnexionException
	{
		double kp, kd;
		if(pwm_max > 155)
		{
			kp = 2.0;
			kd = 50.0;
		}
		else if(pwm_max > 115)
		{
			kp = 0.85;
			kd = 25.0;
		}
		else if(pwm_max > 85)
		{
			kp = 1.0;
			kd = 15.0;
		}
		else
		{
			kp = 2.0;
			kd = 14.0;
		}
		
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}
	
	public void change_const_translation(double kp, double kd, int pwm_max) throws SerialConnexionException
	{
		String chaines[] = {"ctv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}
	
	public void change_const_rotation(double kp, double kd, int pwm_max) throws SerialConnexionException
	{
		String chaines[] = {"crv", Double.toString(kp), Double.toString(kd), Integer.toString(pwm_max)};
		serie.communiquer(chaines, 0);
	}

	/**
	 * Met à jour PWMmoteurGauche, PWMmoteurDroit, erreur_rotation, erreur_translation, derivee_erreur_rotation, derivee_erreur_translation
	 * les nouvelles valeurs sont stokées dans la map
	 */
	public void maj_infos_stoppage_enMouvement() throws SerialConnexionException
	{
		// on envois "?infos" et on lis les 4 int (dans l'ordre : PWM droit, PWM gauche, erreurRotation, erreurTranslation)
		String[] infos_string = serie.communiquer("?infos", 4);
		int[] infos_int = new int[4];
		for(int i = 0; i < 4; i++)
			infos_int[i] = Integer.parseInt(infos_string[i]);
		
		// calcul des dérivées des erreurs en translation et en rotation :
		// on fait la différence entre la valeur actuelle de l'erreur et le valeur précédemment mesurée.
		// on divise par un dt unitaire (non mentionné dans l'expression)
		int deriv_erreur_rot = infos_int[2] - infos_stoppage_enMouvement.get("erreur_rotation");
		int deriv_erreur_tra = infos_int[3] - infos_stoppage_enMouvement.get("erreur_translation");
		
		
		// infos_stoppage_enMouvement est une map dont les clés sont des strings et les valeurs des int
		
		// on stocke la puissance consommée par les moteurs
        infos_stoppage_enMouvement.put("PWMmoteurGauche", infos_int[0]);
        infos_stoppage_enMouvement.put("PWMmoteurDroit", infos_int[1]);
        
        // l'erreur de translation mesurée par les codeuses
        infos_stoppage_enMouvement.put("erreur_rotation", infos_int[2]);
        infos_stoppage_enMouvement.put("erreur_translation", infos_int[3]);
        
        // stocke les dérivées des erreurs, calculés 10 lignes plus haut
        infos_stoppage_enMouvement.put("derivee_erreur_rotation", deriv_erreur_rot);
        infos_stoppage_enMouvement.put("derivee_erreur_translation", deriv_erreur_tra);

        
	}

	/**
	 * Renvoie x, y et orientation du robot
	 * @return un tableau de 3 cases: [x, y, orientation]
	 */
	public double[] get_infos_x_y_orientation() throws SerialConnexionException
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

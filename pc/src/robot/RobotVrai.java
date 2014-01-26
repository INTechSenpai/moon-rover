package robot;

import robot.cartes.Actionneurs;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import hook.Hook;
import hook.HookGenerator;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Hashtable;

import exception.BlocageException;
import exception.CollisionException;
import exception.MouvementImpossibleException;

/**
 * Classe qui fournit des déplacements haut niveau
 * @author pf
 *
 */

public class RobotVrai extends Robot {

	protected Capteurs capteur;
	protected Actionneurs actionneurs;
	protected Deplacements deplacements;
	protected HookGenerator hookgenerator;
	protected Table table;

	private Vec2 consigne = new Vec2(0,0);
	private float orientation_consigne = (float)-Math.PI/2;
	
	private boolean blocage = false;
//	private boolean enMouvement = true;
	
	private boolean marche_arriere = false;
	private boolean effectuer_symetrie = true;
	
	public boolean pret = false;
	
	private int sleep_milieu_boucle_acquittement;
	private int largeur_robot;
	private int distance_detection;
	
	private float maj_ancien_angle;
//	private boolean maj_marche_arriere;
	private int disque_tolerance_consigne;
	private int distance_degagement_robot;
	private float angle_degagement_robot;
	private boolean correction_trajectoire;
	
	// Constructeur
	
	public RobotVrai(Capteurs capteur, Actionneurs actionneurs, Deplacements deplacements, HookGenerator hookgenerator, Table table, Read_Ini config, Log log)
 	{
		super(config, log);
		this.capteur = capteur;
		this.actionneurs = actionneurs;
		this.deplacements = deplacements;
		this.hookgenerator =  hookgenerator;
		this.table = table;
		
		try
		{
			largeur_robot = Integer.parseInt(config.get("largeur_robot"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
		try
		{
			distance_detection = Integer.parseInt(config.get("distance_detection"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
		try
		{
			disque_tolerance_consigne = Integer.parseInt(config.get("disque_tolerance_consigne"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
		try
		{
			distance_degagement_robot = Integer.parseInt(config.get("distance_degagement_robot"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
		try
		{
			sleep_milieu_boucle_acquittement = Integer.parseInt(config.get("sleep_milieu_boucle_acquittement"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
		try
		{
			angle_degagement_robot = Float.parseFloat(config.get("angle_degagement_robot"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
		try
		{
			correction_trajectoire = Boolean.parseBoolean(config.get("correction_trajectoire"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
		update_x_y_orientation();
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	// TODO
	public void recaler()
	{
		
	}
	
	/**
	 * Arrête le robot
	 * @param avec_blocage
	 */
	@Override
	public void stopper(boolean avec_blocage)
	{
		log.debug("Arrêt du robot", this);
		if(avec_blocage)
			blocage = true;
		deplacements.stopper();			
	}
	
	/**
	 * Arrête le robot
	 */
	@Override
	public void stopper()
	{
		stopper(true);
	}

	/**
	 * Modifie la consigne en angle, de façon non bloquante
	 * @param angle
	 */
/*	private void correction_angle(float angle)
	{
		orientation_consigne = angle;
		deplacements.tourner((int)angle);
	}
*/
	
	/**
	 * Avance d'une certaine distance (méthode bloquante), gestion des hooks
	 */
	@Override
	public void avancer(int distance, ArrayList<Hook> hooks, int nbTentatives, boolean retenterSiBlocage, boolean sansLeverException) throws MouvementImpossibleException
	{
		log.debug("Avancer de "+Integer.toString(distance), this);

		boolean memoire_effectuer_symetrie = effectuer_symetrie;
		effectuer_symetrie = false;

		if(distance < 0)
			marche_arriere = true;

		Vec2 consigne = new Vec2(0,0);
		consigne.x = (float) (position.x + distance*Math.cos(orientation_consigne));
		consigne.y = (float) (position.y + distance*Math.sin(orientation_consigne));
		
		try {
			va_au_point(consigne, hooks, nbTentatives, retenterSiBlocage, sansLeverException);
		}
		catch(MouvementImpossibleException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			log.warning("Avancer a catché: "+e, this);
		}
		finally
		{
			effectuer_symetrie = memoire_effectuer_symetrie;
			marche_arriere = false;
		}
		
	}
	
	/**
	 * Fait tourner le robot (méthode bloquante)
	 * @throws MouvementImpossibleException 
	 */
	@Override
	public void tourner(float angle, ArrayList<Hook> hooks, int nombre_tentatives, boolean sans_lever_exception) throws MouvementImpossibleException
	{
		if(effectuer_symetrie)
		{
			if(couleur == "rouge")
				angle = (float)Math.PI - angle;
			log.debug("Tourne à "+Float.toString(angle)+" (symétrie effectuée)", this);
		}
		else
			log.debug("Tourne à "+Float.toString(angle)+" (sans symétrie)", this);

		try{
			tournerBasNiveau(angle, hooks, sans_lever_exception);
		}
		catch(BlocageException e)
		{
			try
			{
				if(nombre_tentatives > 0)
				{
					log.warning("Blocage en rotation ! On tourne dans l'autre sens... reste "+Integer.toString(nombre_tentatives)+" tentative(s)", this);
					if(angle < 0)
						tourner(orientation + angle_degagement_robot, null, nombre_tentatives-1, sans_lever_exception);
				}
			}
			finally
			{
				if(!sans_lever_exception)
					throw new MouvementImpossibleException();
			}
		}
		catch(CollisionException e)
		{
			stopper();
			throw new MouvementImpossibleException();
		}
	}
	

	/**
	 * Fait suivre au robot un chemin (fourni par la recherche de chemin)
	 * @throws MouvementImpossibleException 
	 */
	@Override
	protected void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks, boolean retenter_si_blocage, boolean symetrie_effectuee) throws MouvementImpossibleException
	{
		for(Vec2 position: chemin)
			va_au_point(position, hooks, false, 2, retenter_si_blocage, symetrie_effectuee, false);
	}


	/**
	 * Le robot va au point demandé
	 */
	@Override
	protected void va_au_point(Vec2 point, ArrayList<Hook> hooks, boolean trajectoire_courbe, int nombre_tentatives, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean sans_lever_exception) throws MouvementImpossibleException
	{
		// appliquer la symétrie ne doit pas modifier ce point !
		point = point.clone();
		
		// symetrie_effectuee est important. En effet, sans cela, et puisque va_au_point s'appelle elle-même, la symétrie serait une bascule!
		// Ce qui n'est pas le comportement souhaité.
		if(effectuer_symetrie && !symetrie_effectuee)
		{
			if(couleur == "rouge")
				point.x *= -1;
			log.debug("Va au point "+point+" (symétrie vérifiée pour le "+couleur+"), virage initial: "+Boolean.toString(trajectoire_courbe), this);
		}
		else
			log.debug("Va au point "+point+" (sans symétrie pour la couleur), virage initial: "+Boolean.toString(trajectoire_courbe), this);

		try
		{
			va_au_pointBasNiveau(point, hooks, trajectoire_courbe, sans_lever_exception);
		}
		catch(BlocageException e) // blocage durant le mouvement
		{
			try
			{
				stopper();
				if(retenter_si_blocage)
				{
					log.warning("Blocage en déplacement ! On recule... reste "+Integer.toString(nombre_tentatives)+" tentatives", this);
					if(marche_arriere)
						avancer(distance_degagement_robot, nombre_tentatives-1);
					else
					avancer(-distance_degagement_robot, nombre_tentatives-1);
				}
			}
			finally
			{
				if(!sans_lever_exception)
					throw new MouvementImpossibleException(this);
			}
		}
		catch(CollisionException e) // détection d'un robot adverse
		{
			stopper();
			if(nombre_tentatives > 0)
			{
				log.warning("attente avant nouvelle tentative... reste "+Integer.toString(nombre_tentatives)+" tentative(s)", this);
				sleep(1000);
				va_au_point(point, hooks, trajectoire_courbe, nombre_tentatives-1, retenter_si_blocage, true, sans_lever_exception);
			}
			else if(!sans_lever_exception)
				throw new MouvementImpossibleException(this);
		}
	
	}

	/**
	 * Modifie la vitesse de translation
	 */
	@Override
	public void set_vitesse_translation(String vitesse)
	{
		int pwm_max = conventions_vitesse_translation(vitesse);
		deplacements.set_vitesse_translation(pwm_max);
		log.debug("Modification de la vitesse de translation: "+vitesse, this);
	}

	/**
	 * Modifie la vitesse de rotation
	 */
	@Override
	public void set_vitesse_rotation(String vitesse)
	{
		int pwm_max = conventions_vitesse_rotation(vitesse);
		deplacements.set_vitesse_rotation(pwm_max);
		log.debug("Modification de la vitesse de rotation: "+vitesse, this);
	}
	
	public void update_x_y_orientation()
	{
		float[] infos = deplacements.get_infos_x_y_orientation();
		synchronized(position)
		{
			position.x = infos[0];
			position.y = infos[1];
		}
		orientation = infos[2]/1000; // car get_infos renvoie des milliradians		
	}

	/*
	 * ACTIONNEURS
	 */

	// TODO
	public void initialiser_actionneurs_deplacements()
	{
		deplacements.activer_asservissement_rotation();
		deplacements.activer_asservissement_translation();
		actionneurs.rateau_ranger_droit();
		actionneurs.rateau_ranger_gauche();		
	}

	@Override
	public void tirerBalles()
	{
		// TODO
		nombre_lances--;
	}
	
	@Override
	public void takefire() {
//		boolean retourner = capteur.isFireRed() ^ couleur == "rouge";
		// TODO
		
	}
	
	@Override
	public void bac_bas()
	{
		actionneurs.bac_bas();
	}

	@Override
	public void bac_haut()
	{
		actionneurs.bac_haut();
	}

	@Override
	public void rateau(PositionRateau position, Cote cote)
	{
		if(position == PositionRateau.BAS && cote == Cote.DROIT)
			actionneurs.rateau_bas_droit();
		else if(position == PositionRateau.BAS && cote == Cote.GAUCHE)
			actionneurs.rateau_bas_gauche();
		else if(position == PositionRateau.HAUT && cote == Cote.DROIT)
			actionneurs.rateau_haut_droit();
		else if(position == PositionRateau.HAUT && cote == Cote.GAUCHE)
			actionneurs.rateau_haut_gauche();
		else if(position == PositionRateau.RANGER && cote == Cote.DROIT)
			actionneurs.rateau_ranger_droit();
		else if(position == PositionRateau.RANGER && cote == Cote.GAUCHE)
			actionneurs.rateau_ranger_gauche();
		else if(position == PositionRateau.SUPER_BAS && cote == Cote.DROIT)
			actionneurs.rateau_super_bas_droit();
		else if(position == PositionRateau.SUPER_BAS && cote == Cote.GAUCHE)
			actionneurs.rateau_super_bas_gauche();
	}

	@Override
	public void deposer_fresques() {
		fresques_posees = true;
	}

	/* 
	 * GETTERS & SETTERS
	 */
	
	@Override
	public void setPosition(Vec2 position) {
		synchronized(this.position)
		{
			this.position = position;
		}
		deplacements.set_x((int)position.x);
		deplacements.set_y((int)position.y);
	}

	@Override
	public void setOrientation(float orientation) {
		this.orientation = orientation;
		orientation_consigne = orientation;
		deplacements.set_orientation(orientation);
	}

	/*
	 * MÉTHODES PRIVÉES
	 */

/*	private void avancerBasNiveau(int distance) throws CollisionException, BlocageException
	{
		Vec2 consigne = new Vec2(0,0);
		consigne.x = (float) (this.position.x + distance*Math.cos(this.orientation_consigne));
		consigne.y = (float) (this.position.y + distance*Math.sin(this.orientation_consigne));
		this.va_au_pointBasNiveau(consigne);
	}*/

	private void tournerBasNiveau(float angle, ArrayList<Hook> hooks, boolean sans_lever_exception) throws BlocageException, CollisionException
	{
		blocage = false;
		orientation_consigne = angle;
		deplacements.tourner(angle);
		
		while(!acquittement(true, sans_lever_exception))
		{
			if(hooks != null)
				for(Hook hook : hooks)
					hook.evaluate(this);
			sleep(sleep_milieu_boucle_acquittement);
		}
	}
	
	private void tournerBasNiveau(float angle) throws BlocageException, CollisionException
	{
		tournerBasNiveau(angle, null, false);
	}

	/**
     * Méthode pour parcourir un segment : le robot se rend en (x,y) en corrigeant dynamiquement ses consignes en rotation et translation.
     * Si le paramètre trajectoire_courbe=False, le robot évite d'effectuer un virage, et donc tourne sur lui meme avant la translation.
     * Les hooks sont évalués, et une boucle d'acquittement générique est utilisée.
	 * @param position
	 * @param hooks
	 * @param trajectoire_courbe
	 * @param sans_lever_exception
	 * @throws BlocageException 
	 */
	private void va_au_pointBasNiveau(Vec2 point, ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean sans_lever_exception) throws CollisionException, BlocageException
	{
        // comme à toute consigne initiale de mouvement, le robot est débloqué
		blocage = false;

		// mise en place d'un point consigne, à atteindre (en attribut pour persister dans _mise_a_jour_consignes() )
		consigne = point.clone();

		Vec2 delta = consigne.clone();
		update_x_y_orientation();
		delta.Minus(position);
		float distance = delta.Length();
		
        //gestion de la marche arrière du déplacement (peut aller à l'encontre de marche_arriere)
		float angle = (float) Math.atan2(delta.y, delta.x);

//		maj_marche_arriere = marche_arriere;
		maj_ancien_angle = angle;
		
		if(marche_arriere)
		{
			distance *= -1;
			angle += Math.PI;
		}
		
		if(!trajectoire_courbe)
		{
            // sans virage : la première rotation est blocante
			tournerBasNiveau(angle);
			// on n'avance pas si un obstacle est devant
			detecter_collision();
			deplacements.avancer(distance);
		}
		else
		{
			orientation_consigne = angle;
			deplacements.tourner(angle);
			// on n'avance pas si un obstacle est devant
			detecter_collision();
			deplacements.avancer(distance);			
		}
		
		while(!acquittement(true, sans_lever_exception))
		{
			if(hooks != null)
				for(Hook hook : hooks)
					hook.evaluate(this);
			if(correction_trajectoire)
				mise_a_jour_consignes();
			sleep(sleep_milieu_boucle_acquittement);
		}
		
	}
	
	private void mise_a_jour_consignes()
	{
		Vec2 delta = consigne.clone();
		delta.Minus(position);
		float distance = delta.Length();
		
        //gestion de la marche arrière du déplacement (peut aller à l'encontre de marche_arriere)
		float angle = (float) Math.atan2(delta.y, delta.x);
		float delta_angle = angle - maj_ancien_angle;

		if(delta_angle > Math.PI)
				delta_angle -= 2*Math.PI;
		else if(delta_angle <= Math.PI)
			delta_angle += 2*Math.PI;
		
		maj_ancien_angle = angle;

		// inversement de la marche si la destination n'est plus devant
//		if(Math.abs(delta_angle) > Math.PI/2)
//			maj_marche_arriere = !maj_marche_arriere;

		// mise à jour des consignes en translation et rotation en dehors d'un disque de tolérance
		if(distance > disque_tolerance_consigne)
		{
			// déplacement selon la marche
/*			if(maj_marche_arriere)
			{
				distance *= -1;
				angle += Math.PI;
			}
*/
			// L'attribut orientation_consigne doit être mis à jour à chaque deplacements.tourner() pour le fonctionnement de avancerBasNiveau()
			orientation_consigne = angle;
			deplacements.tourner(angle);
			deplacements.avancer(distance);
		}
	}

	private void detecter_collision() throws CollisionException
	{
		int signe = 1;
//		if(marche_arriere)
//			signe = -1;
		int rayon_detection = largeur_robot + distance_detection/2;
		Vec2 centre_detection = new Vec2((float)(signe * rayon_detection * Math.cos(orientation)), (float)(signe * rayon_detection * Math.sin(orientation)));
		centre_detection.Plus(position);

		if(table.obstaclePresent(centre_detection, distance_detection/2))
		{
			log.warning("Ennemi détecté!", this);
			throw new CollisionException();
		}
	}

	/**
	 * Boucle d'acquittement générique. Retourne des valeurs spécifiques en cas d'arrêt anormal (blocage, capteur)
	 * @param detection_collision
	 * @param sans_lever_exception
	 * @return oui si le robot est arrivé à destination, non si encore en mouvement
	 * @throws BlocageException
	 * @throws CollisionException
	 */
	private boolean acquittement(boolean detection_collision, boolean sans_lever_exception) throws BlocageException, CollisionException
	{
        // récupérations des informations d'acquittement
		Hashtable<String, Integer> infos = deplacements.maj_infos_stoppage_enMouvement();
		
        //robot bloqué ?
        //deplacements.gestion_blocage() n'indique qu'un NOUVEAU blocage : garder le ou logique avant l'ancienne valeur (attention aux threads !)
		if(blocage || deplacements.gestion_blocage(infos))
		{
			blocage = true;
			if(!sans_lever_exception)
				throw new BlocageException(this);
		}
		
		// ennemi détecté devant le robot?
		if(detection_collision)
			detecter_collision();
		
		// robot arrivé?
		if(!deplacements.update_enMouvement(infos))
			return true;

		// robot encore en mouvement
		return false;
	}

	/**
	 * Méthode sleep utilisée par les scripts
	 */
	public void sleep(long duree)
	{
//		log.debug("Sleep de "+duree+" ms", this);
		try {
		Thread.sleep(duree);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}


	/**
	 * Retourne un booléen indiquant si la marche arrière fait gagner du temps pour atteindre le point consigne. 
     * On évite ainsi d'implémenter une marche arrière automatique et on laisse la main aux scripts.
	 * @param position
	 * @return
	 */
/*
	private boolean marche_arriere_est_plus_rapide(Vec2 consigne, float orientation_finale_voulue)
	{
		// appliquer la symétrie ne doit pas modifier ce point !
		consigne = consigne.clone();
		
		if(orientation_finale_voulue == -1000)
			orientation_finale_voulue = orientation;
		else if(effectuer_symetrie && couleur == "rouge")
			orientation_finale_voulue = (float)Math.PI - orientation_finale_voulue;
		
		if(effectuer_symetrie && couleur == "rouge")
		{
			consigne.x *= -1;
		}
		
		Vec2 delta = consigne.Minus(position);
		Vec2 orientationVec = new Vec2((float)Math.cos(orientation), (float)Math.sin(orientation));

		// On regarde le produit scalaire; si c'est positif, alors on est dans le bon sens, et inversement
		return delta.dot(orientationVec) > 0;
		
	}

	private boolean marche_arriere_est_plus_rapide(Vec2 consigne)
	{
		return marche_arriere_est_plus_rapide(consigne, -1000);
	}
*/

	/**
	 * Appelée par des exceptions
	 */
	public void annuleConsigneOrientation()
	{
		orientation_consigne = orientation;
	}
	
}

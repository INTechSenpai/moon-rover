package robot;

import robot.cartes.Actionneurs;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import smartMath.Vec2;
import table.Colour;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;
import hook.Hook;
import hook.HookGenerator;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import exception.BlocageException;
import exception.CollisionException;
import exception.MouvementImpossibleException;
import exception.SerialException;

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

	private int sleep_boucle_acquittement;
	private int largeur_robot;
	private int distance_detection;
	
	private float maj_ancien_angle;
	private boolean maj_marche_arriere;
	private int disque_tolerance_consigne;
	private int distance_degagement_robot;
	private float angle_degagement_robot;
	private boolean correction_trajectoire;
	private int pwm_max_translation;
	private int distance_securite_trajectoire_courbe;
	private boolean autorise_trajectoire_courbe;
	
	private boolean obstacleImprevuDevantCapteur;
	private float obstacleImprevuDevantCapteurDeathDate;
	
	// Constructeur
	public RobotVrai(Capteurs capteur, Actionneurs actionneurs, Deplacements deplacements, HookGenerator hookgenerator, Table table, Read_Ini config, Log log)
 	{
		super(config, log);
		this.capteur = capteur;
		this.actionneurs = actionneurs;
		this.deplacements = deplacements;
		this.hookgenerator =  hookgenerator;
		this.table = table;
		obstacleImprevuDevantCapteur = false;
		maj_config();
		this.set_vitesse_rotation("entre_scripts");
		this.set_vitesse_translation("entre_scripts");
		
		try {
			update_x_y_orientation();
		} catch (SerialException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	public void maj_config()
	{
		super.maj_config();
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
			sleep_boucle_acquittement = Integer.parseInt(config.get("sleep_boucle_acquittement"));
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
			distance_securite_trajectoire_courbe = Integer.parseInt(config.get("distance_securite_trajectoire_courbe"));
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
		try
		{
			autorise_trajectoire_courbe = Boolean.parseBoolean(config.get("autorise_trajectoire_courbe"));
		}
		catch(Exception e)
		{
			log.critical(e, this);
		}
	}

	// La couleur est simulée. Normalement, vu la disposition des couleurs, cela devrait se faire assez bien.
	public Colour getColour(Cote cote)
	{
/*		boolean rouge;
		if(cote == Cote.GAUCHE)
			rouge = capteur.isFireRedGauche();
		else
			rouge = capteur.isFireRedDroit();
		if(rouge)
			return Colour.RED;
		else
			return Colour.YELLOW;*/

		float orientation_utilisee;

		// TODO vérifier que c'est bien la largeur
		Vec2 avant_robot = new Vec2((float)(largeur_robot/2 * Math.cos(orientation)), (float)(largeur_robot/2 * Math.sin(orientation)));
		avant_robot.Plus(position);
		
		int i = table.nearestUntakenFire(avant_robot.clone());
		float distance = table.distanceFire(avant_robot.clone(), i);

		// Si on est plus à 5cm de la position normale debout, c'est que le feu est tombé.
		// Dans ce cas, on regarde où on a vu le feu, et on en déduit de quel côté il est tombé.
		if(distance > 50)
			orientation_utilisee = table.angleFire(avant_robot.clone(), i);
		// Sinon, c'est que le feu est encore debout. On regarde alors de quel côté on vient.
		else
			orientation_utilisee = orientation;
		
		Colour a_priori = table.getFireColour(i);
		if(orientation_utilisee >= Math.PI/4 && orientation_utilisee <= 5*Math.PI/4)
			return a_priori;
		else
			return inverserCouleur(a_priori);
	}
	
	private Colour inverserCouleur(Colour colour)
	{
		if(colour == Colour.RED)
			return Colour.YELLOW;
		else
			return Colour.RED;
	}
	
	// Un début, on va voir si ça marche
	public void recaler()
	{

		try {
			deplacements.set_vitesse_translation(50);
			deplacements.set_vitesse_rotation(80);


			avancer(-200, false, true);
			position.x = 1500 - 165;
			if(couleur == "rouge")
			{
				deplacements.set_x(-1500+165);
				setOrientation(0f);
			}
			else
			{
				deplacements.set_x(1500-165);
				setOrientation((float)Math.PI);
			}
			sleep(500);
			avancer(45, false, true);	// toujours pas d'exeption, car on ne sait toujours pas ou on est sur la map
			tourner(-(float)Math.PI/2, null, true);
			avancer(-600, false, true);
			position.y = 2000 - 165;
			deplacements.set_y(2000 - 165);
			sleep(500);
			avancer(100);
			setOrientation((float)(-Math.PI)/2);
			//Normalement on se trouve à (1500 - 170 - 70 = 1260 ; 2000 - 170 - 100 = 1730)
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		try {
			deplacements.stopper();
		} catch (SerialException e) {
			e.printStackTrace();
		}			
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
	protected void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean trajectoire_courbe) throws MouvementImpossibleException
	{
		Iterator<Vec2> i = chemin.iterator();
		while(i.hasNext())
		{
			Vec2 point = i.next();
			va_au_point(point, hooks, trajectoire_courbe, nb_tentatives, retenter_si_blocage, symetrie_effectuee, false, false/*i.hasNext()*/);
		}
	}


	/**
	 * Le robot va au point demandé
	 */
	@Override
	protected void va_au_point(Vec2 point, ArrayList<Hook> hooks, boolean trajectoire_courbe, int nombre_tentatives, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean sans_lever_exception, boolean enchainer) throws MouvementImpossibleException
	{
		try {
			update_x_y_orientation();
		} catch (SerialException e1) {
			e1.printStackTrace();
		}
		
		// Si trajectoire_courbe a été donné en true, cela signifie que va_au_point prend lui-même la décision
		// Là où on prendra plus de place, c'est devant le robot (marche avant)
		if(autorise_trajectoire_courbe && trajectoire_courbe)
			trajectoire_courbe = !table.obstaclePresent(position.PlusNewVector(new Vec2(50*(float)Math.cos(orientation),50*(float)Math.sin(orientation))), distance_securite_trajectoire_courbe);
		else
			trajectoire_courbe = false;
		
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
			va_au_pointBasNiveau(point, hooks, trajectoire_courbe, sans_lever_exception, enchainer);
		}
		catch(BlocageException e) // blocage durant le mouvement
		{
			try
			{
				stopper();
				if(retenter_si_blocage)
				{
					// TODO gérer nombre_tentatives = 0
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
				va_au_point(point, hooks, trajectoire_courbe, nombre_tentatives-1, retenter_si_blocage, true, sans_lever_exception, false);
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
		pwm_max_translation = conventions_vitesse_translation(vitesse);
		try {
			deplacements.set_vitesse_translation(pwm_max_translation);
		} catch (SerialException e) {
			e.printStackTrace();
		}
		log.debug("Modification de la vitesse de translation: "+vitesse, this);
	}

	/**
	 * Modifie la vitesse de rotation
	 */
	@Override
	public void set_vitesse_rotation(String vitesse)
	{
		int pwm_max = conventions_vitesse_rotation(vitesse);
		try {
			deplacements.set_vitesse_rotation(pwm_max);
		} catch (SerialException e) {
			e.printStackTrace();
		}
		log.debug("Modification de la vitesse de rotation: "+vitesse, this);
	}
	
	public void update_x_y_orientation() throws SerialException
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
		try {
			deplacements.activer_asservissement_rotation();
			deplacements.activer_asservissement_translation();
			actionneurs.rateau_ranger_droit();
			actionneurs.rateau_ranger_gauche();		
			actionneurs.bac_bas();
		} catch (SerialException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void tirerBalle() throws SerialException
	{
		log.debug("On lance une balle!", this);
		actionneurs.tirerBalle();
		nombre_lances--;
	}

	@Override
	public void takefire(Cote cote) throws SerialException, MouvementImpossibleException {

		if(!isTient_feu(cote))
		{
			int signe = 1;
			if(cote == Cote.GAUCHE)
				signe = -1;
			String vitesse_rotation = get_vitesse_rotation();
			stopper();
			avancer(-150);
			ouvrir_bas_pince(cote);
			tourner_relatif(signe*0.2f);
			sleep(600);
			avancer(120);
			presque_fermer_pince(cote);
			set_vitesse_rotation("prise_feu");
			tourner_relatif(-signe*0.3f);
			set_vitesse_rotation(vitesse_rotation);
			avancer(30);
			fermer_pince(cote);
			sleep(500);
			lever_pince(cote);
			sleep(500);
			setTient_feu(cote);
			setFeu_tenu_rouge(cote, getColour(cote));
			// On signale à la table qu'on a prit un feu. A priori, c'est le plus proche de cette position.
			table.pickFire(table.nearestUntakenFire(position.clone()));
		}
	}

	@Override
	public void bac_bas() throws SerialException
	{
		actionneurs.bac_bas();
	}

	@Override
	public void bac_haut() throws SerialException
	{
		actionneurs.bac_haut();
	}

	@Override
	public void rateau(PositionRateau position, Cote cote) throws SerialException
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

	@Override	
	public void lancerFilet() throws SerialException
	{
		stopper();
		deplacements.desactiver_asservissement_rotation();
		deplacements.desactiver_asservissement_translation();
		actionneurs.lancerFilet();
	}
	@Override
	public void milieu_pince(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.milieu_pince_gauche();
		else
			actionneurs.milieu_pince_droite();
	}
	@Override
	public void baisser_pince(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.baisser_pince_gauche();
		else
			actionneurs.baisser_pince_droite();
	}	
	@Override	
	public void lever_pince(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.lever_pince_gauche();
		else
			actionneurs.lever_pince_droite();
	}

	@Override
	public void ouvrir_pince(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.ouvrir_pince_gauche();
		else
			actionneurs.ouvrir_bas_pince_droite();
	}
	@Override	
	public void fermer_pince(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.fermer_pince_gauche();
		else
			actionneurs.fermer_pince_droite();
	}
	@Override
	public void presque_fermer_pince(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.presque_fermer_pince_gauche();
		else
			actionneurs.presque_fermer_pince_droite();
	}
	@Override
	public void ouvrir_bas_pince(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.ouvrir_bas_pince_gauche();
		else
			actionneurs.ouvrir_bas_pince_droite();
	}
	@Override
	public void tourner_pince(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.tourner_pince_gauche();
		else
			actionneurs.tourner_pince_droite();
	}
	
	@Override	
	public void poserFeuBonCote(Cote cote) throws SerialException
	{
		log.debug("On pose le feu gauche sans le retourner", this);
		//Ca remonte la pince aussi !
		milieu_pince(cote);
		sleep(1000);
		ouvrir_pince(cote);
		sleep(1000);
		lever_pince(cote);
		sleep(1000);
		fermer_pince(cote);
		sleep(1000);
	}

	@Override	
	public void poserFeuEnRetournant(Cote cote) throws SerialException
	{
		log.debug("On pose le feu gauche en le retournant", this);
		//Ca remonte la pince aussi !
		baisser_pince(cote);
		tourner_pince(cote);
		ouvrir_pince(cote);
		lever_pince(cote);
		fermer_pince(cote);
	}

	
	/* 
	 * GETTERS & SETTERS
	 */
	@Override
	public void setPosition(Vec2 position) {
		synchronized(this.position)
		{
			this.position = position;
			try {
				deplacements.set_x((int)position.x);
				deplacements.set_y((int)position.y);
			} catch (SerialException e) {
				e.printStackTrace();
			}
			sleep(300);
		}
	}

	@Override
	public void setOrientation(float orientation) {
		this.orientation = orientation;
		orientation_consigne = orientation;
		try {
			deplacements.set_orientation(orientation);
		} catch (SerialException e) {
			e.printStackTrace();
		}
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
		boolean relancer = false;
		
		try {
			deplacements.tourner(angle);
		} catch (SerialException e) {
			e.printStackTrace();
		}
		
		while(!acquittement(false, sans_lever_exception))
		{
			if(hooks != null)
				for(Hook hook : hooks)
					relancer |= hook.evaluate(this);
			if(relancer)
				break;
			sleep(sleep_boucle_acquittement);
		}
		
		// Si un hook a bougé le robot, le dernier ordre est relancé après son exécution
		if(relancer)
			tournerBasNiveau(angle, hooks, sans_lever_exception);

	}
	
	
/*	private void tournerBasNiveau(float angle) throws BlocageException, CollisionException
	{
		tournerBasNiveau(angle, null, false);
	}*/

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
	private void va_au_pointBasNiveau(Vec2 point, ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean sans_lever_exception, boolean enchainer) throws CollisionException, BlocageException
	{
		boolean relancer = false;
		
        // comme à toute consigne initiale de mouvement, le robot est débloqué
		blocage = false;

		// mise en place d'un point consigne, à atteindre (en attribut pour persister dans _mise_a_jour_consignes() )
		consigne = point.clone();

		Vec2 delta = consigne.clone();
		try {
			update_x_y_orientation();
		} catch (SerialException e) {
			e.printStackTrace();
		}
		delta.Minus(position);
		float distance = delta.Length();
		
        //gestion de la marche arrière du déplacement (peut aller à l'encontre de marche_arriere)
		float angle = (float) Math.atan2(delta.y, delta.x);

		maj_marche_arriere = marche_arriere;
		maj_ancien_angle = angle;
		
		if(marche_arriere)
		{
			distance *= -1;
			angle += Math.PI;
		}
		
		if(!trajectoire_courbe)
		{
            // sans virage : la première rotation est blocante
			tournerBasNiveau(angle, null, sans_lever_exception);
			
			
			
			
			// on n'avance pas si un obstacle est devant
			
			//Probleme : recalage utilise ceci avant que la position du robot  ne soit connue. Donc uniquement si !sans_lever_exception
			if(!sans_lever_exception)
				detecter_collision();

			try {
				deplacements.avancer(distance);
			} catch (SerialException e) {
				e.printStackTrace();
			}
		}
		else
		{
			orientation_consigne = angle;
			try {
				deplacements.tourner(angle);
				// on n'avance pas si un obstacle est devant
				detecter_collision();
				deplacements.avancer(distance);			
			} catch (SerialException e) {
				e.printStackTrace();
			}
		}
		
		float distance_restante_carre = 5000;
		
		//meme soucis ici que pour le premier detecter_collision, acquittement ne doit pas vérifier les collisions si on lui demande justement de ne pas lever d'exeption.
		while((!acquittement(!sans_lever_exception, sans_lever_exception) && !enchainer) || (enchainer && distance_restante_carre > 1000))
		{
			if(hooks != null)
				for(Hook hook : hooks)
					relancer |= hook.evaluate(this);

			if(relancer)
				break;

			// Si on utilise la trajectoire courbe, on doit nécessairement utiliser la correction de trajectoire.
			// update_x_y_orientation() est déjà appelé dans mise_a_jour_consignes
			if(correction_trajectoire || trajectoire_courbe)
				mise_a_jour_consignes();
			else
				try {
					update_x_y_orientation();
				} catch (SerialException e) {
					e.printStackTrace();
				}
			sleep(sleep_boucle_acquittement);
			Vec2 delta_restant = consigne.clone();
			delta_restant.Minus(position);
			distance_restante_carre = delta_restant.SquaredLength();

		}
		
//		log.debug("Distance restante: "+Math.sqrt(distance_restante_carre), this);
		
		// Si un hook a bougé le robot, le dernier ordre est relancé après son exécution
		if(relancer)
		{
			log.debug("Un hook a bougé le robot, on le relance vers "+point, this);
			va_au_pointBasNiveau(point, hooks, trajectoire_courbe, sans_lever_exception, enchainer);
		}
		
	}
	
	private void mise_a_jour_consignes()
	{
		/*
		 * Tonton PF, raconte-nous une histoire!
		 * C'est l'histoire d'une routine qui remet le robot sur le bon chemin. Pour cela, elle prend la position du robot, la position de la consigne, et calcule l'angle et la distance entre les deux.
		 * Pourtant, la routine arrive toujours trop loin! Pourquoi? Parce que le temps que la série lui donne la position du robot, celui-ci a déjà réduit la distance entre lui et la consigne.
		 * La routine, ne le sachant pas, surestime donc la distance...
		 * Morale de l'histoire: ne faîtes pas confiance à la série. Et corriger cette erreur en extrapolant la position future du robot connaissant sa vitesse actuelle.
		 */
		
		long t1 = System.currentTimeMillis(); 

		try {
			update_x_y_orientation();
		} catch (SerialException e1) {
			e1.printStackTrace();
		}

		Vec2 delta = consigne.clone();
		delta.Minus(position);
		
        //gestion de la marche arrière du déplacement (peut aller à l'encontre de marche_arriere)
		float angle = (float) Math.atan2(delta.y, delta.x);
		float delta_angle = angle - maj_ancien_angle;

		if(delta_angle > Math.PI)
			delta_angle -= 2*Math.PI;
		else if(delta_angle <= -Math.PI)
			delta_angle += 2*Math.PI;
		
		maj_ancien_angle = angle;

		// inversement de la marche si la destination n'est plus devant
		if(Math.abs(delta_angle) > Math.PI/2)
			maj_marche_arriere = !maj_marche_arriere;

		long t2 = System.currentTimeMillis(); 
		
		float distance_entretemps = ((float)2500)/((float)613.52 * (float)(Math.pow((double)pwm_max_translation,(double)(-1.034))))/1000*(t2-t1);
		float distance = delta.Length() - distance_entretemps;

		log.debug("Correction trajectoire,  distance: "+distance, this);

		// mise à jour des consignes en translation et rotation en dehors d'un disque de tolérance
		if(distance > disque_tolerance_consigne)
		{
			// déplacement selon la marche
			if(maj_marche_arriere)
			{
				distance *= -1;
				angle += Math.PI;
			}

			// L'attribut orientation_consigne doit être mis à jour à chaque deplacements.tourner() pour le fonctionnement de avancerBasNiveau()
			orientation_consigne = angle;
			try {
				deplacements.tourner(angle);
				deplacements.avancer(distance);
			} catch (SerialException e) {
				e.printStackTrace();
			}
		}
	}

	// fonction vérifiant que l'on ne va pas taper dans le robot adverse. 
	// Renvois true si ennemi droit devant.
	// @param devant : fait la détection derrière le robot si l'on avance a reculons 
	private void detecter_collision(boolean devant) throws CollisionException
	{
		int signe = -1;
		if(devant)
			signe = 1;
		
		// was largeur_robot + distance_detection/2;
		int rayon_detection = largeur_robot/2 + distance_detection;
		Vec2 centre_detection = new Vec2((float)(signe * rayon_detection * Math.cos(orientation)), (float)(signe * rayon_detection * Math.sin(orientation)));
		centre_detection.Plus(position);

		if(table.obstaclePresent(centre_detection, distance_detection/2))
		{
			log.warning("Ennemi détecté en : " + centre_detection.x + "; " + centre_detection.y, this);
			throw new CollisionException();
		}
		if(isObstacleImprevuDevantCapteur())
		{
			log.warning("Obstacle capteur droit devant !", this);
			throw new CollisionException();
		}
			
	}
	
	private void detecter_collision() throws CollisionException
	{
		detecter_collision(!marche_arriere);
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
		Hashtable<String, Integer> infos;
		try {
				infos = deplacements.maj_infos_stoppage_enMouvement();
			
	        //robot bloqué ?
	        //deplacements.gestion_blocage() n'indique qu'un NOUVEAU blocage : garder le ou logique avant l'ancienne valeur (attention aux threads !)
			if(blocage || deplacements.gestion_blocage(infos))
			{
				blocage = true;
				if(!sans_lever_exception)
					throw new BlocageException(this);
				return true;
			}
			
			// ennemi détecté devant le robot?
			if(detection_collision)
				detecter_collision();
			
			// robot arrivé?
			if(!deplacements.update_enMouvement(infos))
				return true;

		} catch (SerialException e) {
			e.printStackTrace();
		}

		// robot encore en mouvement
		return false;
	}

	/**
	 * Méthode sleep utilisée par les scripts
	 */

	@Override	
	public void sleep(long duree)
	{
		Sleep.sleep(duree);
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

	
	/**
	 * @return the obstacleDevantCapteur
	 */
	public boolean isObstacleImprevuDevantCapteur()
	{
		System.out.println("isObstacleImprevuDevantCapteur : " +((Boolean)obstacleImprevuDevantCapteur).toString());
		
		if (System.currentTimeMillis() > obstacleImprevuDevantCapteurDeathDate)
			obstacleImprevuDevantCapteur = false;
		
		return obstacleImprevuDevantCapteur;
	}

	/**
	 * @param obstacleDevantCapteur the obstacleDevantCapteur to set
	 */
	public void setObstacleImprevuDevantCapteur(boolean obstacleDevantCapteur, float TTL) 
	{
		this.obstacleImprevuDevantCapteur = obstacleDevantCapteur;
		if (obstacleImprevuDevantCapteur)
			obstacleImprevuDevantCapteurDeathDate = TTL + System.currentTimeMillis();
	}

	
}

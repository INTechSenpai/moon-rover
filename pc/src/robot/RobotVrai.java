package robot;

import robot.cartes.Actionneurs;
import robot.cartes.Capteur;
import robot.cartes.Deplacements;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import hook.Hook;
import hook.HookGenerator;

import java.lang.Math;
import java.util.ArrayList;

import pathfinding.Pathfinding;
import container.Service;

/**
 * Classe qui fournit des déplacements haut niveau
 * @author pf
 *
 */

public class RobotVrai extends Robot {

	protected Pathfinding pathfinding;
	protected Capteur capteur;
	protected Actionneurs actionneurs;
	protected Deplacements deplacements;
	protected HookGenerator hookgenerator;
	protected Table table;

	private Vec2 consigne = new Vec2(0,0);
	private float orientation_consigne = (float)-Math.PI/2;
	
	private boolean blocage = false;
	private boolean enMouvement = true;
	
	private boolean marche_arriere = false;
	private boolean effectuer_symetrie = true;
	
	public boolean pret = false;
	
	public int sleep_milieu_boucle_acquittement; // = self.config["sleep_acquit_serie"]
	public int sleep_fin_boucle_acquittement = 0;
	
	// Constructeur
	
	public RobotVrai(Pathfinding pathfinding, Capteur capteur, Actionneurs actionneurs, Deplacements deplacements, HookGenerator hookgenerator, Table table, Read_Ini config, Log log)
 	{
		super(config, log);
		this.pathfinding = pathfinding;
		this.capteur = capteur;
		this.actionneurs = actionneurs;
		this.deplacements = deplacements;
		this.hookgenerator =  hookgenerator;
		this.table = table;
 	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	// TODO
	public void initialiser_actionneurs()
	{
		
	}

	// TODO
	public void recaler()
	{
		
	}
	
	/**
	 * Arrête le robot
	 * @param avec_blocage
	 */
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
	public void stopper()
	{
		stopper(true);
	}

	/**
	 * Modifie la consigne en angle, de façon non bloquante
	 * @param angle
	 */
	public void correction_angle(float angle)
	{
		orientation_consigne = angle;
		deplacements.tourner((int)angle);
	}
	
	// TODO
	public void avancer(int distance, int nbTentatives, boolean retenterSiBlocage,
			boolean sansLeverException)
	{
		boolean memoire_marche_arriere = this.marche_arriere;
		boolean memoire_effectuer_symetrie = this.effectuer_symetrie;
		this.marche_arriere = (distance < 0);
		this.effectuer_symetrie = false;

		Vec2 consigne = new Vec2(0,0);
		consigne.x = (float) (this.position.x + distance*Math.cos(this.orientation_consigne));
		consigne.y = (float) (this.position.y + distance*Math.sin(this.orientation_consigne));
		
		this.va_au_point(consigne);
		
		this.marche_arriere = memoire_marche_arriere;
		this.effectuer_symetrie = memoire_effectuer_symetrie;
	}
	
	// TODO
	/**
	 * Fait tourner le robot (méthode bloquante)
	 */
	public void tourner(float angle, Hook[] hooks, int nombre_tentatives, boolean sans_lever_exception)
	{
		
	}
	

	// TODO
	/**
	 * Fait suivre au robot un chemin (fourni par la recherche de chemin)
	 */
	public void suit_chemin(ArrayList<Vec2> chemin, Hook[] hooks, boolean marche_arriere_auto, boolean symetrie_effectuee)
	{
		
	}


	// TODO
	/**
	 * Le robot va au point demandé
	 */
	public void va_au_point(Vec2 point, Hook[] hooks, boolean trajectoire_courbe, int nombre_tentatives, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean sans_lever_exception)
	{
		
	}

	/**
	 * Modifie la vitesse de translation
	 */
	public void set_vitesse_translation(String vitesse)
	{
		int pwm_max = conventions_vitesse_translation(vitesse);
		deplacements.set_vitesse_translation(pwm_max);
		log.debug("Modification de la vitesse de translation: "+vitesse, this);
	}

	/**
	 * Modifie la vitesse de rotation
	 */
	public void set_vitesse_rotation(String vitesse)
	{
		int pwm_max = conventions_vitesse_rotation(vitesse);
		deplacements.set_vitesse_rotation(pwm_max);
		log.debug("Modification de la vitesse de rotation: "+vitesse, this);
	}
	
	/**
	 * UTILISÉ UNIQUEMENT PAR LE THREAD DE MISE À JOUR
	 */	
	public void update_x_y_orientation()
	{
		float[] infos = deplacements.get_infos_x_y_orientation();
		position = new Vec2(infos[0], infos[1]);
		orientation = infos[2]/1000; // car get_infos renvoie des milliradians		
	}

	/*
	 * ACTIONNEURS
	 */

	public void tirerBalles(boolean rightSide)
	{
		// TODO
	}
	
	
	/* 
	 * GETTERS & SETTERS
	 */
	
	public void setPosition(Vec2 position) {
		this.position = position;
		deplacements.set_x((int)position.x);
		deplacements.set_y((int)position.y);
	}

	public void setOrientation(float orientation) {
		this.orientation = orientation;
		// TODO vérifier s'il n'y a pas un facteur 1000 qui se cache (passage radian en milliradian)
		deplacements.set_orientation((int)orientation);
	}

	public Vec2 getConsigne() {
		return consigne;
	}

	public void setConsigne(Vec2 consigne) {
		this.consigne = consigne;
	}

	public boolean isBlocage() {
		return blocage;
	}

	public void setBlocage(boolean blocage) {
		this.blocage = blocage;
	}

	public boolean isEnMouvement() {
		return enMouvement;
	}

	public void setEnMouvement(boolean enMouvement) {
		this.enMouvement = enMouvement;
	}

	public boolean isMarche_arriere() {
		return marche_arriere;
	}

	public void setMarche_arriere(boolean marche_arriere) {
		this.marche_arriere = marche_arriere;
	}

	public boolean isEffectuer_symetrie() {
		return effectuer_symetrie;
	}

	public void setEffectuer_symetrie(boolean effectuer_symetrie) {
		this.effectuer_symetrie = effectuer_symetrie;
	}
	
	/*
	 * MÉTHODES PRIVÉES
	 */

	private void avancerBasNiveau(int distance)
	{
		Vec2 consigne = new Vec2(0,0);
		consigne.x = (float) (this.position.x + distance*Math.cos(this.orientation_consigne));
		consigne.y = (float) (this.position.y + distance*Math.sin(this.orientation_consigne));
		this.va_au_point(consigne);
	}

	// TODO
	private void tournerBasNiveau(float angle, Hook[] hooks, boolean sans_lever_exception)
	{
		
	}
	
	private void tournerBasNiveau(float angle)
	{
		tournerBasNiveau(angle, null, false);
	}
	
	private void tournerBasNiveau(float angle, boolean sans_lever_exception)
	{
		tournerBasNiveau(angle, null, sans_lever_exception);
	}

	private void tournerBasNiveau(float angle, Hook[] hooks)
	{
		tournerBasNiveau(angle, hooks, false);
	}

	// TODO
	private void va_au_pointBasNiveau(Vec2 position, Hook[] hooks, boolean trajectoire_courbe, boolean sans_lever_exception)
	{
		
	}
	
	private void va_au_pointBasNiveau(Vec2 position, boolean trajectoire_courbe, boolean sans_lever_exception)
	{
		va_au_pointBasNiveau(position, null, trajectoire_courbe, sans_lever_exception);
	}
	
	private void va_au_pointBasNiveau(Vec2 position, boolean trajectoire_courbe)
	{
		va_au_pointBasNiveau(position, null, trajectoire_courbe, false);		
	}

	private void va_au_pointBasNiveau(Vec2 position, Hook[] hooks, boolean trajectoire_courbe)
	{
		va_au_pointBasNiveau(position, hooks, trajectoire_courbe, false);		
	}

	private void va_au_pointBasNiveau(Vec2 position, Hook[] hooks)
	{
		va_au_pointBasNiveau(position, hooks, false, false);		
	}

	private void va_au_pointBasNiveau(Vec2 position)
	{
		va_au_pointBasNiveau(position, null, false, false);		
	}

	// TODO
	private void mise_a_jour_consignes()
	{
		
	}

	// TODO
	private void detecter_collision()
	{
		
	}
	
	// TODO
	private void acquittement(boolean detection_collision, boolean sans_lever_exception)
	{
		
	}
	
	private void acquittement()
	{
		acquittement(true, false);
	}
	
}

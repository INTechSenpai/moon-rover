package robot;

import smartMath.Vec2;
import java.lang.Math;
import container.Service;

/**
 * Classe qui fournit des déplacements haut niveau
 * @author pf
 *
 */

public class RobotVrai extends Robot {

	private Vec2 position = new Vec2(0,0);
	private double orientation = 0;

	private Vec2 consigne = new Vec2(0,0);
	private double orientation_consigne = 0;
	
	private boolean blocage = false;
	private boolean enMouvement = true;
	
	private boolean marche_arriere = false;
	private boolean effectuer_symetrie = true;
	
	public boolean pret = false;
	
	public int sleep_milieu_boucle_acquittement; // = self.config["sleep_acquit_serie"]
	public int sleep_fin_boucle_acquittement = 0;
	
	// Constructeur
	
	public RobotVrai(Service pathfinding, Service capteur, Service actionneurs, Service deplacements, Service hookgenerator, Service table, Service config, Service log)
 	{
		super(pathfinding, capteur, actionneurs, deplacements, hookgenerator, table, config, log);
 	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	public void stopper(boolean avec_blocage)
	{
		log.debug("Arrêt du robot", this);
		if(avec_blocage)
			blocage = true;
		deplacements.stopper();			
	}
	
	public void stopper()
	{
		stopper(true);
	}

	/**
	 * Modifie la consigne en angle, de façon non bloquante
	 * @param angle
	 */
	public void correction_angle(double angle)
	{
		orientation_consigne = angle;
		deplacements.tourner((int)angle);
	}
	
	private void avancerBasNiveau(int distance)
	{
		Vec2 consigne = new Vec2(0,0);
		consigne.x = (float) (this.position.x + distance*Math.cos(this.orientation_consigne));
		consigne.y = (float) (this.position.y + distance*Math.sin(this.orientation_consigne));
		this.va_au_point(consigne);
	}
	
	@Override
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
	
	public void avancer(int distance, int nbTentatives, boolean retenterSiBlocage)
	{
		this.avancer(distance, nbTentatives, retenterSiBlocage, false);
	}
	
	public void avancer(int distance, int nbTentatives)
	{
		this.avancer(distance, nbTentatives, true, false);
	}

	public void avancer(int distance)
	{
		this.avancer(distance, 2, true, false);
	}

	public void tourner()
	{
		
	}
	public void suit_chemin()
	{
		
	}
	
	@Override
	public void va_au_point(Vec2 point)
	{
		
	}
	public void set_vitesse_translation()
	{
		
	}
	public void set_vitesse_rotation()
	{
		
	}
	
	/**
	 * UTILISÉ UNIQUEMENT PAR LE THREAD DE MISE À JOUR
	 */	
	public void update_x_y_orientation()
	{
		int[] infos = deplacements.get_infos_x_y_orientation();
		position = new Vec2(infos[0], infos[1]);
		orientation = infos[2]/1000; // car get_infos renvoie des milliradians		
	}
	
	/* 
	 * GETTERS & SETTERS
	 */
	
	public Vec2 getPosition() {
		return this.position;
	}

	public void setPosition(Vec2 position) {
		deplacements.set_x((int)position.x);
		deplacements.set_y((int)position.y);
	}

	public double getOrientation() {
		return orientation;
	}

	public void setOrientation(float orientation) {
		this.orientation = orientation;
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
	
}

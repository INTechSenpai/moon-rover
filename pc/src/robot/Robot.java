package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import container.Service;
import exception.ConfigException;
import exception.MouvementImpossibleException;
import exception.SerialException;
import utils.Log;
import utils.Read_Ini;

/**
 *  Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author PF
 */

public abstract class Robot implements Service {
	
	/*
	 * DÉPLACEMENT HAUT NIVEAU
	 */
	
	public abstract void stopper(boolean avec_blocage);
	protected abstract void tourner(float angle, ArrayList<Hook> hooks, int nombre_tentatives, boolean sans_lever_exception)
			 	throws MouvementImpossibleException;
	protected abstract void avancer(int distance, ArrayList<Hook> hooks, int nbTentatives, boolean retenterSiBlocage, boolean sansLeverException)
				throws MouvementImpossibleException;
	protected abstract void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean trajectoire_courbe)
				throws MouvementImpossibleException;
	public abstract void set_vitesse_translation(String vitesse);
	public abstract void set_vitesse_rotation(String vitesse);
	
	/**
	 * Va au point donné. Différentes options possibles.
	 * @param point
	 * @param hooks
	 * @param trajectoire_courbe: le robot est-il autorisé à effectuer une trajectoire courbe? Une trajectoire courbe est plus rapide mais peu contrôlée (on parle bien d'autorisation; l'utilisation effective de la trajectoire courbe dépend d'autres paramètres, comme la place disponible)
	 * @param nombre_tentatives: le nombre de tentatives restantes
	 * @param retenter_si_blocage: le robot doit-il retenter en cas de blocage? (détection ennemi, ...). Dans notre cas, s'il y a d'autres scripts, il ne retente pas.
	 * @param symetrie_effectuee: la symétrie a-t-elle déjà été effectuée? (il ne faut pas l'appliquer deux fois)
	 * @param sans_lever_exception: utilisé pour foncer dans le mur exprès, par exemple pour poser les fresques ou pour se recaler.
	 * @param enchainer: utilisé lors d'un enchaînement de va_au_point par suit_chemin. Au lieu de s'arrêter entre chaque segment, le robot conserve sa vitesse.
	 * @throws MouvementImpossibleException
	 */
	protected abstract void va_au_point(Vec2 point, ArrayList<Hook> hooks, boolean trajectoire_courbe, int nombre_tentatives, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean sans_lever_exception, boolean enchainer)
				throws MouvementImpossibleException;

	/*
	 * Méthodes d'initialisation
	 */
	
	public abstract void setPosition(Vec2 position);
	public abstract void setOrientation(float orientation);
	
	/*
	 * ACTIONNEURS
	 */
	
	public abstract void tirerBalle() throws SerialException;
	public abstract void takefire() throws SerialException;
	public abstract void deposer_fresques() throws SerialException;
	public abstract void bac_bas() throws SerialException;
	public abstract void bac_haut() throws SerialException;
	public abstract void rateau(PositionRateau position, Cote cote) throws SerialException;

	public abstract void sleep(long duree);
	
	// Dépendances
	protected Read_Ini config;
	protected Log log;

	/* Ces attributs sont nécessaires à robotvrai et à robotchrono, donc ils sont ici.
	 * Cela regroupe tous les attributs ayant une conséquence dans la stratégie
	 */
	protected Vec2 position = new Vec2(0, 0);
	protected float orientation = 0;
	protected String couleur;
	protected boolean effectuer_symetrie = true;
	
	protected int nombre_lances = 6;
	protected boolean fresques_posees = false;

	protected int nb_tentatives = 2;
	
	public Robot(Read_Ini config, Log log)
	{
		this.config = config;
		this.log = log;
		try {
			couleur = config.get("couleur");
		} catch (ConfigException e) {
			log.critical(e, this);
		}
		try {
			nb_tentatives = Integer.parseInt(config.get("nb_tentatives"));
		} catch (ConfigException e) {
			log.critical(e, this);
		}
	}
	
	protected int conventions_vitesse_translation(String vitesse)
	{
        if(vitesse == "entre_scripts")
        	return 150;
        else if(vitesse == "recal_faible")
            return 90;
        else if(vitesse == "recal_forte")
            return 120;
        else if(vitesse == "vitesse_mammouth")
        	return 50; // TODO
        else
        {
        	log.warning("Erreur vitesse translation: "+vitesse, this);
        	return 150;
        }
	}

	protected int conventions_vitesse_rotation(String vitesse)
	{
        if(vitesse == "entre_scripts")
        	return 160;
        else if(vitesse == "recal_faible")
            return 120;
        else if(vitesse == "recal_forte")
            return 130;
        else
        {
        	log.warning("Erreur vitesse rotation: "+vitesse, this);
        	return 160;
        }
	}
	
	public Vec2 getPosition() {
		return position.clone();
	}

	public float getOrientation() {
		return orientation;
	}
	
	public int getNbrLances() {
		return nombre_lances;
	}

	public boolean isFresquesPosees()
	{
		return fresques_posees;
	}
	
	// Les méthodes avec le paramètre nbTentatives sont en protected 
	protected void va_au_point(Vec2 point, ArrayList<Hook> hooks, int nbTentatives, boolean retenterSiBlocage, boolean sansLeverException) throws MouvementImpossibleException
	{
		va_au_point(point, hooks, false, nb_tentatives, retenterSiBlocage, false, sansLeverException, false);
	}

	public void va_au_point(Vec2 point) throws MouvementImpossibleException
	{
		va_au_point(point, null, false, nb_tentatives, true, false, false, false);
	}

	public void va_au_point(Vec2 point, boolean retenterSiBlocage) throws MouvementImpossibleException
	{
		va_au_point(point, null, false, nb_tentatives, retenterSiBlocage, false, false, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks) throws MouvementImpossibleException
	{
		suit_chemin(chemin, hooks, false, false, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks, boolean retenter_si_blocage, boolean trajectoire_courbe) throws MouvementImpossibleException
	{
		suit_chemin(chemin, hooks, retenter_si_blocage, false, trajectoire_courbe);
	}

	public void suit_chemin(ArrayList<Vec2> chemin) throws MouvementImpossibleException
	{
		suit_chemin(chemin, null, false, false, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks, boolean retenter_si_blocage) throws MouvementImpossibleException
	{
		suit_chemin(chemin, hooks, retenter_si_blocage, false, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, boolean retenter_si_blocage) throws MouvementImpossibleException
	{
		suit_chemin(chemin, null, retenter_si_blocage, false, false);
	}

	public void suit_chemin(ArrayList<Vec2> chemin, boolean retenter_si_blocage, boolean trajectoire_courbe) throws MouvementImpossibleException
	{
		suit_chemin(chemin, null, retenter_si_blocage, false, trajectoire_courbe);
	}

	public void tourner(float angle, ArrayList<Hook> hooks, boolean sans_lever_exception) throws MouvementImpossibleException
	{
		tourner(angle, null, nb_tentatives, sans_lever_exception);
	}

	public void tourner(float angle, boolean pas_de_symetrie) throws MouvementImpossibleException
	{
		boolean mem_effectuer_symetrie = effectuer_symetrie;
		if(pas_de_symetrie)
			effectuer_symetrie = false;
		tourner(angle, null, nb_tentatives, false);
		effectuer_symetrie = mem_effectuer_symetrie;
	}
	
	protected void tourner(float angle, int nombre_tentatives) throws MouvementImpossibleException
	{
		tourner(angle, null, nombre_tentatives, false);		
	}

	public void tourner(float angle, ArrayList<Hook> hooks) throws MouvementImpossibleException
	{
		tourner(angle, hooks, nb_tentatives, false);
	}

	public void tourner(float angle) throws MouvementImpossibleException
	{
		tourner(angle, null, nb_tentatives, false);
	}

	protected void avancer(int distance, int nbTentatives, boolean retenterSiBlocage, boolean sansLeverException) throws MouvementImpossibleException
	{
		this.avancer(distance, null, nbTentatives, retenterSiBlocage, sansLeverException);
	}

	public void avancer(int distance, boolean retenterSiBlocage, boolean sansLeverException) throws MouvementImpossibleException
	{
		this.avancer(distance, null, nb_tentatives, retenterSiBlocage, sansLeverException);
	}

	protected void avancer(int distance, int nbTentatives, boolean retenterSiBlocage) throws MouvementImpossibleException
	{
		this.avancer(distance, null, nbTentatives, retenterSiBlocage, false);
	}
	
	protected void avancer(int distance, int nbTentatives) throws MouvementImpossibleException
	{
		this.avancer(distance, null, nbTentatives, true, false);
	}

	public void avancer(int distance, ArrayList<Hook> hooks) throws MouvementImpossibleException
	{
		this.avancer(distance, hooks, nb_tentatives, true, false);
	}

	public void avancer(int distance) throws MouvementImpossibleException
	{
		this.avancer(distance, null, nb_tentatives, true, false);
	}

	public void avancer(int distance, boolean retenterSiBlocage) throws MouvementImpossibleException
	{
		this.avancer(distance, null, nb_tentatives, retenterSiBlocage, false);
	}

	public void stopper()
	{
		stopper(false);
	}

}

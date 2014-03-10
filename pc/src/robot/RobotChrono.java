package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import utils.Log;
import utils.Read_Ini;
import exception.MouvementImpossibleException;
import exception.RobotChronoException;
import exception.SerialException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf, krissprolls
 *
 */

public class RobotChrono extends Robot {

	private float vitesse_mmpms;
	private float vitesse_rpms;
	
	// Durée en millisecondes
	private int duree = 0;
	
	public RobotChrono(Read_Ini config, Log log)
	{
		super(config, log);
	}
	
	@Override
	public void setPosition(Vec2 position) {
		this.position = position;
	}
	
	@Override
	public void setOrientation(float orientation) {
		this.orientation = orientation;
	}

	// La plupart de ces méthodes resteront vides

	@Override
	public void stopper(boolean avec_blocage)
	{
	}
	
	@Override
	protected void avancer(int distance, ArrayList<Hook> hooks, int nbTentatives, boolean retenterSiBlocage, boolean sansLeverException)
	{
		try {
			dureePositive((long)(((float)Math.abs(distance))/vitesse_mmpms));
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}
		duree += ((float)Math.abs(distance))/vitesse_mmpms;
		Vec2 ecart = new Vec2((float)Math.cos(orientation), (float)Math.sin(orientation));
		ecart.x *= distance;
		ecart.y *= distance;
		position.Plus(ecart);
	}
	
	@Override
	public void set_vitesse_translation(String vitesse)
	{
        int pwm_max = conventions_vitesse_translation(vitesse);
        vitesse_mmpms = ((float)2500)/((float)613.52 * (float)(Math.pow((double)pwm_max,(double)(-1.034))))/1000;
	}

	@Override
	public void set_vitesse_rotation(String vitesse)
	{
        int pwm_max = conventions_vitesse_rotation(vitesse);
        vitesse_rpms = ((float)Math.PI)/((float)277.85 * (float)Math.pow(pwm_max,(-1.222)))/1000;
	}

	// Méthodes propres à RobotChrono
	
	public void initialiser_compteur(int distance_initiale)
	{
		try {
			dureePositive((long)(((float)distance_initiale)/vitesse_mmpms));
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}
		duree = (int) (((float)distance_initiale)/vitesse_mmpms);
	}
	public int get_compteur()
	{
		return duree;
	}

	public void clone(RobotChrono rc)
	{
		rc.position = position.clone();
		rc.orientation = orientation;
		rc.vitesse_rpms = vitesse_rpms;
		rc.vitesse_mmpms = vitesse_mmpms;
	}

	public RobotChrono clone()
	{
		RobotChrono cloned_robotchrono = new RobotChrono(config, log);
		clone(cloned_robotchrono);
		return cloned_robotchrono;
	}

	@Override
	protected void tourner(float angle, ArrayList<Hook> hooks, int nombre_tentatives, boolean sans_lever_exception)
	{
		float delta = angle-orientation;
		if(delta < 0)
			delta += 2*Math.PI;
		if(delta > Math.PI)
			delta = 2*(float)Math.PI - delta;
		orientation = angle;
		
		try {
			dureePositive((long)(delta/vitesse_rpms));
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}
		duree += delta/vitesse_rpms;
	}

	@Override
	public void tirerBalle()
	{
		// durée "nulle" car appelé par un hook
	}
	@Override
	public void lancerFilet()
	{
		duree += 1000;
	}
	@Override
	public void poserFeuBonCoteGauche()
	{
		duree += 1000;
	}
	@Override
	public void poserFeuEnRetournantGauche()
	{
		duree += 1500;
	}
	@Override
	public void poserFeuBonCoteDroit()
	{
		duree += 1000;
	}
	@Override
	public void poserFeuEnRetournantDroit()
	{
		duree += 1500;
	}
	@Override
	public void lever_pince_gauche() 
	{
		duree += 1000;
	}

	@Override	
	public void lever_pince_droite()
	{
		duree += 1000;
	}
	@Override
	public void baisser_pince_gauche()
	{
		duree +=1000;
	}
	@Override
	public void baisser_pince_droite()
	{
		duree +=1000;
	}
	@Override
	public void ouvrir_pince_gauche() 
	{
		duree +=1000;
	}
	@Override
	public void ouvrir_pince_droite()
	{
		duree +=1000;
	}
	@Override
	public void milieu_pince_gauche() throws SerialException
	{
		duree += 1000;
	}
	@Override
	public void milieu_pince_droite() throws SerialException
	{
		duree +=1000;
	}
	public void fermer_pince_gauche() throws SerialException {
		duree += 1000;		
	}

	@Override
	public void fermer_pince_droite() throws SerialException {
		duree += 1000;
	}
	@Override
	protected void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean trajectoire_courbe) throws MouvementImpossibleException
	{
		for(Vec2 point: chemin)
			va_au_point(point);
	}

	@Override
	protected void va_au_point(Vec2 point, ArrayList<Hook> hooks, boolean trajectoire_courbe, int nombre_tentatives, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean sans_lever_exception, boolean enchainer)
	{
		if(couleur == "rouge")
			point.x *= -1;
		try {
			dureePositive((long)(position.distance(point)/vitesse_mmpms));
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}
		duree += position.distance(point)/vitesse_mmpms;
		position = point.clone();
	}

	@Override
	public void bac_bas()
	{
	}

	@Override
	public void bac_haut()
	{
	}

	@Override
	public void rateau(PositionRateau position, Cote cote)
	{
	}

	@Override
	public void deposer_fresques() {
	}

	@Override
	public void takefiredroit() {
		duree += 2000;
	}

	@Override
	public void takefiregauche() {
		duree += 2000;		
	}

	
	// TODO à compléter au fur et à mesure
	public void majRobotChrono(RobotVrai robotvrai)
	{
		position = robotvrai.position;
		orientation = robotvrai.orientation;
		nombre_lances = robotvrai.nombre_lances;
		fresques_posees = robotvrai.fresques_posees;
	}
	
	/**
	 * Utilisé par les tests
	 * @param other
	 * @return
	 */
	// TODO à compléter au fur et à mesure

	public boolean equals(RobotChrono other)
	{
		return 	position.equals(other.position)
				&& orientation == other.orientation
				&& nombre_lances == other.nombre_lances
				&& fresques_posees == other.fresques_posees;
	}

	@Override
	public void sleep(long duree) {
		try {
			dureePositive(duree);
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}
		this.duree += duree;
	}
	
	private void dureePositive(long duree) throws RobotChronoException
	{
		if(duree < 0)
			throw new RobotChronoException();
	}

	
}

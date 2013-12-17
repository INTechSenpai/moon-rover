package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import utils.Log;
import utils.Read_Ini;
import exception.MouvementImpossibleException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
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
	
	public void setPosition(Vec2 position) {
		this.position = position;
	}
	
	public void setOrientation(float orientation) {
		this.orientation = orientation;
	}

	// La plupart de ces méthodes resteront vides

	public void stopper()
	{
	}
	
	public void avancer(int distance, ArrayList<Hook> hooks, int nbTentatives, boolean retenterSiBlocage, boolean sansLeverException)
	{
		duree += ((float)distance)/vitesse_mmpms;
		Vec2 ecart = new Vec2((float)Math.cos(orientation), (float)Math.sin(orientation));
		ecart.x *= distance;
		ecart.y *= distance;
		position.Plus(ecart);
	}
	
	public void correction_angle(float angle)
	{
	}

	public void set_vitesse_translation(String vitesse)
	{
        int pwm_max = conventions_vitesse_translation(vitesse);
        vitesse_mmpms = ((float)2500)/((float)613.52 * (float)(Math.pow((double)pwm_max,(double)(-1.034))))/1000;
	}
	public void set_vitesse_rotation(String vitesse)
	{
        int pwm_max = conventions_vitesse_rotation(vitesse);
        vitesse_rpms = ((float)Math.PI)/((float)277.85 * (float)Math.pow(pwm_max,(-1.222)))/1000;
	}

	// Méthodes propres à RobotChrono
	
	public void initialiser_compteur(int distance_initiale)
	{
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

	public void tourner(float angle, ArrayList<Hook> hooks, int nombre_tentatives, boolean sans_lever_exception)
	{
		float delta = angle-orientation;
		if(delta < 0)
			delta += 2*Math.PI;
		if(delta > Math.PI)
			delta = 2*(float)Math.PI - delta;
		orientation = angle;
		duree += delta/vitesse_rpms;
	}

	public void tirerBalles()
	{
		// durée "nulle" car appelé par un hook
	}

	public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks, boolean marche_arriere_auto, boolean symetrie_effectuee) throws MouvementImpossibleException
	{
		for(Vec2 point: chemin)
			va_au_point(point);
	}

	public void va_au_point(Vec2 point, ArrayList<Hook> hooks, boolean trajectoire_courbe, int nombre_tentatives, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean sans_lever_exception)
	{
		duree += position.distance(point)/vitesse_mmpms;
		position = point.clone();
	}

	public void recaler()
	{
	}

	@Override
	public void initialiser_actionneurs()
	{
	}

	@Override
	public void baisser_rateaux() {
	}

	@Override
	public void baisser_rateaux_bas() {
	}

	@Override
	public void remonter_rateau(boolean right) {
	}

	@Override
	public void remonter_rateaux() {
	}
	
	@Override
	public void takefire() {
	}
	
	// TODO à compléter au fur et à mesure
	public void majRobotChrono(RobotVrai robotvrai)
	{
		position = robotvrai.position;
		orientation = robotvrai.orientation;
		nombre_lances = robotvrai.nombre_lances;
	}
}

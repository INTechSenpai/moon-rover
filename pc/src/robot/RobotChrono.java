package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import strategie.MemoryManagerProduct;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 *
 */

public class RobotChrono extends Robot implements Service, MemoryManagerProduct {

	private float vitesse_mmps;
	private float vitesse_rps;
	
	// Durée en millisecondes
	private long duree = 0;
	
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
	
	private void setVitesse_rps(float vitesse_rps) {
		this.vitesse_rps = vitesse_rps;
	}

	private void setVitesse_mmps(float vitesse_mmps) {
		this.vitesse_mmps = vitesse_mmps;
	}

	// La plupart de ces méthodes resteront vides

	public void stopper()
	{
	}
	
	// TODO
	public void avancer(int distance, Hook[] hooks, int nbTentatives, boolean retenterSiBlocage,
			boolean sansLeverException)
	{
		
	}
	
	public void correction_angle(float angle)
	{
	}

	public void set_vitesse_translation(String vitesse)
	{
        int pwm_max = conventions_vitesse_translation(vitesse);
        vitesse_mmps = ((float)2500)/((float)613.52 * (float)(Math.pow((double)pwm_max,(double)(-1.034))));
	}
	public void set_vitesse_rotation(String vitesse)
	{
        int pwm_max = conventions_vitesse_rotation(vitesse);
        vitesse_rps = ((float)Math.PI)/((float)277.85 * (float)Math.pow(pwm_max,(-1.222)));
        vitesse_mmps = ((float)2500)/((float)613.52 * (float)(Math.pow((double)pwm_max,(double)(-1.034))));
	}

	// Méthodes propres à RobotChrono
	
	public void reset_compteur()
	{
		duree = 0;
	}
	public long get_compteur()
	{
		return duree;
	}

	@Override
	public MemoryManagerProduct clone(MemoryManagerProduct cloned_robotchrono) {
		((RobotChrono)cloned_robotchrono).setPosition(position);
		((RobotChrono)cloned_robotchrono).setOrientation(orientation);
		((RobotChrono)cloned_robotchrono).setVitesse_rps(vitesse_rps);
		((RobotChrono)cloned_robotchrono).setVitesse_mmps(vitesse_mmps);
		return cloned_robotchrono;
	}

	public MemoryManagerProduct clone()
	{
		RobotChrono cloned_robotchrono = new RobotChrono(config, log);
		return clone(cloned_robotchrono);
	}

	@Override
	public String getNom() {
		return "RobotChrono";
	}

	// TODO
	public void tourner(float angle, Hook[] hooks, int nombre_tentatives, boolean sans_lever_exception)
	{

	}

	// TODO durée
	public void tirerBalles(boolean rightSide)
	{
		
	}

	// TODO
	public void suit_chemin(ArrayList<Vec2> chemin, Hook[] hooks, boolean marche_arriere_auto, boolean symetrie_effectuee)
	{
		
	}

	// TODO
	public void va_au_point(Vec2 point, Hook[] hooks, boolean trajectoire_courbe, int nombre_tentatives, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean sans_lever_exception)
	{
		
	}

	// TODO
	public void avancer(int distance, int nbTentatives,
			boolean retenterSiBlocage) {
		
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
	
	// TODO à compléter au fur et à mesure
	public void initialiserRobotChrono(RobotVrai robotvrai)
	{
		position = robotvrai.position;
		orientation = robotvrai.orientation;
		nombre_lances = robotvrai.nombre_lances;
	}
}

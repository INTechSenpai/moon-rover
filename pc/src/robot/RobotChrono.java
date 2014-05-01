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
 * @author pf
 * @author (krissprolls)
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
		Vec2 ecart = new Vec2((int)(distance*Math.cos(orientation)), (int)(distance*Math.sin(orientation)));
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
		if(distance_initiale != 0)
		{
			try {
				dureePositive((long)(((float)distance_initiale)/vitesse_mmpms));
			} catch (RobotChronoException e) {
				e.printStackTrace();
			}
			duree = (int) (((float)distance_initiale)/vitesse_mmpms);
		}
		else 
			distance_initiale = 0;
	}
	public int get_compteur()
	{
		return duree;
	}

	public RobotChrono clone()
	{
		RobotChrono cloned_robotchrono = new RobotChrono(config, log);
		copy(cloned_robotchrono);
		return cloned_robotchrono;
	}

	@Override
	protected void tourner(float angle, ArrayList<Hook> hooks,
			int nombre_tentatives, boolean sans_lever_exception,
			boolean symetrie_effectuee, boolean retenter_si_blocage)
			throws MouvementImpossibleException
	{
		float delta = angle-orientation;
		if(delta < 0)
			delta *= -1;
		while(delta > 2*Math.PI)
			delta -= 2*Math.PI;
		if(delta > Math.PI)
			delta = 2*(float)Math.PI - delta;
		orientation = angle;
		if(delta != 0) 
		{
			try {
				dureePositive((long)(delta/vitesse_rpms));
			} catch (RobotChronoException e) {
				e.printStackTrace();
			}
		}
		duree += delta/vitesse_rpms;
	}

	@Override
	public void tirerBalle() throws SerialException
	{
	    super.tirerBalle();
		// durée "nulle" car appelé par un hook
		duree += 1500; // TODO
	}
	@Override
	public void lancerFilet()
	{
		duree += 1000; // TODO
	}
	@Override
	public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks, boolean retenter_si_blocage, boolean symetrie_effectuee, boolean trajectoire_courbe, boolean sans_lever_exception) throws MouvementImpossibleException
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
		duree += 1000;
	}

	@Override
	public void bac_haut() throws SerialException
	{
		super.bac_haut();
		duree += 1000; // TODO
	}

	@Override
	public void rateau(PositionRateau position, Cote cote)
	{
		duree += 200; // TODO
	}

	@Override
	public void deposer_fresques() throws SerialException {
		super.deposer_fresques();
		duree +=2000; //TODO
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
		if(duree < -0.001)
			throw new RobotChronoException();
	}

	@Override
	public void poserFeuBonCote(Cote cote) throws SerialException {
	    super.poserFeuBonCote(cote);
		duree += 1000; // TODO
	}

	@Override
	public void poserFeuEnRetournant(Cote cote) throws SerialException {
        super.poserFeuEnRetournant(cote);
		duree += 1000; // TODO
	}

	@Override
	public void lever_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void baisser_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void fermer_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void ouvrir_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void milieu_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void tourner_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void presque_fermer_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void ouvrir_bas_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void prendre_torche(Cote cote) throws SerialException {
		super.prendre_torche(cote);
	    duree += 1000; // TODO
	}
	
}

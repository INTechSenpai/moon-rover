package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import utils.Log;
import utils.Read_Ini;
import enums.Cote;
import enums.PositionRateau;
import enums.Vitesse;
import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf
 * @author (krissprolls), pf et marsu !
 *
 */

public class RobotChrono extends Robot {

	protected Vec2 position = new Vec2();
	protected double orientation;
	
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
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	// La plupart de ces méthodes resteront vides

	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
            throws MouvementImpossibleException
	{
/*		try {
			dureePositive((long)(((float)Math.abs(distance))/vitesse_mmpms));
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}*/
		duree += Math.abs(distance)*vitesse.inverse_vitesse_mmpms;
		Vec2 ecart;
        ecart = new Vec2((int)(distance*Math.cos(orientation)), (int)(distance*Math.sin(orientation)));

		position.Plus(ecart);
	}
	
	@Override
	public void set_vitesse(Vitesse vitesse)
	{
	    this.vitesse = vitesse;
	}

	// Méthodes propres à RobotChrono
	
	public void initialiser_compteur(int distance_initiale)
	{
		if(distance_initiale != 0)
		{
/*			try {
				dureePositive((long)(((float)distance_initiale)/vitesse_mmpms));
			} catch (RobotChronoException e) {
				e.printStackTrace();
			}*/
			duree = distance_initiale*vitesse.inverse_vitesse_mmpms;
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
    public void tourner(double angle, ArrayList<Hook> hooks, boolean mur)
            throws MouvementImpossibleException
	{
        if(symetrie)
            angle = Math.PI-angle;
	    
		double delta = angle-orientation;
		if(delta < 0)
			delta *= -1;
		while(delta > 2*Math.PI)
			delta -= 2*Math.PI;
		if(delta > Math.PI)
			delta = 2*(float)Math.PI - delta;
		orientation = angle;
/*		if(delta != 0) 
		{
			try {
				dureePositive((long)(delta/vitesse_rpms));
			} catch (RobotChronoException e) {
				e.printStackTrace();
			}
		}*/
		duree += delta*vitesse.inverse_vitesse_rpms;
	}

	@Override
	public void tirerBalle()
	{
	    super.tirerBalle();
		duree += 1500;
	}
	@Override
	public void lancerFilet()
	{
		duree += 1000;
	}
	@Override
    public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks)
            throws MouvementImpossibleException
	{
		for(Vec2 point: chemin)
			va_au_point(point);
	}
	
	public void va_au_point(Vec2 point)
	{
		if(symetrie)
			point.x *= -1;
/*		try {
			dureePositive((long)(position.distance(point)/vitesse_mmpms));
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}*/
		duree += position.distance(point)*vitesse.inverse_vitesse_mmpms;
		position = point.clone();
	}

	@Override
	public void bac_bas()
	{
		duree += 167;
	}

	@Override
	public void bac_haut() throws SerialException
	{
		super.bac_haut();
		duree += 167;
	}

	@Override
	public void rateau(PositionRateau position, Cote cote)
	{
		duree += 313;
	}

	@Override
	public void deposer_fresques() throws SerialException {
		super.deposer_fresques();
		duree +=0; //durée nulle (pas d'actionneur associé)
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
/*		try {
			dureePositive(duree);
		} catch (RobotChronoException e) {
			e.printStackTrace();
		}*/
		this.duree += duree;
	}
	
/*	private void dureePositive(long duree) throws RobotChronoException
	{
		if(duree < -0.001)
			throw new RobotChronoException();
	}*/

	@Override
	public void poserFeuBonCote(Cote cote) throws SerialException {
	    super.poserFeuBonCote(cote);
		duree += 3000;
	}

	@Override
	public void poserFeuEnRetournant(Cote cote) throws SerialException {
        super.poserFeuEnRetournant(cote);
		duree += 5000; // TODO
	}

	@Override
	public void lever_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}
	@Override
	public void renverserFeu(Cote cote) throws SerialException
	{		
		duree += 800;
	}

	@Override
	public void baisser_pince(Cote cote) throws SerialException {
		duree += 500; // TODO
	}

	@Override
	public void fermer_pince(Cote cote) throws SerialException {
		duree += 600; // TODO
	}

	@Override
	public void ouvrir_pince(Cote cote) throws SerialException {
		duree += 800; // TODO
	}

	@Override
	public void milieu_pince(Cote cote) throws SerialException {
		duree += 1000; // TODO
	}

	@Override
	public void tourner_pince(Cote cote) throws SerialException {
		duree += 1500; // TODO
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
	    duree += 5000; // TODO
	}
	@Override
    public void takefire(Cote cotePrise, Cote coteReel) throws SerialException, MouvementImpossibleException
	{
		super.takefire(cotePrise, coteReel);
		duree += 5000;
	}

    @Override
    public void stopper()
    {
    }

    @Override
    public Vec2 getPosition()
    {
        return position.clone();
    }

    @Override
    public double getOrientation()
    {
        return orientation;
    }
    
    @Override
    public Vec2 getPositionFast()
    {
        return position.clone();
    }

    @Override
    public double getOrientationFast()
    {
        return orientation;
    }

    @Override
    public void setInsiste(boolean insiste)
    {}

    @Override
    public void copy(RobotChrono rc)
    {
        super.copy(rc);
        position.copy(rc.position);
        rc.orientation = orientation;
    }

    @Override
    public void bac_tres_bas() throws SerialException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void allume_ventilo() throws SerialException
    {
    	
    }

    @Override
    public void eteint_ventilo() throws SerialException
    {
    }

    public void desactiver_asservissement_rotation()
    {}

    public void activer_asservissement_rotation()
    {}

}

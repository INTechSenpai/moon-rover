package robot;

import java.util.ArrayList;

import pathfinding.Pathfinding;
import hook.Hook;
import smartMath.Vec2;
import container.Service;
import enums.Colour;
import enums.Cote;
import enums.PositionRateau;
import enums.Vitesse;
import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;
import exceptions.strategie.PathfindingException;
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
	
	public abstract void stopper();
    public abstract void tourner(double angle, ArrayList<Hook> hooks, boolean mur)
            throws MouvementImpossibleException;
    public abstract void avancer(int distance, ArrayList<Hook> hooks, boolean mur)
            throws MouvementImpossibleException;
    public abstract void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks)
            throws MouvementImpossibleException;
	public abstract void set_vitesse(Vitesse vitesse);
	
	public abstract void setPosition(Vec2 position);
	public abstract void setOrientation(double orientation);
    public abstract Vec2 getPosition();
    public abstract double getOrientation();
    public abstract Vec2 getPositionFast();
    public abstract double getOrientationFast();
    public abstract void sleep(long duree);
    public abstract void setInsiste(boolean insiste);

	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
    public void copy(RobotChrono rc) // 15,3%
    {
        rc.set_vitesse(vitesse);
        rc.nombre_lances = nombre_lances;
        rc.fresques_posees = fresques_posees;
        rc.nombre_fruits_bac = nombre_fruits_bac;
        rc.tient_feu_droite = tient_feu_droite;
        rc.tient_feu_gauche = tient_feu_gauche;
        rc.feu_tenu_gauche_rouge = feu_tenu_gauche_rouge;
        rc.feu_tenu_droite_rouge = feu_tenu_droite_rouge;
    }

    /*
	 * ACTIONNEURS
	 */

	public void tirerBalle()
	{
	    nombre_lances--;
	}
		
    public abstract void bac_tres_bas() throws SerialException;
	public abstract void bac_bas() throws SerialException;
	public abstract void rateau(PositionRateau position, Cote cote) throws SerialException;
	public abstract void lancerFilet() throws SerialException;
	public abstract void lever_pince(Cote cote) throws SerialException;
	public abstract void baisser_pince(Cote cote) throws SerialException;
	public abstract void fermer_pince(Cote cote)throws SerialException;
	public abstract void ouvrir_pince(Cote cote) throws SerialException;
	public abstract void milieu_pince(Cote cote) throws SerialException;
	public abstract void tourner_pince(Cote cote) throws SerialException;
	public abstract void presque_fermer_pince(Cote cote) throws SerialException;
	public abstract void ouvrir_bas_pince(Cote cote) throws SerialException;
	public abstract void renverserFeu(Cote cote) throws SerialException;
    public abstract void allume_ventilo() throws SerialException;
    public abstract void eteint_ventilo() throws SerialException;
	
	public void deposer_fresques() throws SerialException
	{
		fresques_posees = true;
	}
	public void prendre_torche(Cote cote) throws SerialException
	{
        if(cote == Cote.GAUCHE)
            tient_feu_gauche = true;
        else
           tient_feu_droite = true;
		
	}
    public void takefire(Cote cotePrise, Cote coteReel) throws SerialException, MouvementImpossibleException
	{
		if(cotePrise == Cote.GAUCHE)
            tient_feu_gauche = true;
        else
           tient_feu_droite = true;
	}
	public void add_fruits(int n)
	{
	    nombre_fruits_bac += n;
	}
	
    public void bac_haut() throws SerialException
    {
       nombre_fruits_bac = 0;
    }
    public void poserFeuBonCote(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            tient_feu_gauche = false;
        else
           tient_feu_droite = false;
    }
    public void poserFeuEnRetournant(Cote cote) throws SerialException
    {
       if(cote == Cote.GAUCHE)
            tient_feu_gauche = false;
        else
           tient_feu_droite = false;
    }

	// Dépendances
	protected Read_Ini config;
	protected Log log;

	/* Ces attributs sont nécessaires à robotvrai et à robotchrono, donc ils sont ici.
	 * Cela regroupe tous les attributs ayant une conséquence dans la stratégie
	 */
	protected boolean symetrie;	
	protected int nombre_lances = 6;
	protected boolean fresques_posees = false;
	protected int nombre_fruits_bac = 0;
	protected boolean tient_feu_droite = false;
	protected boolean tient_feu_gauche = false;
	protected boolean feu_tenu_gauche_rouge = false;
	protected boolean feu_tenu_droite_rouge = false;
	protected Vitesse vitesse = Vitesse.ENTRE_SCRIPTS;
	
	public Robot(Read_Ini config, Log log)
	{
		this.config = config;
		this.log = log;
		maj_config();
	}
		
	public void maj_config()
	{
		symetrie = config.get("couleur").equals("rouge");
	}
	
	public Vitesse get_vitesse_() {
		return vitesse;
	}

	public int getNbrLances() {
		return nombre_lances;
	}

	public int get_nombre_fruits_bac() {
		return nombre_fruits_bac;
	}
	
	public boolean isFresquesPosees()
	{
		return fresques_posees;
	}
	
	public void setTient_feu(Cote cote)
	{
		if(cote == Cote.GAUCHE)
			tient_feu_gauche = true;
		else
			tient_feu_droite = true;
	}

	public void setTientPas_feu(Cote cote)
	{
		if(cote == Cote.GAUCHE)
			tient_feu_gauche = false;
		else
			tient_feu_droite = false;
	}
	
	public boolean isTient_feu(Cote cote)
	{
		if(cote == Cote.GAUCHE)
			return tient_feu_gauche;
		else
			return tient_feu_droite;
	}
	
	public boolean isFeu_tenu_rouge(Cote cote)
	{
		if(cote == Cote.GAUCHE)
			return feu_tenu_gauche_rouge;
		else
			return feu_tenu_droite_rouge;
	}
	
	public void setFeu_tenu_rouge(Cote cote, Colour colour)
	{
		if(cote == Cote.GAUCHE)
			feu_tenu_gauche_rouge = (colour == Colour.RED);
		else
			feu_tenu_droite_rouge = (colour == Colour.RED);			
	}
	
	
	public void tourner_relatif(double angle) throws MouvementImpossibleException
	{
		tourner(getOrientation() + angle, null, false);
	}

    public void tourner(double angle) throws MouvementImpossibleException
    {
        tourner(angle, null, false);
    }

    public void tourner_sans_symetrie(double angle) throws MouvementImpossibleException
    {
        if(symetrie)
            tourner(Math.PI-angle, null, false);
        else
            tourner(angle, null, false);
    }


    public void avancer(int distance) throws MouvementImpossibleException
    {
        avancer(distance, null, false);
    }

    public void avancer(int distance, ArrayList<Hook> hooks) throws MouvementImpossibleException
    {
        avancer(distance, hooks, false);
    }

    public void avancer_dans_mur(int distance) throws MouvementImpossibleException
    {
        Vitesse sauv_vitesse = vitesse; 
        set_vitesse(Vitesse.DANS_MUR);
        avancer(distance, null, true);
        set_vitesse(sauv_vitesse);
    }
    
    /**
     * Va au point "arrivée" en utilisant le pathfinding.
     * @param arrivee
     * @param hooks
     * @param insiste
     * @throws PathfindingException
     * @throws MouvementImpossibleException
     */
    public boolean va_au_point_pathfinding(Pathfinding pathfinding, Vec2 arrivee, ArrayList<Hook> hooks, boolean insiste) throws PathfindingException, MouvementImpossibleException
    {
        /* On demande au pathfinding simple un itinéraire
         * - si on a une exception de Pathfinding (chemin non trouvé), on utilise le A*
         * - si on a une exception de MouvementImpossible:
         *      - si on insiste, on reprend ça (nouveau chemin avec pathfinding simple, A* si pas de chemin trouvé)
         *      - si on n'insiste pas, on lève une exception
         */
        boolean out = false;
        log.debug("A", this);
        ArrayList<Vec2> chemin;
        try 
        {
        	Vec2 position_robot = getPosition();
		    if(symetrie)
		    	position_robot.x *= -1;

            log.debug("B", this);
            chemin = pathfinding.chemin(position_robot, arrivee, insiste);
            for(Vec2 point: chemin)
            	log.debug(point, this);
            suit_chemin(chemin, hooks);
            log.debug("Robot en "+getPosition(), this);
            out = true;
        }
        catch (MouvementImpossibleException e)
        {
            log.debug("C", this);
            e.printStackTrace();
            if(insiste)
            {
            	Vec2 position_robot = getPosition();
    		    if(symetrie)
    		    	position_robot.x *= -1;
                log.debug("D", this);
                chemin = pathfinding.chemin(position_robot, arrivee, insiste);
                log.debug("E", this);
                tourner(getOrientation()+Math.PI);
                suit_chemin(chemin, hooks);
                log.debug("F", this);
                out = true;
            }
            else throw e;
        }
        return out;
    }

    public abstract void desactiver_asservissement_rotation();
    public abstract void activer_asservissement_rotation();
    
}

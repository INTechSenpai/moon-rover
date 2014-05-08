package robot;

import java.util.ArrayList;

import pathfinding.Pathfinding;
import hook.Hook;
import smartMath.Vec2;
import container.Service;
import enums.Colour;
import enums.Cote;
import enums.PositionRateau;
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
	public abstract void set_vitesse_translation(String vitesse);
	public abstract void set_vitesse_rotation(String vitesse);
	
	public abstract void setPosition(Vec2 position);
	public abstract void setOrientation(double orientation);
    public abstract Vec2 getPosition();
    public abstract double getOrientation();
    public abstract void sleep(long duree);
    public abstract void setInsiste(boolean insiste);

	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
    public void copy(Robot rc)
    {
        rc.setPosition(getPosition().clone());
        rc.setOrientation(getOrientation());
        rc.set_vitesse_rotation(vitesse_rotation);
        rc.set_vitesse_translation(vitesse_translation);
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
	private String vitesse_translation;
	private String vitesse_rotation;
	
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
	
	protected int conventions_vitesse_translation(String vitesse)
	{
		vitesse_translation = vitesse;
        if(vitesse == "entre_scripts")
        	return 170;
        else if(vitesse == "dans_mur")
            return 90;        
        else if(vitesse == "recal_faible")
            return 90;
        else if(vitesse == "recal_forte")
            return 120;
        else if(vitesse == "arbre_arriere")
        	return 50; // TODO
        else if(vitesse == "arbre_avant")
        	return 60; // TODO
        else if(vitesse == "torche")
        	return 40; //TODO
        else
        {
        	log.warning("Erreur vitesse translation: "+vitesse, this);
        	return 150;
        }
	}

	protected int conventions_vitesse_rotation(String vitesse)
	{
		vitesse_rotation = vitesse;
        if(vitesse == "entre_scripts")
        	return 160;
        else if(vitesse == "recal_faible")
            return 120;
        else if(vitesse == "prise_feu")
            return 60;
        else if(vitesse == "recal_forte")
            return 130;
        else
        {
        	log.warning("Erreur vitesse rotation: "+vitesse, this);
        	return 160;
        }
	}
	
	public String get_vitesse_translation() {
		return vitesse_translation;
	}

	public String get_vitesse_rotation() {
		return vitesse_rotation;
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
	
	protected void setFeu_tenu_rouge(Cote cote, Colour colour)
	{
		if(cote == Cote.GAUCHE)
			feu_tenu_gauche_rouge = (colour == Colour.RED);
		else
			feu_tenu_droite_rouge = (colour == Colour.RED);			
	}
	
	
	public void tourner_relatif(float angle) throws MouvementImpossibleException
	{
		tourner(getOrientation() + angle, null, false);
	}

    public void tourner(float angle) throws MouvementImpossibleException
    {
        tourner(angle, null, false);
    }

    public void tourner_sans_symetrie(float angle) throws MouvementImpossibleException
    {
        if(symetrie)
            tourner((float) (Math.PI-angle), null, false);
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
        String sauv_vitesse = vitesse_translation; 
        set_vitesse_translation("dans_mur");
        avancer(distance, null, true);
        set_vitesse_translation(sauv_vitesse);
    }
    
    /**
     * Va au point "arrivée" en utilisant le pathfinding.
     * @param arrivee
     * @param hooks
     * @param insiste
     * @throws PathfindingException
     * @throws MouvementImpossibleException
     */
    public void va_au_point_pathfinding(Pathfinding pathfinding, Vec2 arrivee, ArrayList<Hook> hooks) throws PathfindingException, MouvementImpossibleException
    {
        // S'il y a une exception du pathfinding, on remonte la remonte
        // S'il y a une exception de mouvement (ennemi ou mur), on cherche un nouveau chemin.
        boolean exceptionMouvementImpossible;
        do {
            exceptionMouvementImpossible = false;
            ArrayList<Vec2> chemin = pathfinding.cheminAStar(getPosition(), arrivee);
            try
            {
                suit_chemin(chemin, hooks);
            } catch (MouvementImpossibleException e)
            {
                exceptionMouvementImpossible = true;
            }        
        } while(exceptionMouvementImpossible);
    }

}

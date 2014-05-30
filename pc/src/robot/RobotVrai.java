package robot;

import robot.hautniveau.ActionneursHautNiveau;
import robot.hautniveau.CapteurSimulation;
import robot.hautniveau.DeplacementsHautNiveau;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;
import hook.Hook;

import java.util.ArrayList;

import enums.Colour;
import enums.Cote;
import enums.PositionRateau;
import enums.Vitesse;
import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;

/**
 * Classe qui fournit des déplacements haut niveau
 * @author pf
 *
 */

public class RobotVrai extends Robot {

	private ActionneursHautNiveau actionneurs;
	private Table table;
	private DeplacementsHautNiveau deplacements;
	private CapteurSimulation capteur_simulation;

	// Constructeur
	public RobotVrai(CapteurSimulation capteur_simulation, ActionneursHautNiveau actionneurs, DeplacementsHautNiveau deplacements, Table table, Read_Ini config, Log log)
 	{
		super(config, log);
		this.capteur_simulation = capteur_simulation;
		this.actionneurs = actionneurs;
		this.deplacements = deplacements;
		this.table = table;
		maj_config();
		vitesse = Vitesse.ENTRE_SCRIPTS;		
	}
	
	/*
	 * MÉTHODES PUBLIQUES
	 */
	
	public void maj_config()
	{
		super.maj_config();
	}
	
	public void desactiver_asservissement_rotation()
	{
		deplacements.desactiver_asservissement_rotation();
	}

	public void activer_asservissement_rotation()
	{
		deplacements.activer_asservissement_rotation();
	}

	// La couleur est simulée. Normalement, vu la disposition des couleurs, cela devrait se faire assez bien.
	public Colour getColour()
	{
	    return capteur_simulation.getColour(deplacements.getPosition(), deplacements.getOrientation());
	}
	
	public void recaler()
	{
	    set_vitesse(Vitesse.RECALER);
	    deplacements.recaler();
	}
	
	/**
	 * Avance d'une certaine distance (méthode bloquante), gestion des hooks
	 * @throws MouvementImpossibleException 
	 */
	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws MouvementImpossibleException
	{
		deplacements.avancer(distance, hooks, mur);
	}	

	/**
	 * Modifie la vitesse de translation
	 */
	@Override
	public void set_vitesse(Vitesse vitesse)
	{
        deplacements.set_vitesse_translation(vitesse.PWM_translation);
        deplacements.set_vitesse_rotation(vitesse.PWM_rotation);
		log.debug("Modification de la vitesse: "+vitesse, this);
	}
	
	@Override
    public void setInsiste(boolean insiste)
    {
        deplacements.setInsiste(insiste);
    }

	/*
	 * ACTIONNEURS
	 */

	public void initialiser_actionneurs_deplacements()
	{
	    actionneurs.initialiser_actionneurs();
	    deplacements.initialiser_deplacements();
	}

	@Override
	public void tirerBalle()
	{
        super.tirerBalle();
		log.debug("On lance une balle!", this);
		actionneurs.tirerBalle();
	}

	@Override
	public void takefire(Cote cotePrise, Cote coteReel) throws SerialException, MouvementImpossibleException {

//	    if(coteReel == Cote.MILIEU)
	        
	    if(!isTient_feu(cotePrise))
		{
			int signe = 1;
			if(cotePrise == Cote.GAUCHE)
				signe = -1;
			Vitesse vitesse_sauv = vitesse;
			stopper();
			avancer(-150);
            tourner_relatif(-signe*0.4f);
			ouvrir_bas_pince(cotePrise);
			tourner_relatif(signe*0.2f);
			sleep(600);
			avancer(250);
			presque_fermer_pince(cotePrise);
			set_vitesse(Vitesse.PRISE_FEU);
			tourner_relatif(-signe*0.3f);
			set_vitesse(vitesse_sauv);
			avancer(30);
			fermer_pince(cotePrise);
			sleep(500);
			lever_pince(cotePrise);
			sleep(500);
			super.takefire(cotePrise, coteReel);
			// On signale à la table qu'on a prit un feu. A priori, c'est le plus proche de cette position.
			table.pickFire(table.nearestUntakenFire(deplacements.getPosition().clone()));
	        setFeu_tenu_rouge(cotePrise, getColour());
		}
	}

	@Override
	public void bac_bas() throws SerialException
	{
		actionneurs.bac_bas();
	}

    @Override
    public void bac_tres_bas() throws SerialException
    {
        actionneurs.bac_tres_bas();
    }

	@Override
	public void bac_haut() throws SerialException
	{
		super.bac_haut();
		actionneurs.bac_haut();
	}

	@Override
	public void deposer_fresques() throws SerialException {
		super.deposer_fresques();
	}

	@Override	
	public void lancerFilet() throws SerialException
	{
		stopper();
		deplacements.desasservit();
		actionneurs.lancerFilet();
	}

	@Override
	public void milieu_pince(Cote cote) throws SerialException
	{
		actionneurs.milieu_pince(cote);
	}

	@Override
	public void baisser_pince(Cote cote) throws SerialException
	{
	    actionneurs.baisser_pince(cote);
	}	

	@Override	
	public void lever_pince(Cote cote) throws SerialException
	{
	    actionneurs.lever_pince(cote);
	}

	@Override
	public void ouvrir_pince(Cote cote) throws SerialException
	{
		actionneurs.ouvrir_bas_pince(cote);
	}

	@Override	
	public void fermer_pince(Cote cote) throws SerialException
	{
		actionneurs.fermer_pince(cote);
	}
	@Override
	public void presque_fermer_pince(Cote cote) throws SerialException
	{
		actionneurs.presque_fermer_pince(cote);
	}
	@Override
	public void ouvrir_bas_pince(Cote cote) throws SerialException
	{
		actionneurs.ouvrir_bas_pince(cote);
	}
	@Override
	public void prendre_torche(Cote cote) throws SerialException
	{
		super.prendre_torche(cote);
		actionneurs.prendre_torche(cote);
	}
		
	@Override
	public void tourner_pince(Cote cote) throws SerialException
	{
		actionneurs.tourner_pince(cote);
	}
	
	@Override	
	public void poserFeuBonCote(Cote cote) throws SerialException
	{
	    super.poserFeuBonCote(cote);
	    actionneurs.poserFeuBonCote(cote);
		log.debug("On pose le feu gauche sans le retourner", this);
	}

	@Override	
	public void poserFeuEnRetournant(Cote cote) throws SerialException
	{
	    super.poserFeuEnRetournant(cote);
	    actionneurs.poserFeuEnRetournant(cote);
		log.debug("On pose le feu gauche en le retournant", this);
	}
	public void renverserFeu(Cote cote) throws SerialException
	{
		if(cote == Cote.GAUCHE)
			actionneurs.renverserFeuGauche();
		else
			actionneurs.renverserFeuDoite();
	}
	

	
	/* 
	 * GETTERS & SETTERS
	 */
	@Override
	public void setPosition(Vec2 position)
	{
	    deplacements.setPosition(position);
	}
	
    @Override
	public Vec2 getPosition()
	{
	    return deplacements.getPosition();
	}

    @Override
    public Vec2 getPositionFast()
    {
        return deplacements.getPositionFast();
    }

	@Override
	public void setOrientation(double orientation)
	{
	    deplacements.setOrientation(orientation);
	}

    @Override
    public double getOrientation()
    {
        return deplacements.getOrientation();
    }

    @Override
    public double getOrientationFast()
    {
        return deplacements.getOrientationFast();
    }

	/**
	 * Méthode sleep utilisée par les scripts
	 */
	@Override	
	public void sleep(long duree)
	{
		Sleep.sleep(duree);
	}

    @Override
    public void stopper()
    {
        deplacements.stopper();
    }

    @Override
    public void tourner(double angle, ArrayList<Hook> hooks, boolean mur) throws MouvementImpossibleException
    {
        deplacements.tourner(angle, hooks, mur);
    }
    
    @Override
    public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks) throws MouvementImpossibleException
    {
        deplacements.suit_chemin(chemin, hooks);
    }

    @Override
    public void rateau(PositionRateau position, Cote cote)
            throws SerialException
    {
        actionneurs.rateau(position, cote);
    }

    @Override
    public void copy(RobotChrono rc)
    {
        super.copy(rc);
        getPositionFast().copy(rc.position);
        rc.orientation = getOrientationFast();
    }

    @Override
    public void allume_ventilo() throws SerialException
    {
        actionneurs.allume_ventilo();
    }

    @Override
    public void eteint_ventilo() throws SerialException
    {
        actionneurs.eteint_ventilo();
    }

}

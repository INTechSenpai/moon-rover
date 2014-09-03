package robot.hautniveau;

import container.Service;
import enums.Cote;
import enums.PositionRateau;
import exceptions.serial.SerialException;
import robot.cartes.Actionneurs;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;

/**
 * Classe qui fournit au robot des méthodes plus haut niveau que Actionneurs
 * @author pf
 *
 */

public class ActionneursHautNiveau implements Service
{
    
    private Actionneurs actionneurs;
    
    public ActionneursHautNiveau(Read_Ini config, Log log, Actionneurs actionneurs)
    {
        this.actionneurs = actionneurs;
        maj_config();
    }


    public void rateau(PositionRateau position, Cote cote) throws SerialException
    {
        if(position == PositionRateau.BAS && cote == Cote.DROIT)
            actionneurs.rateau_bas_droit();
        else if(position == PositionRateau.BAS && cote == Cote.GAUCHE)
            actionneurs.rateau_bas_gauche();
        else if(position == PositionRateau.HAUT && cote == Cote.DROIT)
            actionneurs.rateau_haut_droit();
        else if(position == PositionRateau.HAUT && cote == Cote.GAUCHE)
            actionneurs.rateau_haut_gauche();
        else if(position == PositionRateau.RANGER && cote == Cote.DROIT)
            actionneurs.rateau_ranger_droit();
        else if(position == PositionRateau.RANGER && cote == Cote.GAUCHE)
            actionneurs.rateau_ranger_gauche();
        else if(position == PositionRateau.SUPER_BAS && cote == Cote.DROIT)
            actionneurs.rateau_super_bas_droit();
        else if(position == PositionRateau.SUPER_BAS && cote == Cote.GAUCHE)
            actionneurs.rateau_super_bas_gauche();
    }

    public void initialiser_actionneurs()
    {
        try {
            actionneurs.rateau_ranger_droit();
            actionneurs.rateau_ranger_gauche();     
            actionneurs.fermer_pince_droite();
            actionneurs.fermer_pince_gauche();
            actionneurs.lever_pince_droite();
            actionneurs.lever_pince_gauche();
            actionneurs.bac_bas();
            actionneurs.recharger();
        } catch (SerialException e) {
            e.printStackTrace();
        }
    }
    
    public void tirerBalle()
    {
        try
        {
            actionneurs.tirerBalle();
        } catch (SerialException e)
        {
            e.printStackTrace();
        }        
    }

    public void bac_bas()
    {
        try
        {
            actionneurs.bac_bas();
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
    }
    
    public void bac_tres_bas()
    {
        try
        {
            actionneurs.bac_tres_bas();
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
    }


    public void bac_haut()
    {
        try
        {
            actionneurs.bac_haut();
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
    }
    
    public void milieu_pince(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.milieu_pince_gauche();
        else
            actionneurs.milieu_pince_droite();
    }

    public void baisser_pince(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.baisser_pince_gauche();
        else
            actionneurs.baisser_pince_droite();
    }   

    public void lever_pince(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.lever_pince_gauche();
        else
            actionneurs.lever_pince_droite();
    }

    public void ouvrir_pince(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.ouvrir_pince_gauche();
        else
            actionneurs.ouvrir_pince_droite();
    }

    public void fermer_pince(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.fermer_pince_gauche();
        else
            actionneurs.fermer_pince_droite();
    }

    public void presque_fermer_pince(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.presque_fermer_pince_gauche();
        else
            actionneurs.presque_fermer_pince_droite();
    }

    public void ouvrir_bas_pince(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.ouvrir_bas_pince_gauche();
        else
            actionneurs.ouvrir_bas_pince_droite();
    }

    public void prendre_torche(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.prendre_torche_gauche();
        else
            actionneurs.prendre_torche_droite();
    }
    
    public void tourner_pince(Cote cote) throws SerialException
    {
        if(cote == Cote.GAUCHE)
            actionneurs.tourner_pince_gauche();
        else
            actionneurs.tourner_pince_droite();
    }

    public void lancerFilet() throws SerialException
    {
        actionneurs.lancerFilet();
    }
    
    public void poserFeuEnRetournant(Cote cote) throws SerialException
    {
        //Ca remonte la pince aussi !
    	milieu_pince(cote);
    	Sleep.sleep(700);
        tourner_pince(cote);
        Sleep.sleep(700);
        ouvrir_pince(cote);
        Sleep.sleep(700);
        lever_pince(cote);
        Sleep.sleep(700);
        fermer_pince(cote);
        Sleep.sleep(700);
    }

    public void poserFeuBonCote(Cote cote) throws SerialException
    {
        milieu_pince(cote);
        Sleep.sleep(700);
        ouvrir_pince(cote);
        Sleep.sleep(700);
        lever_pince(cote);
        Sleep.sleep(700);
        fermer_pince(cote);
        Sleep.sleep(700);
    }

    public void allume_ventilo() throws SerialException
    {
        actionneurs.allume_ventilo();
    }

    public void eteint_ventilo() throws SerialException
    {
        actionneurs.eteint_ventilo();
    }

	public void renverserFeuGauche() throws SerialException
	{
		actionneurs.renverserFeuGauche();
	}
	public void renverserFeuDoite() throws SerialException
	{
		actionneurs.renverserFeuDoite();
	}
    @Override
    public void maj_config()
    {}

}

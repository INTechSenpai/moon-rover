package enums;

/**
 * Enumération des vitesses disponibles.
 * @author pf
 *
 */

public enum Vitesse
{
    ENTRE_SCRIPTS(170, 160),
    DANS_MUR(90, 160),
    RECALER(90, 90),
    ARBRE_AVANT(100, 160),
    PRISE_FEU(90, 140),
    DEBUT(250, 70);
    
    public int PWM_translation;
    public int PWM_rotation;
    public int inverse_vitesse_mmpms;
    public int inverse_vitesse_rpms;
        
    private Vitesse(int PWM_translation, int PWM_rotation)
    {
        this.PWM_translation = PWM_translation;
        this.PWM_rotation = PWM_rotation;
        inverse_vitesse_mmpms = (int) (1./(((float)2500)/((float)613.52 * (float)(Math.pow((double)PWM_translation,(double)(-1.034))))/1000));
        inverse_vitesse_rpms = (int) (1./(((float)Math.PI)/((float)277.85 * (float)Math.pow(PWM_rotation,(-1.222)))/1000));
    }

    
}

package hook.sortes;

import hook.Hook;
import enums.Cote;
import robot.RobotVrai;
import robot.cartes.Capteurs;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe des hook de capteur de feu, qui hérite de la classe hook
 * @author pf
 *
 */

class HookFeu extends Hook {

	private Capteurs capteur;
	Cote cote;
	
	public HookFeu(Read_Ini config, Log log, GameState<RobotVrai> real_state, Capteurs capteur, Cote cote)
	{
		super(config, log, real_state);
		this.capteur = capteur;
		this.cote = cote;
	}
	
	public boolean evaluate()
	{
	    // TODO: si le robot détecte un feu à gauche et que sa pince gauche est prise, alors il prend le feu à droite...
		// si on tient déjà un feu de ce côté...
		if(real_state.robot.isTient_feu(cote))
			return false;
		
		// on regarde à gauche ou à droite selon la valeur de "gauche"
		if(cote == Cote.GAUCHE && capteur.isThereFireGauche() || cote == Cote.DROIT && capteur.isThereFireDroit())
		{
			if(cote == Cote.GAUCHE)
				log.warning("Un feu a été détecté à gauche! Il est pris.", this);
			else
				log.warning("Un feu a été détecté à droite! Il est pris.", this);
			return declencher();
		}
		return false;
	}
	
}

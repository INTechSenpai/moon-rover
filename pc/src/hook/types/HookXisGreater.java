package hook.types;

import hook.types.HookX;
import robot.RobotReal;
import strategie.GameState;
import utils.Log;
import utils.Config;

/**
 * déclenche un évènement dès que le robot a une coordonnée X (sur la table) supérieure a celle fournie
 * @author pf, marsu
 *
 */

class HookXisGreater extends HookX
{

	/**
	 * TODO doc
	 * @param config
	 * @param log
	 * @param real_state
	 * @param abscisse
	 * @param tolerance
	 * @param effectuer_symetrie
	 */
    public HookXisGreater(Config config, Log log,GameState<RobotReal> real_state, float abscisse, float tolerance,boolean effectuer_symetrie)
    {
		super(config, log, real_state, abscisse, tolerance, effectuer_symetrie);
	}
    
	/**
	 * TODO: Doc
	 */
    @Override
    public boolean evaluate()
    {
        if(real_state.robot.getPosition().x > xValue)
            return declencher();

        return false;
    }
    
}

package hook.sortes;

import hook.Hook;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe des hook d'ordonnée, qui hérite de la classe hook
 * @author pf
 *
 */

class HookOrdonneeHaut extends Hook {

    private float ordonnee;
    
    public HookOrdonneeHaut(Read_Ini config, Log log, GameState<RobotVrai> real_state, float ordonnee, boolean effectuer_symetrie)
    {
        super(config, log, real_state);
        this.ordonnee = ordonnee;
    }
    
    public boolean evaluate()
    {
        Vec2 positionRobot = real_state.robot.getPosition();

        if(positionRobot.y > ordonnee)
            return declencher();

        return false;
    }
    
}

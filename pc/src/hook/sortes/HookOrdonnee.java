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

class HookOrdonnee extends Hook {

    private float ordonnee;
    private float tolerance;
    
    public HookOrdonnee(Read_Ini config, Log log, GameState<RobotVrai> real_state, float ordonnee, float tolerance, boolean effectuer_symetrie)
    {
        super(config, log, real_state);
        this.ordonnee = ordonnee;
        this.tolerance = tolerance;
    }
    
    public boolean evaluate()
    {
        Vec2 positionRobot = real_state.robot.getPosition();

        if(Math.abs(positionRobot.y-ordonnee) < tolerance)
            return declencher();

        return false;
    }
    
}

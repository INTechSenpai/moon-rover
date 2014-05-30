package hook.sortes;

import hook.Hook;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe des hook d'abscisse, qui hérite de la classe hook
 * @author pf
 *
 */

class HookAbscisseDroite extends Hook {

    private float abscisse;
    
    public HookAbscisseDroite(Read_Ini config, Log log, GameState<RobotVrai> real_state, float abscisse, boolean effectuer_symetrie)
    {
        super(config, log, real_state);
        this.abscisse = abscisse;
        if(effectuer_symetrie)
            this.abscisse *= -1;
    }
    
    public boolean evaluate()
    {
        Vec2 positionRobot = real_state.robot.getPosition();

        if(positionRobot.x > abscisse)
            return declencher();

        return false;
    }
    
}

package hook.sortes;

import hook.Hook;
import robot.RobotVrai;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe hook d'abscisse, qui hérite de la classe hook
 * @author pf
 *
 */

class HookAbscisse extends Hook
{

	private float abscisse;
	private float tolerance;
	
	public HookAbscisse(Read_Ini config, Log log, GameState<RobotVrai> real_state, float abscisse, float tolerance, boolean effectuer_symetrie)
	{
	    super(config, log, real_state);
		this.abscisse = abscisse;
		this.tolerance = tolerance;
		if(effectuer_symetrie)
			abscisse *= -1;
	}
	
	public boolean evaluate()
	{
		Vec2 positionRobot = real_state.robot.getPosition();

		if(Math.abs(positionRobot.x-abscisse) < tolerance)
			return declencher();

		return false;
	}
	
}

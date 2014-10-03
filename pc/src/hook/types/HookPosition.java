package hook.types;

import hook.Hook;
import robot.RobotReal;
import smartMath.Vec2;
import strategie.GameState;
import utils.Log;
import utils.Config;

/**
 * Classe des hook de position, qui hérite de la classe hook
 * @author pf
 *
 */

class HookPosition extends Hook {

	private Vec2 position;
	private int toleranceCarre;
	
	public HookPosition(Config config, Log log, GameState<RobotReal> real_state, Vec2 position, int tolerance, boolean effectuer_symetrie)
	{
		super(config, log, real_state);
		this.position = position;
		this.toleranceCarre = tolerance*tolerance;
		if(effectuer_symetrie)
			position.x *= -1;
	}
	
	public boolean evaluate()
	{
		Vec2 positionRobot = real_state.robot.getPosition();
		if(position.SquaredDistance(positionRobot) <= toleranceCarre)
			return declencher();
		return false;
	}
	
}

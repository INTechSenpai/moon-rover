package hook;

import robot.Robot;
import smartMath.Vec2;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe des hook de position, qui hérite de la classe hook
 * @author pf
 *
 */

class HookPosition extends Hook {

	private Vec2 position;
	private int tolerance;
	
	public HookPosition(Read_Ini config, Log log, Vec2 position, int tolerance, boolean effectuer_symetrie)
	{
		super(config, log);
		this.position = position;
		this.tolerance = tolerance;
		if(effectuer_symetrie)
			position.x *= -1;
	}
	
	public boolean evaluate(final Robot robot)
	{
		Vec2 positionRobot = robot.getPosition();
		if(position.SquaredDistance(positionRobot) <= tolerance*tolerance)
			return declencher();
		return false;
	}
	
}

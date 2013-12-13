package hook;

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
	
	public void evaluate(Vec2 positionRobot)
	{
		if(position.SquaredDistance(positionRobot) <= tolerance*tolerance)
		{
			declencher();
		}
	}
	
}

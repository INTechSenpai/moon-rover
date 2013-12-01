package utils;

import smartMath.Vec2;

/**
 * Classe des hook de position, qui hérite de la classe hook
 * @author pf
 *
 */

public class HookPosition extends Hook {

	private Vec2 position;
	private int tolerance;
	
	public HookPosition(Vec2 position, int tolerance, boolean effectuer_symetrie)
	{
		this.position = position;
		this.tolerance = tolerance;
		if(effectuer_symetrie)
			position.x *= -1;
	}
	
	public void evaluate(Vec2 positionRobot)
	{
		if(position.dot(positionRobot) <= tolerance*tolerance)
		{
			declencher();
		}
	}
	
}

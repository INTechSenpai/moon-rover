package hook;

import robot.Robot;
import smartMath.Vec2;
import utils.Log;
import utils.Read_Ini;

/**
 * Classe des hook de position, qui hérite de la classe hook
 * Utilisée pour lancer les balles
 * @author pf
 *
 */

class HookAbscisse extends Hook {

	private float abscisse;
	private float tolerance;
	
	public HookAbscisse(Read_Ini config, Log log, float abscisse, float tolerance, boolean effectuer_symetrie)
	{
		super(config, log);
		this.abscisse = abscisse;
		this.tolerance = tolerance;
		if(effectuer_symetrie)
			abscisse *= -1;
	}
	
	public boolean evaluate(final Robot robot)
	{
		Vec2 positionRobot = robot.getPosition();
		log.debug(positionRobot+" "+abscisse, this);
		if(Math.abs(positionRobot.x-abscisse) < tolerance)
			return declencher();
		return false;
	}
	
}

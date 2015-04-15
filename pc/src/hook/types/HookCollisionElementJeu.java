package hook.types;

import hook.Hook;
import exceptions.ChangeDirectionException;
import exceptions.FinMatchException;
import exceptions.ScriptHookException;
import exceptions.WallCollisionDetectedException;
import obstacles.ObstacleCircular;
import obstacles.ObstacleRectangular;
import strategie.GameState;
import utils.Config;
import utils.Log;
import vec2.ReadOnly;
import vec2.Vec2;

/**
 * Hook pour gérer la collision avec les éléments de jeux
 * @author pf
 *
 */

public class HookCollisionElementJeu extends Hook
{
	private ObstacleCircular obstacle;
	private ObstacleRectangular obstacleRobot;
	
	public HookCollisionElementJeu(Config config, Log log, GameState<?> state, ObstacleCircular o) throws FinMatchException
	{
		super(config, log, state);
		obstacle = o;
		obstacleRobot = new ObstacleRectangular(state);
	}

	@Override
	public boolean simulated_evaluate(Vec2<? extends ReadOnly> pointA, Vec2<? extends ReadOnly> pointB, long date)
	{
		ObstacleRectangular r = new ObstacleRectangular(pointA, pointB);
		return r.isColliding(obstacle);
	}
	
    /**
     * Déclenche le hook si la distance entre la position du robot et la position de de déclenchement du hook est inférieure a tolerancy
     * @return true si la position/oriantation du robot a été modifiée.
     * @throws ScriptHookException 
     * @throws WallCollisionDetectedException 
     * @throws ChangeDirectionException 
     */
	public void evaluate() throws FinMatchException, ScriptHookException, WallCollisionDetectedException, ChangeDirectionException
	{
		obstacleRobot.update(state.robot.getPosition(), state.robot.getOrientation());
		if(obstacleRobot.isColliding(obstacle))
			trigger();
	}

}

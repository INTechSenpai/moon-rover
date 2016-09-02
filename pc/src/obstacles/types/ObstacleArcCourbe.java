package obstacles.types;

import java.util.ArrayList;

import pathfinding.astarCourbe.arcs.ArcCourbe;
import utils.Vec2;
import utils.permissions.ReadOnly;

/**
 * Obstacle d'un arc de trajectoire courbe
 * Construit à partir de plein d'obstacles rectangulaires
 * @author pf
 *
 */

public class ObstacleArcCourbe extends Obstacle
{
	public ObstacleArcCourbe()
	{
		super(null);
	}
	
	public void add(ObstacleRectangular o)
	{
		ombresRobot.add(o);
	}

	public void clear()
	{
		ombresRobot.clear();
	}
	
	protected ArrayList<ObstacleRectangular> ombresRobot = new ArrayList<ObstacleRectangular>();

	@Override
	public double squaredDistance(Vec2<ReadOnly> position)
	{
		double min = Double.MAX_VALUE;
		for(ObstacleRectangular o : ombresRobot)
		{
			min = Math.min(min, o.squaredDistance(position));
			if(min == 0)
				return 0;
		}
		return min;
	}

	@Override
	public boolean isColliding(ObstacleRectangular obs) {
		for(ObstacleRectangular o : ombresRobot)
			if(obs.isColliding(o))
				return true;
		return false;
	}

}

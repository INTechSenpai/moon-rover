package table;
import smartMath.Vec2;

/**
 * Super classe des obstacles. Les obstacles fixes en sont (foyer, ...)
 * @author pf
 *
 */
public abstract class Obstacle {

	protected Vec2 position;
	
	public Obstacle (Vec2 position)
	{
		this.position = position;
	}
	
	public abstract Obstacle clone();
	public Vec2 getPosition()
	{
		return this.position;
	}
	
	// Attn, ony supports circular and rectangular objects
	public boolean dans_obstacle(Vec2 pos, Obstacle obstacle)
	{
		if(obstacle instanceof ObstacleRectangulaire)
		{
			Vec2 position_obs = obstacle.getPosition();
			return !(	pos.x < ((ObstacleRectangulaire)obstacle).getLongueur()+position_obs.x &&
						position_obs.x < pos.x &&
						position_obs.y < pos.y &&
						pos.y < position_obs.y+((ObstacleRectangulaire)obstacle).getLargeur()
					);
		}			
		// sinon, c'est qu'il est circulaire
		return   !( pos.distance(obstacle.getPosition()) < ((ObstacleCirculaire)obstacle).getRadius() );
	}
	
}

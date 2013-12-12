package table;
import smartMath.Vec2;

/**
 * Super classe des obstacles. Les obstacles fixes en sont (foyer, ...)
 * @author pf
 *
 */
public class Obstacle {

	protected Vec2 position;
	protected float radius;
	
	public Obstacle (Vec2 position, float rad)
	{
		this.position = position;
		this.radius = rad;
		
	}
	
	public Obstacle clone()
	{
		return new Obstacle(position, radius);
	}
	
}

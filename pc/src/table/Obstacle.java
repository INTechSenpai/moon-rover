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
	
}

package table;
import smartMath.Vec2;

public abstract class Obstacle {

	protected Vec2 position;
	protected float radius;
	
	public Obstacle (Vec2 position, float rad)
	{
		this.position = position;
		this.radius = rad;
		
	}
	
}

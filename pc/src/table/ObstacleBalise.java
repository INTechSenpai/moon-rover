package table;
import smartMath.Vec2;

public class ObstacleBalise extends Obstacle {

	private Vec2 speed;
	
	public ObstacleBalise (Vec2 position, float rad, Vec2 spe)
	{
		super(position,rad);
		this.speed = spe;
	}
}

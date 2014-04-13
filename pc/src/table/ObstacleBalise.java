package table;
import smartMath.Vec2;

/**
 * Obstacles détectés par balise. On connaît leur vitesse.
 * @author pf
 *
 */
class ObstacleBalise extends ObstacleCirculaire {

	private Vec2 speed;
	
	public ObstacleBalise (Vec2 position, int rad, Vec2 speed)
	{
		super(position,rad);
		this.speed = speed;
	}

	public void clone(ObstacleBalise ob)
	{
		super.clone(ob);
		ob.speed = speed;
	}
}

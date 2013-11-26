
public class Position {

	private float x;
	private float y;
	
	public Position(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void somme(Position p2)
	{
		x += p2.getX();
		y += p2.getY();
	}
	
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	
	
}

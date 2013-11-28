import SmartMath.Vec2;

public class Table {

	Feu arrayFire[] = new Feu[16];
	
	public Table()
	{
		arrayFire[0] = new Feu(0, 0, Orientation.GROUND, Colour.RED, new Vec2(100,100));
		
	}
	
}


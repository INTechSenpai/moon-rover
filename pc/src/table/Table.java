package table;

import robot.Orientation;
import smartMath.Vec2;

public class Table {

	Fire arrayFire[] = new Fire[16];
	
	public Table()
	{
		arrayFire[0] = new Fire(new Vec2(1500,1200), 0, 0, Orientation.XPLUS, Colour.YELLOW);
		arrayFire[1] = new Fire(new Vec2(1100,900), 1, 0, Orientation.YPLUS, Colour.YELLOW);
		arrayFire[2] = new Fire(new Vec2(600,1400), 2, 0, Orientation.XPLUS, Colour.YELLOW);
		arrayFire[3] = new Fire(new Vec2(600,900), 3, 1, Orientation.GROUND, Colour.YELLOW);
		arrayFire[4] = new Fire(new Vec2(600,900), 4, 2, Orientation.GROUND, Colour.RED);
		arrayFire[5] = new Fire(new Vec2(600,900), 5, 3, Orientation.GROUND, Colour.YELLOW);
		arrayFire[6] = new Fire(new Vec2(600,400), 6, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[7] = new Fire(new Vec2(200,0), 7, 0, Orientation.YPLUS, Colour.YELLOW);
		arrayFire[8] = new Fire(new Vec2(-200,0), 8, 0, Orientation.YPLUS, Colour.RED);
		arrayFire[9] = new Fire(new Vec2(-600,1400), 9, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[10] = new Fire(new Vec2(-600,900), 10, 1, Orientation.GROUND, Colour.RED);
		arrayFire[11] = new Fire(new Vec2(-600,900), 11, 2, Orientation.GROUND, Colour.YELLOW);
		arrayFire[12] = new Fire(new Vec2(-600,900), 12, 3, Orientation.GROUND, Colour.RED);
		arrayFire[13] = new Fire(new Vec2(-600,400), 13, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[14] = new Fire(new Vec2(-1100,900), 14, 0, Orientation.YPLUS, Colour.RED);
		arrayFire[15] = new Fire(new Vec2(-1500,1200), 15, 0, Orientation.XPLUS, Colour.YELLOW);




	}
	
}


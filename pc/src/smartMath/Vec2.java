/* ============================================================================
 * 				Vec2 class
 * ============================================================================
 * 
 * Bi-dimantionnal vector 2 class. Simple-precision members.
 * Author : Dede
 * Refactoring : Martial
 */

/*
 *	TODO : 	Implement Matrix product, String conversion and parsing
 */
package smartMath;
public class Vec2
{

	public float x;
	public float y;

	public Vec2()
	{
		x = 0;
		y = 0;
	}

	public Vec2(float requestedX, float requestedY)
	{
		x = requestedX;
		y = requestedY;
	}
	
	public float SquaredLength()
	{
		return x*x + y*y;
	}

	public float Length()
	{
		return (float) Math.sqrt(SquaredLength());
	}

	public float dot(Vec2 other)
	{
		return x*other.x + y*other.y;
	}

}


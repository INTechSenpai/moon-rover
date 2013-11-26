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
public class Vec2
{

	public float x;
	public float y;

	public Vector()
	{
		x = 0;
		y = 0;
	}

	public SquaredLength()
	{
		return x*x + y*y;
	}

	public SquaredLength()
	{
		return Math.sqrt(SquaredLength());
	}

	public dot(Vec2 other)
	{
		return x*other.x + y*other.y;
	}

}


package smartMath;


/**
 * 	Simple classe contenant une paire d'entiers
 * 
 * @author Martial
 *
 */
public class IntPair 
{

	public int x;
	public int y;
	
	public IntPair(int a, int b)
	{	
		x = a;
		y = b;
	}
	

	public void set( IntPair other)
	{
		x = other.x;
		y = other.y;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	
	public IntPair makeCopy()
	{
		return new IntPair(x, y);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntPair other = (IntPair) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}

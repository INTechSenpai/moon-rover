package smartMath;

public class LibMath {

	private LibMath()
	{
	}
	
	public static int exponentiation(int a, int b)
	{
		if(b == 0)
			return 1;
		else if(b == 1)
			return a;
		int c = exponentiation(a, b/2);
		if((b&1) == 0)	// si b est pair
			return c*c;
		else
			return c*c*a;
	}

}

package exception;
/**
 * Exception lancée par le service RobotVrai
 * @author Krissprolls
 * Youpi, j'ai fait une exception
 *
 */
public class FunnyActionException extends Exception {

	private static final long serialVersionUID = -5526643375989499071L;

	public FunnyActionException()
	{
		super();
	}
	
	public FunnyActionException(String m)
	{
		super(m);
	}
	
}

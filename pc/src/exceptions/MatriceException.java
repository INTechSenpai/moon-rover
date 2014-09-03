package exceptions;

/**
 * Exception levée en cas de calculs matriciels impossibles
 * @author pf
 *
 */

public class MatriceException  extends Exception {

	private static final long serialVersionUID = -7968975910907981869L;

	public MatriceException()
	{
		super();
	}
	
	public MatriceException(String m)
	{
		super(m);
	}
	

}

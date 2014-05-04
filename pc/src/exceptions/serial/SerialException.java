package exceptions.serial;

/**
 * Exception levée par le serialmanager
 * @author pf
 *
 */
public class SerialException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1826278884421114631L;

	public SerialException()
	{
		super();
	}
	
	public SerialException(String m)
	{
		super(m);
	}
}

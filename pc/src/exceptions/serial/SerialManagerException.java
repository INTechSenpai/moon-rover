package exceptions.serial;

/**
 * Exception levée par le serialmanager
 * @author pf
 *
 */
public class SerialManagerException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1826278884421114631L;

	public SerialManagerException()
	{
		super();
	}
	
	public SerialManagerException(String m)
	{
		super(m);
	}
}

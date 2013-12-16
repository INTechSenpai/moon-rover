package exception;

/**
 * Exception lancée par le service Read_Ini
 * @author pf
 *
 */
public class ConfigException extends Exception {

	private static final long serialVersionUID = -5526643375989499071L;

	public ConfigException()
	{
		super();
	}
	
	public ConfigException(String m)
	{
		super(m);
	}
	
}

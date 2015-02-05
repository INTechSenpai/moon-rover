package exceptions;

/**
 * Exception levée en cas de demande d'un script inconnu
 * @author marsu
 *
 */

public class ScriptException  extends Exception
{
	private static final long serialVersionUID = -3039558414266587469L;

	public ScriptException()
	{
		super();
	}
	
	public ScriptException(String m)
	{
		super(m);
	}
	

}

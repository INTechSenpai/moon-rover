package exceptions.deplacements;

/**
 * Exception levée en cas de détection d'un ennemi proche
 * @author pf
 *
 */
public class CollisionException extends Exception {

	private static final long serialVersionUID = -3791360446545658528L;

	public CollisionException()
	{
		super();
	}
	
	public CollisionException(String m)
	{
		super(m);
	}
	
}

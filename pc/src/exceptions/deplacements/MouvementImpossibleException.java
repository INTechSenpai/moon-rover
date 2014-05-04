package exceptions.deplacements;

/**
 * Exception levée en cas de blocage ou de "collision" (ennemi proche)
 * @author pf
 *
 */
public class MouvementImpossibleException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8139322860107594266L;

	public MouvementImpossibleException()
	{
		super();
	}
	
	public MouvementImpossibleException(String m)
	{
		super(m);
	}
	
}

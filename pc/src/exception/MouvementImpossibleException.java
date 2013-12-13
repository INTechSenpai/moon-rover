package exception;

import robot.RobotVrai;

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
	
	public MouvementImpossibleException(RobotVrai robotvrai)
	{
		super();
		robotvrai.annuleConsigneOrientation();
	}
	
}

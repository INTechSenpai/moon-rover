package utils;

import robot.RobotVrai;
import container.Service;

/**
 * Service qui permettra de faire un checkup du robot avant le match
 * @author pf
 *
 */
public class CheckUp implements Service {

	private Log log;
	private RobotVrai robotvrai;
	
	public CheckUp(Log log, RobotVrai robotvrai)
	{
		this.robotvrai = robotvrai;
		this.log = log;
	}
	
	// TODO check-up du robot
	public void lancer()
	{
		
	}
	
}

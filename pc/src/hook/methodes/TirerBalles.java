package hook.methodes;

import exception.SerialException;
import robot.Robot;
import hook.Executable;

/**
 * Classe implémentant la méthode qui tire des balles.
 * @author pf
 *
 */
public class TirerBalles implements Executable {

	private Robot robot;
	
	public TirerBalles(Robot robot)
	{
		this.robot = robot;
	}

	public void execute() {
		try {
			robot.tirerBalles();
		} catch (SerialException e) {
			e.printStackTrace();
		}
	}

	
}

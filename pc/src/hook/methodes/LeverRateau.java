package hook.methodes;

import exception.SerialException;
import robot.Cote;
import robot.PositionRateau;
import robot.Robot;
import hook.Executable;

/**
 * Classe implémentant la méthode de leverrateau, utilisée lors du ramassage des fruits
 * @author pf
 *
 */

public class LeverRateau implements Executable  {

	private Cote cote;
	private Robot robot;
	
	public LeverRateau(Robot robot, Cote cote)
	{
		this.robot = robot;
		this.cote = cote;
	}
	
	@Override
	public void execute()
	{
		try {
			robot.rateau(PositionRateau.HAUT, cote);
		} catch (SerialException e) {
			e.printStackTrace();
		}
	}	
	
}

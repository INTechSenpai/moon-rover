package hook.methods;

import robot.Robot;
import robot.RobotChrono;
import robot.cardsWrappers.enums.HauteurBrasClap;
import strategie.GameState;
import vec2.ReadWrite;
import exceptions.FinMatchException;
import exceptions.SerialConnexionException;
import hook.Executable;
import enums.Side;

/**
 * Méthode pour baisser un clap
 * @author pf
 *
 */

public class BaisseClap implements Executable
{
	private Robot robot;
	private Side cote;
	
	public BaisseClap(Robot robot, Side cote)
	{
		this.robot = robot;
		this.cote = cote;
	}
	
	@Override
	public void execute() throws FinMatchException {
		try {
			robot.bougeBrasClap(cote, HauteurBrasClap.FRAPPE_CLAP, false);
		} catch (SerialConnexionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateGameState(GameState<RobotChrono,ReadWrite> state)
	{}

}

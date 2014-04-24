package threads;

import exception.SerialException;
import robot.RobotVrai;
import utils.Sleep;

/**
 * Thread qui demande en continu au robot de mettre à jour ses coordonnées
 * @author pf
 *
 */
class ThreadPosition extends AbstractThread {

	private RobotVrai robotvrai;
	
	public boolean robot_pret = false;
	
	ThreadPosition(RobotVrai robotvrai)
	{
		this.robotvrai = robotvrai;
		Thread.currentThread().setPriority(6);
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de mise à jour", this);
		
		do
		{
			if(stop_threads)
				break;
			try {
				robotvrai.update_x_y_orientation();
			} catch (SerialException e) {
				e.printStackTrace();
			}
			robot_pret = true;
			Sleep.sleep(80);
		} while(!ThreadTimer.fin_match);

		log.debug("Arrêt du thread de position", this);
	
	}
	
}

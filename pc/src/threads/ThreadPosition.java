package threads;

import robot.RobotVrai;

/**
 * Thread qui demande en continu au robot de mettre à jour ses coordonnées
 * @author pf
 *
 */
class ThreadPosition extends AbstractThread {

	private RobotVrai robotvrai;
	private ThreadTimer threadTimer;
	
	public boolean robot_pret = false;
	
	ThreadPosition(RobotVrai robotvrai, ThreadTimer threadTimer)
	{
		this.robotvrai = robotvrai;
		this.threadTimer = threadTimer;
	}
	
	@Override
	public void run()
	{
		log.debug("Lancement du thread de mise à jour", this);
		
		do
		{
			if(stop_threads)
				break;
			robotvrai.update_x_y_orientation();
			robot_pret = true;
			sleep(100);
		} while(!threadTimer.fin_match);

		log.debug("Arrêt du thread de position", this);
	
	}

}

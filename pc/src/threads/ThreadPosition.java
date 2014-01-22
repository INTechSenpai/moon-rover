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
			try
			{
				robotvrai.update_x_y_orientation();
				robot_pret = true;
			}
			catch(Exception e)
			{
				log.warning(e.toString(), this);
			}
			sleep(100);
		} while(!threadTimer.fin_match);

		log.debug("Arrêt du thread de position", this);
	
	}

}

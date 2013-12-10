package threads;

import robot.RobotVrai;
import container.Service;

public class ThreadPosition extends AbstractThread {

	RobotVrai robotvrai;
	ThreadTimer threadTimer;
	
	ThreadPosition(Service config, Service log, Service robotvrai, Service threadTimer)
	{
		super(config, log);
		this.robotvrai = (RobotVrai) robotvrai;
		this.threadTimer = (ThreadTimer) threadTimer;
	}
	
	public void run()
	{
		log.debug("Lancement du thread de mise à jour", this);
		
		boolean robot_pret = false;
		
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
		} while(threadTimer.get_fin_match());

		log.debug("Arrêt du thread de position", this);
	
	}

}

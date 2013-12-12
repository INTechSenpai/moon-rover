package threads;

import robot.RobotVrai;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Thread qui demande en continu au robot de mettre à jour ses coordonnées
 * @author pf
 *
 */
public class ThreadPosition extends AbstractThread {

	private RobotVrai robotvrai;
	private ThreadTimer threadTimer;
	
	public boolean robot_pret = false;
	
	ThreadPosition(Read_Ini config, Log log, RobotVrai robotvrai, ThreadTimer threadTimer)
	{
		super(config, log);
		this.robotvrai = robotvrai;
		this.threadTimer = threadTimer;
	}
	
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
		} while(threadTimer.fin_match);

		log.debug("Arrêt du thread de position", this);
	
	}

}

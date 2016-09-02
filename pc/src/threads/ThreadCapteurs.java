package threads;

import obstacles.Capteurs;
import obstacles.SensorsData;
import obstacles.SensorsDataBuffer;
import utils.Config;
import utils.Log;
import container.Service;

/**
 * Thread qui gère les entrées des capteurs
 * @author pf
 *
 */

public class ThreadCapteurs extends Thread implements Service
{
	private SensorsDataBuffer buffer;
	private Capteurs capteurs;
	
	protected Log log;
	
	public ThreadCapteurs(Log log, SensorsDataBuffer buffer, Capteurs capteurs)
	{
		this.log = log;
		this.buffer = buffer;
		this.capteurs = capteurs;
	}
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadRobotCapteurs");
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			while(true)
			{
				SensorsData e = null;
				synchronized(buffer)
				{
					if(buffer.isEmpty())
						buffer.wait();
					e = buffer.poll();
				}
				capteurs.updateObstaclesMobiles(e);
				
			}
		} catch (InterruptedException e2) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
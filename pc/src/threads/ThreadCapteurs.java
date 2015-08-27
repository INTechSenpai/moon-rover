package threads;

import obstacles.Capteurs;
import buffer.IncomingData;
import buffer.IncomingDataBuffer;
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
	private IncomingDataBuffer buffer;
	private Capteurs capteurs;
	
	protected Log log;
	
	public ThreadCapteurs(Log log, IncomingDataBuffer buffer, Capteurs capteurs)
	{
		this.log = log;
		this.buffer = buffer;
		this.capteurs = capteurs;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			IncomingData e = null;
			synchronized(buffer)
			{
				try {
					while(buffer.isEmpty())
						buffer.wait(100);
//					log.debug("Réveil de ThreadObstacleManager");
					e = buffer.poll();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			// Cet appel peut lancer un obstaclemanager.notifyAll()
			// Il n'est pas synchronized car il ne modifie pas le buffer
//			if(e != null)
			if(e.capteursOn)
				capteurs.updateObstaclesMobiles(e);
			
		}
//		log.debug("Fermeture de ThreadObstacleManager");
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
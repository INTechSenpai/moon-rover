package threads;

import obstacles.Capteurs;
import obstacles.IncomingData;
import obstacles.IncomingDataBuffer;
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
					if(buffer.isEmpty())
						buffer.wait();
					e = buffer.poll();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			capteurs.updateObstaclesMobiles(e);
			
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}
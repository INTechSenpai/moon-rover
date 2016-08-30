package threads.serie;

import container.Service;
import serie.BufferIncomingOrder;
import serie.SerieCoucheTrame;
import utils.Config;
import utils.Log;

/**
 * Thread qui s'occupe de la partie bas niveau du protocole série
 * @author pf
 *
 */

public class ThreadSerialInputCoucheTrame extends Thread implements Service
{

	protected Log log;
	private SerieCoucheTrame serie;
	private BufferIncomingOrder buffer;
	
	public ThreadSerialInputCoucheTrame(Log log, SerieCoucheTrame serie, BufferIncomingOrder buffer)
	{
		this.log = log;
		this.serie = serie;
		this.buffer = buffer;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadSerialInputCoucheTrame");
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			while(true)
				buffer.add(serie.readData());
		} catch (InterruptedException e) {
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

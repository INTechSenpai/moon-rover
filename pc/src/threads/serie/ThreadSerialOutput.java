package threads.serie;

import serie.BufferIncomingBytes;
import serie.BufferOutgoingOrder;
import serie.SerieCoucheTrame;
import serie.trame.Order;
import threads.ThreadService;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import enums.SerialProtocol.OutOrder;

/**
 * Thread qui vérifie s'il faut envoyer des choses sur la série
 * @author pf
 *
 */

public class ThreadSerialOutput extends ThreadService
{
	protected Log log;
	private SerieCoucheTrame serie;
	private BufferOutgoingOrder data;
	private int sleep;
	private BufferIncomingBytes input;
	
	public ThreadSerialOutput(Log log, SerieCoucheTrame serie, BufferOutgoingOrder data, BufferIncomingBytes input)
	{
		this.log = log;
		this.serie = serie;
		this.data = data;
		this.input = input;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("ThreadRobotSerialOutput");
		log.debug("Démarrage de "+Thread.currentThread().getName());
		Order message;
		
		// On envoie d'abord le ping long initial
		try {
			serie.init();
			Thread.sleep(50); // on attend que la série soit bien prête
			synchronized(input)
			{
				serie.sendOrder(new Order(OutOrder.PING));
				input.wait(); // on est notifié dès qu'on reçoit quelque chose sur la série
			}
		
			while(true)
			{
				synchronized(data)
				{
					/**
					 * Pour désactiver le ping automatique, remplacer "data.wait(500)" par "data.wait()"
					 */
						
					if(data.isEmpty()) // pas de message ? On attend
//						data.wait(500);
						data.wait();

					if(data.isEmpty()) // si c'est le timeout qui nous a réveillé, on envoie un ping
					{
						message = new Order(OutOrder.PING);
						if(Config.debugSerie)
							log.debug("Envoi d'un ping pour vérifier la connexion");
					}
					else
						message = data.poll();
				}
				serie.sendOrder(message);
				Thread.sleep(sleep); // laisse un peu de temps entre deux trames si besoin est
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		sleep = config.getInt(ConfigInfo.SLEEP_ENTRE_TRAMES);
	}

}

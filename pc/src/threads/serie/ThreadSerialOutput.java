/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package threads.serie;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.SerialClass;
import serie.BufferIncomingBytes;
import serie.BufferOutgoingOrder;
import serie.SerieCoucheTrame;
import serie.SerialProtocol.OutOrder;
import serie.trame.Order;
import threads.ThreadService;
import utils.Log;

/**
 * Thread qui vérifie s'il faut envoyer des choses sur la série
 * @author pf
 *
 */

public class ThreadSerialOutput extends ThreadService implements Configurable, SerialClass
{
	protected Log log;
	private SerieCoucheTrame serie;
	private BufferOutgoingOrder data;
	private int sleep;
	private BufferIncomingBytes input;
	private boolean debugSerie;
	
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
		Thread.currentThread().setName(getClass().getSimpleName());
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
						if(debugSerie)
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
	public void useConfig(Config config)
	{
		debugSerie = config.getBoolean(ConfigInfo.DEBUG_SERIE);
		sleep = config.getInt(ConfigInfo.SLEEP_ENTRE_TRAMES);
	}

}

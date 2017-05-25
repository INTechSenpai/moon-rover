/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import config.Config;
import config.ConfigInfo;
import container.Container;
import container.Container.ErrorCode;
import container.dependances.GUIClass;
import remoteControl.Commandes;
import serie.BufferOutgoingOrder;
import utils.Log;

/**
 * Thread du contrôle à distance
 * 
 * @author pf
 *
 */

public class ThreadRemoteControl extends ThreadService implements GUIClass
{
	private Log log;
	private BufferOutgoingOrder data;
	private Container container;
	private ServerSocket ssocket = null;
	private boolean remote;

	public ThreadRemoteControl(Log log, BufferOutgoingOrder data, Container container, Config config)
	{
		this.log = log;
		this.data = data;
		this.container = container;
		remote = config.getBoolean(ConfigInfo.REMOTE_CONTROL);
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de " + Thread.currentThread().getName());
		try
		{
			if(!remote)
			{
				log.debug(getClass().getSimpleName() + " annulé");
				while(true)
					Thread.sleep(10000);
			}

			ssocket = new ServerSocket(13371);
			control(ssocket.accept());
		}
		catch(InterruptedException | IOException | ClassNotFoundException e)
		{			
			if(ssocket != null && !ssocket.isClosed())
				try
				{
					ssocket.close();
				}
				catch(IOException e1)
				{
					e1.printStackTrace();
					e1.printStackTrace(log.getPrintWriter());
				}

			/*
			 * On arrête tous les threads de socket en cours
			 */
			log.debug("Arrêt de " + Thread.currentThread().getName()+" : "+e);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Surcharge d'interrupt car accept() y est insensible
	 */
	@Override
	public void interrupt()
	{
		try
		{
			if(ssocket != null && !ssocket.isClosed())
				ssocket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			e.printStackTrace(log.getPrintWriter());
		}
		super.interrupt();
	}

	private void control(Socket socket) throws IOException, ClassNotFoundException, InterruptedException
	{
		log.debug("Connexion d'un client !");
		short vitesse = 0;
		short vitesseMax = 1000;
		short pasVitesse = 10;
		double courbure = 0;
		boolean run = false;
		double angleRoues = 0;
		double courbureMax = 3;
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		while(true)
		{
			Commandes tab = (Commandes) in.readObject();
			if(tab == Commandes.SPEED_UP)
			{
				if(vitesse + pasVitesse <= vitesseMax)
				{
					vitesse += pasVitesse;
					data.setMaxSpeed(vitesse);
				}
			}
			else if(tab == Commandes.SPEED_DOWN)
			{
				if(vitesse - pasVitesse >= -vitesseMax)
				{
					vitesse -= pasVitesse;
					data.setMaxSpeed(vitesse);
				}
			}
			else if(tab == Commandes.RUN)
			{
				if(!run)
				{
					data.run();
					run = true;
				}
			}
			else if(tab == Commandes.STOP)
			{
				if(run)
				{
					run = false;
					data.immobilise();
					data.waitStop();
				}
			}
			else if(tab == Commandes.RESET_WHEELS)
			{
				courbure = 0;
				data.setCurvature(courbure);
			}
			else if(tab == Commandes.TURN_LEFT || tab == Commandes.TURN_RIGHT)
			{
				double prochainAngleRoues;
				if(tab == Commandes.TURN_LEFT)		
					prochainAngleRoues = angleRoues + 0.1;
				else
					prochainAngleRoues = angleRoues - 0.1;
					
				double nextCourbure = 200 * Math.tan(prochainAngleRoues);
				if(nextCourbure <= courbureMax)
				{
					angleRoues = prochainAngleRoues;
					courbure = nextCourbure;
					data.setCurvature(courbure);
				}
			}
			else if(tab == Commandes.SHUTDOWN)
				container.interruptWithCodeError(ErrorCode.EMERGENCY_STOP);
		}
	}

}

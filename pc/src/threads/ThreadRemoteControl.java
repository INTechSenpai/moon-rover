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
import serie.Ticket;
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

	private class ThreadListen extends Thread
	{
		protected Log log;
		private ObjectInputStream in;
		private Commandes lu = null;
		
		public ThreadListen(Log log, ObjectInputStream in)
		{
			this.log = log;
			this.in = in;
		}
		
		private boolean isEmpty()
		{
			return lu == null;
		}
		
		private Commandes get()
		{
			Commandes out = lu;
			lu = null;
			return out;
		}

		@Override
		public void run()
		{
			Thread.currentThread().setName(getClass().getSimpleName());
			log.debug("Démarrage de " + Thread.currentThread().getName());
			try
			{
				while(true)
				{
					lu = (Commandes) in.readObject();
					while(lu != null)
						Thread.sleep(1);
				}
			}
			catch(IOException | ClassNotFoundException | InterruptedException e)
			{
				log.debug("Arrêt de " + Thread.currentThread().getName());
				Thread.currentThread().interrupt();
			}
		}

	}

	
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
		short pasVitesse = 50;
		double courbure = 0;
		Ticket run = null;
		double angleRoues = 0;
		double courbureMax = 3;
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		int autostopseuil = 100;
		int autostopcompteur = 0;
		
		ThreadListen t = new ThreadListen(log, in);
		t.start();

		while(true)
		{
			Commandes c = null;
			
			if(!t.isEmpty())
			{
				c = t.get();
				autostopcompteur = 0;
			}
			else
			{
				autostopcompteur++;
				if(autostopcompteur == autostopseuil)
					c = Commandes.STOP;
				Thread.sleep(5);
			}
			
			if(c != null)
				log.debug("On exécute : "+c);

			if(c == null)
				continue;
			else if(c == Commandes.PING);
			else if(c == Commandes.SPEED_UP || c == Commandes.SPEED_DOWN)
			{
				if(run != null && !run.isEmpty()) // le robot s'est arrêté
				{
					vitesse = 0;
					run = null;
				}

				short nextVitesse;
				if(c == Commandes.SPEED_UP)
					nextVitesse = (short) (vitesse + pasVitesse);
				else
					nextVitesse = (short) (vitesse - pasVitesse);
					
				if(nextVitesse <= vitesseMax && nextVitesse >= -vitesseMax)
				{
					vitesse = nextVitesse;
					data.setMaxSpeed(vitesse);
				}
				
				if(vitesse != 0 && run == null)
					run = data.run();
			}
			else if(c == Commandes.STOP)
			{
				if(run != null)
				{
					run = null;
					vitesse = 0;
					data.immobilise();
					data.waitStop();
				}
			}
			else if(c == Commandes.RESET_WHEELS)
			{
				courbure = 0;
				data.setCurvature(courbure);
			}
			else if(c == Commandes.TURN_LEFT || c == Commandes.TURN_RIGHT)
			{
				double prochainAngleRoues;
				if(c == Commandes.TURN_LEFT)		
					prochainAngleRoues = angleRoues + 0.01;
				else
					prochainAngleRoues = angleRoues - 0.01;
				
				double nextCourbure = 1. / 0.2 * Math.tan(prochainAngleRoues);
				
				log.debug(nextCourbure);
			
				if(nextCourbure <= courbureMax)
				{
					angleRoues = prochainAngleRoues;
					courbure = nextCourbure;
					data.setCurvature(courbure);
				}
			}
			else if(c == Commandes.SHUTDOWN)
			{
				Thread.sleep(1000);
				container.interruptWithCodeError(ErrorCode.EMERGENCY_STOP);
			}
		}
	}

}

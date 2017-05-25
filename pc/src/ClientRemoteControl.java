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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import config.Config;
import remoteControl.Commandes;
import utils.Log;

/**
 * Le client du serveur de contrôle à distance
 * 
 * @author pf
 *
 */

public class ClientRemoteControl
{

	public static void main(String[] args) throws InterruptedException
	{
		Log log = new Log();
		Config config = new Config();
		log.useConfig(config);
		
		InetAddress rpiAdresse = null;
		boolean loop = false;
		log.debug("Démarrage du client de contrôle à distance");
		try
		{
			if(args.length != 0)
			{
				for(int i = 0; i < args.length; i++)
				{
					if(args[i].equals("-d"))
						loop = true;
					else if(!args[i].startsWith("-"))
					{
						String[] s = args[i].split("\\."); // on découpe
															// avec les
															// points
						if(s.length == 4) // une adresse ip,
											// probablement
						{
							log.debug("Recherche du serveur à partir de son adresse ip : " + args[i]);
							byte[] addr = new byte[4];
							for(int j = 0; j < 4; j++)
								addr[j] = Byte.parseByte(s[j]);
							rpiAdresse = InetAddress.getByAddress(addr);
						}
						else // le nom du serveur, probablement
						{
							log.debug("Recherche du serveur à partir de son nom : " + args[i]);
							rpiAdresse = InetAddress.getByName(args[i]);
						}
					}
					else
						log.warning("Paramètre inconnu : " + args[i]);
				}
			}

			if(rpiAdresse == null) // par défaut, la raspi (ip fixe)
			{
				rpiAdresse = InetAddress.getByAddress(new byte[] { (byte) 172, 24, 1, 1 });
				log.debug("Utilisation de l'adresse par défaut : " + rpiAdresse);
			}
		}
		catch(UnknownHostException e)
		{
			log.critical("La recherche du serveur a échoué ! " + e);
			return;
		}

		Socket socket = null;
		do
		{

			boolean ko;
			log.debug("Tentative de connexion…");

			do
			{
				try
				{
					socket = new Socket(rpiAdresse, 13371);
					ko = false;
				}
				catch(IOException e)
				{
					Thread.sleep(500); // on attend un peu avant de
										// réessayer
					ko = true;
				}
			} while(ko);

			log.debug("Connexion réussie !");
			Thread.sleep(1000);
			ObjectOutputStream out;
			try
			{
				out = new ObjectOutputStream(socket.getOutputStream());
			}
			catch(IOException e)
			{
				log.warning("Le serveur a coupé la connexion : " + e);
				continue; // on relance la recherche
			}

			try
			{
				while(true)
				{
					out.writeObject(Commandes.SPEED_UP);
					out.flush();
					Thread.sleep(100);
					out.writeObject(Commandes.SPEED_UP);
					out.flush();
					Thread.sleep(100);
					out.writeObject(Commandes.SPEED_UP);
					out.flush();
					Thread.sleep(10000);
					out.writeObject(Commandes.STOP);
					out.flush();
					Thread.sleep(1000);
					out.writeObject(Commandes.SHUTDOWN);
					out.flush();
				}
			}
			catch(IOException e)
			{
				log.warning("Le serveur a coupé la connexion : " + e);
				e.printStackTrace();
			}
			finally
			{
				try
				{
					out.writeObject(Commandes.SHUTDOWN);
					out.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}

		} while(loop);

		try
		{
			socket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		log.debug("Arrêt du client de contrôle à distance");
	}

}

package remoteControl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import config.Config;
import utils.Log;

public class Client extends JPanel implements KeyListener
{
	private static final long serialVersionUID = 9171422634457882975L;
	private JFrame frame;
	private Log log;
	private ObjectOutputStream out;
	private Thread mainThread;
	private int sleep = 100;
	private volatile Commandes current = null;
	private volatile boolean uniqueSent = false;
	
	private class WindowExit extends WindowAdapter
	{
		@Override
		public synchronized void windowClosing(WindowEvent e)
		{
			notify();
			frame.dispose();
		}
	}
	
	public Client(String[] args) throws InterruptedException
	{
		mainThread = Thread.currentThread();
		log = new Log();
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
			try
			{
				out = new ObjectOutputStream(socket.getOutputStream());
			}
			catch(IOException e)
			{
				log.warning("Le serveur a coupé la connexion : " + e);
				continue; // on relance la recherche
			}

			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(100, 100));
			frame = new JFrame();
			frame.addKeyListener(this);
			frame.addWindowListener(new WindowExit());
			frame.getContentPane().add(this);
			frame.pack();
			frame.setVisible(true);
			
			int noSending = 0;
			try
			{
				while(true)
				{
					Thread.sleep(sleep);
					
					Commandes currentTmp = current;
					if(uniqueSent)
						currentTmp = null;
					
					if(currentTmp == null && noSending == 3)
					{
						out.writeObject(Commandes.PING);
						out.flush();
						noSending = 0;
					}
					
					if(currentTmp != null)
					{
						out.writeObject(currentTmp);
						out.flush();
					}
					else
						noSending++;
					
					// pas de spam !
					if(current == Commandes.RESET_WHEELS || current == Commandes.STOP)
						uniqueSent = true;
				}
			}
			catch(IOException e)
			{
				log.warning(e);
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

	@Override
	public synchronized void keyPressed(KeyEvent arg0)
	{
		if(current != null)
			return;
		int code = arg0.getKeyCode();
		log.debug("Pressed : "+arg0.getKeyCode());
		if(out == null)
			return;
		uniqueSent = false;
		try {
			if(code == 38)
				current = Commandes.SPEED_UP;
			else if(code == 40)
				current = Commandes.SPEED_DOWN;
			else if(code == 37)
				current = Commandes.TURN_LEFT;
			else if(code == 39)
				current = Commandes.TURN_RIGHT;
			else if(code == 10)
				current = Commandes.RESET_WHEELS;
			else if(code == 32)
				current = Commandes.STOP;
		} catch(Exception e)
		{
			mainThread.interrupt();
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void keyReleased(KeyEvent arg0)
	{
		current = null;
	}

	@Override
	public synchronized void keyTyped(KeyEvent arg0)
	{}
}

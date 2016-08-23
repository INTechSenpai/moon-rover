package serie;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import container.Service;
import utils.Config;
import utils.Log;
import utils.Sleep;
import exceptions.MissingCharacterException;

/**
 * La connexion série
 * @author pf
 *
 */

public class SerialConnexion implements SerialPortEventListener, Service
{
	private SerialPort serialPort;
	protected Log log;
	
	protected boolean isClosed;
	private int baudrate;
	
	/** The input stream from the port */
	protected InputStream input;

	/** The output stream to the port */
	protected OutputStream output;

	protected boolean busy = false;
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;

	private boolean shouldEstimateLatency = true; // on estime la latence qu'une seule fois
	
	/**
	 * Constructeur pour la série de test
	 * @param log
	 */
	public SerialConnexion(Log log, int baudrate)
	{
		this.log = log;
		this.baudrate = baudrate;
		if(!searchPort())
		{
			/**
			 * Suppression des verrous qui empêchent parfois la connexion
			 */
			try {
				log.critical("Port série non trouvé, suppression des verrous");
				Runtime.getRuntime().exec("sudo rm -f /var/lock/LCK..tty*");
			} catch (IOException e) {
				e.printStackTrace();
			}
			while(!searchPort())
			{
				log.critical("Port série non trouvé, réessaie dans 500 ms");
				Sleep.sleep(500);
			}
		}		
	}

	protected synchronized boolean searchPort()
	{
		busy = true;
		log.debug("Recherche de la série à "+baudrate+" baud");
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();

		while(ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();

			log.debug("Essai du port " + port.getName());
			
			// Test du port
			if(!initialize(port, baudrate))
				continue;
			
			if(ping())
			{
				log.debug("Carte sur " + port.getName());
				if(shouldEstimateLatency)
				{
					shouldEstimateLatency = false;
					estimeLatence();
				}
				
				// Il ne faut activer le listener que maintenant, sinon
				// ça pose des problèmes avec le ping
				serialPort.notifyOnDataAvailable(true);
//				notifyAll();
				busy = false; // voilà, les threads peuvent parler
				return true;
			}
			else
				log.debug(port.getName()+": non");
				
			// Ce n'est pas cette série, on la ferme donc
			serialPort.close();
		}
		// La série n'a pas été trouvée
		return false;
	}
	
	/**
	 * Il donne à la série tout ce qu'il faut pour fonctionner
	 * @param port_name
	 * 					Le port où est connecté la carte
	 * @param baudrate
	 * 					Le baudrate que la carte utilise
	 */
	private boolean initialize(CommPortIdentifier portId, int baudrate)
	{
		try
		{
			serialPort = (SerialPort) portId.open("TechTheTroll", TIME_OUT);
			// set port parameters
			serialPort.setSerialPortParams(baudrate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
//			serialPort.setInputBufferSize(100);
//			serialPort.setOutputBufferSize(100);
//			serialPort.enableReceiveTimeout(100);
//			serialPort.enableReceiveThreshold(1);
			serialPort.notifyOnDataAvailable(false); // on désactive le listener qui pourrait paniquer avec le ping

			// Configuration du Listener
			try {
				serialPort.addEventListener(this);
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}

			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			isClosed = false;
			return true;
		}
		catch (PortInUseException | UnsupportedCommOperationException | IOException e2)
		{
			log.critical(e2);
			return false;
		}
	}
	
	public void communiquer(String out)
	{
		communiquer(out.getBytes());
	}
	
	protected void attendSiPing()
	{
//		log.debug("busy : "+busy);
		// Si la série est occupée, on attend sagement
		if(busy)
			synchronized(this)
			{
				log.debug("Attente du ping");
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				log.debug("Attente du ping finie");
			}
	}


	/**
	 * Doit être appelé quand on arrête de se servir de la série
	 */
	public void close()
	{
		if (!isClosed && serialPort != null)
		{
			log.debug("Fermeture de la carte");
			serialPort.close();
			isClosed = true;
		}
		else if(isClosed)
			log.warning("Carte déjà fermée");
		else
			log.warning("Carte jamais ouverte");
	}

	/**
	 * Gestion d'un évènement sur la série.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent)
	{
//		log.debug("SerialEvent !");
		try {
			if(input.available() > 0)
				notify();
//			else
//				log.debug("Fausse alerte");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean available()
	{
		// tant qu'on est occupé, on dit qu'on ne reçoit rien
		if(busy)
			return false;
		try {
			return input.available() != 0;
		} catch (IOException e) {
			e.printStackTrace(); // Cette exception a lieu si la série est fermée
			return false;
		}
	}
	
	/**
	 * Lit un byte. On sait qu'il doit y en a avoir un.
	 * @return
	 * @throws IOException
	 * @throws MissingCharacterException
	 */
	public int read() throws MissingCharacterException
	{
		attendSiPing();
		try
		{
			if(input.available() == 0)
				Sleep.sleep(10); // On attend un tout petit peu, au cas où
	
			if(input.available() == 0)
				throw new MissingCharacterException(); // visiblement on ne recevra rien de plus
	 
			byte out = (byte) input.read();
			if(Config.debugSerieTrame)
			{
				String s = Integer.toHexString(out).toUpperCase();
				if(s.length() == 1)
					log.debug("Reçu : "+"0"+s);
				else
					log.debug("Reçu : "+s.substring(s.length()-2, s.length()));	
			}
			return out & 0xFF;

		}
		catch(IOException e)
		{
			System.out.println(e);
			throw new MissingCharacterException();
		}
	}
	
	/**
	 * Ping de la carte.
	 * @return l'id de la carte
	 */
	protected boolean ping()
	{
		try
		{
			//Evacuation de l'éventuel buffer indésirable
			output.flush();

			//ping
			output.write(null);

			if(Config.debugSerieTrame)
			{
				log.debug("Question : ");
				afficheMessage(null);
			}

			// on laisse le temps au périphérique de réagir
			Sleep.sleep(100);
			
			byte[] lu = new byte[input.available()];
			int nbLu = input.read(lu);
			if(Config.debugSerieTrame)
			{
				log.debug("Réponse : ");
				afficheMessage(lu);
			}
	
			int i = 0;
			while(i < nbLu && lu[i] != (byte)0x55)
				i++;
			
			
			log.debug("Série trouvée.");
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	protected void estimeLatence()
	{
/*		try {
			log.debug("Estimation de la latence…");
			long avant = System.currentTimeMillis();
			for(int i = 0; i < 10; i++)
			{
				output.write(question);
				while(input.available() == 0);
				input.skip(input.available());
			}
			log.debug("Latence de la série : "+((System.currentTimeMillis() - avant)*50)+" ns.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}

	protected void afficheMessage(byte[] out)
	{
		String m = "";
		for(int i = 0; i < out.length; i++)
		{
			String s = Integer.toHexString(out[i]).toUpperCase();
			if(s.length() == 1)
				m += "0"+s+" ";
			else
				m += s.substring(s.length()-2, s.length())+" ";
		}
		log.debug(m);
	}
	
	/**
	 * Cette méthode est synchronized car deux threads peuvent l'utiliser : ThreadSerialOutput et ThreadSerialOutputTimeout
	 * @param message
	 */
	public synchronized void communiquer(byte[] out)
	{
		/**
		 * Un appel à une série fermée ne devrait jamais être effectué.
		 */
		if(isClosed)
		{
			log.debug("La série est fermée et ne peut envoyer :");
			afficheMessage(out);
			return;
		}

		attendSiPing();
		
		try
		{
			if(Config.debugSerieTrame)
				afficheMessage(out);

			output.write(out);
		}
		catch (Exception e)
		{
			/**
			 * Si la carte ne répond vraiment pas, on recommence de manière infinie.
			 * De toute façon, on n'a pas d'autre choix...
			 */
			log.critical("Ne peut pas parler à la carte. Tentative de reconnexion.");
			while(!searchPort())
			{
				log.critical("Pas trouvé... On recommence");
				// On laisse la série respirer un peu
				Sleep.sleep(200);
			}
			// On a retrouvé la série, on renvoie le message
			communiquer(out);
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}

}

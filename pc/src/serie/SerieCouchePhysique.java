package serie;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import serie.trame.OutgoingFrame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import container.Service;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Sleep;
import exceptions.MissingCharacterException;

/**
 * La connexion série
 * @author pf
 *
 */

public class SerieCouchePhysique implements SerialPortEventListener, Service, SerialInterface
{
	private SerialPort serialPort;
	protected Log log;
	
	protected volatile boolean isClosed;
	private int baudrate;
	
	private String portName;
	
	/** The input stream from the port */
	protected InputStream input;

	/** The output stream to the port */
	protected OutputStream output;

	// Permet d'ouvrir le port à la première utilisation de la série
	protected volatile boolean portOuvert = false;
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;

	/**
	 * Constructeur pour la série de test
	 * @param log
	 */
	public SerieCouchePhysique(Log log)
	{
		this.log = log;
	}

	/**
	 * Ouverture du port
	 */
	protected void openPort()
	{
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
	
	/**
	 * Recherche du port
	 * @return
	 */
	protected synchronized boolean searchPort()
	{
		log.debug("Recherche de la série sur "+portName+" à "+baudrate+" baud");
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		
		while(ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();

			if(port.getName().equals(portName))
			{
				log.debug("Port "+port.getName()+" trouvé !");
				if(!initialize(port, baudrate))
					break;

				portOuvert = true;
				return true;
			}
		}

		log.warning("Port "+portName+" introuvable.");
		portOuvert = false;
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
			serialPort = (SerialPort) portId.open("MoonRover", TIME_OUT);
			// set port parameters
			serialPort.setSerialPortParams(baudrate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
//			serialPort.setInputBufferSize(100);
//			serialPort.setOutputBufferSize(100);
//			serialPort.enableReceiveTimeout(100);
//			serialPort.enableReceiveThreshold(1);
			serialPort.notifyOnDataAvailable(true); // activation du listener

			// Configuration du Listener
			serialPort.addEventListener(this);

			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			isClosed = false;
			return true;
		}
		catch (TooManyListenersException | PortInUseException | UnsupportedCommOperationException | IOException e2)
		{
			log.critical(e2);
			return false;
		}
	}
	
	/**
	 * Doit être appelé quand on arrête de se servir de la série
	 */
	public void close()
	{
		if (!isClosed && portOuvert)
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
		try {
			if(input.available() > 0)
				notify();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retourne "true" ssi un octet est lisible en utilisant "read"
	 */
	public boolean available()
	{
		if(!portOuvert)
			openPort();

		try {
			return input.available() != 0;
		} catch (IOException e) {
			e.printStackTrace(); // Cette exception a lieu si la série est fermée
			return false;
		}
	}
	
	/**
	 * Lit un octet
	 * On sait qu'un octet doit s'y trouver ; soit parce que available() retourne "true", soit parce que le protocole l'impose.
	 * @return
	 * @throws IOException
	 * @throws MissingCharacterException
	 */
	public int read() throws MissingCharacterException
	{
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
					log.debug("Reçu : "+"0"+s+" ("+(char)(out & 0xFF)+")");
				else
					log.debug("Reçu : "+s.substring(s.length()-2, s.length())+" ("+(char)(out & 0xFF)+")");	
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
	 * Envoie une frame sur la série
	 * Cette méthode est synchronized car deux threads peuvent l'utiliser : ThreadSerialOutput et ThreadSerialOutputTimeout
	 * @param message
	 */
	public synchronized void communiquer(OutgoingFrame out)
	{
		if(!portOuvert)
			openPort();

		/**
		 * Un appel à une série fermée ne devrait jamais être effectué.
		 */
		if(isClosed)
		{
			log.debug("La série est fermée et ne peut envoyer :"+out);
			return;
		}
		
		try
		{
			if(Config.debugSerieTrame)
				log.debug(out);

			output.write(out.trame);
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
	{
		portName = config.getString(ConfigInfo.SERIAL_PORT);
		baudrate = config.getInt(ConfigInfo.BAUDRATE);
	}

}

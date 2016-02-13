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

import utils.Log;
import utils.Sleep;
import exceptions.MissingCharacterException;

/**
 * La connexion série vers la STM ou la XBEE
 * @author pf
 *
 */

public abstract class SerialConnexion implements SerialPortEventListener, SerialInterface
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
				log.debug("STM sur " + port.getName());
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
			serialPort.setInputBufferSize(100);
			serialPort.setOutputBufferSize(100);
			serialPort.enableReceiveTimeout(100);
			serialPort.enableReceiveThreshold(1);
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
	
	public abstract void communiquer(byte[] out);
	
	protected void attendSiPing()
	{
		// Si la série est occupée, on attend sagement
		if(busy)
			synchronized(this)
			{
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
	public void serialEvent(SerialPortEvent oEvent)
	{
		try {
			if(input.available() > 0)
				notify();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean available() throws IOException
	{
		// tant qu'on est occupé, on dit qu'on ne reçoit rien
		if(busy)
			return false;
		return input.available() != 0;
	}
	
	/**
	 * Lit un byte. On sait qu'il doit y en a avoir un.
	 * @return
	 * @throws IOException
	 * @throws MissingCharacterException
	 */
	public byte read() throws IOException, MissingCharacterException
	{
		attendSiPing();

		if(input.available() == 0)
			Sleep.sleep(1); // On attend un tout petit peu, au cas où

		if(input.available() == 0)
			throw new MissingCharacterException(); // visiblement on ne recevra rien de plus

		return (byte) input.read();
	}
	
	protected abstract boolean ping();
	protected abstract void estimeLatence();
	protected abstract void afficheMessage(byte[] out);

}

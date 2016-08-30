package serie;

import java.io.IOException;
import java.io.InputStream;

import utils.Config;
import utils.Log;
import container.Service;
import exceptions.serie.MissingCharacterException;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * Buffer très bas niveau qui récupère les octets sur la série
 * @author pf
 *
 */

public class BufferIncomingBytes implements Service, SerialPortEventListener
{
	private Log log;
	
	private InputStream input;

	private int bufferReading[] = new int[256];
	
	private volatile int indexBufferStart = 0;
	private volatile int indexBufferStop = 0;
	
	public BufferIncomingBytes(Log log)
	{
		this.log = log;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
	public void setInput(InputStream input)
	{
		this.input = input;
	}

	/**
	 * Gestion d'un évènement sur la série.
	 */
	public void serialEvent(SerialPortEvent oEvent)
	{
		if(oEvent.getEventType() != SerialPortEvent.DATA_AVAILABLE)
			log.warning(oEvent.getEventType());
		else
		{
			try {
				do
				{
					synchronized(this)
					{
						bufferReading[indexBufferStop++] = input.read();
						indexBufferStop &= 0xFF;
						notifyAll();
					}
				} while(input.available() > 0);

			} catch (IOException e) {
				log.critical(e);
			}
		}
	}

	/**
	 * Retourne "true" ssi un octet est lisible en utilisant "read"
	 */
	public final synchronized boolean available()
	{
		return indexBufferStart != indexBufferStop;
	}
	
	/**
	 * Lit un octet
	 * On sait qu'un octet doit s'y trouver ; soit parce que available() retourne "true", soit parce que le protocole l'impose.
	 * @return
	 * @throws IOException
	 * @throws MissingCharacterException
	 * @throws InterruptedException 
	 */
	public final synchronized int read() throws MissingCharacterException, InterruptedException
	{
		int essai = 0;
		while(indexBufferStart == indexBufferStop && essai < 10)
		{
			wait(0, 10000);
			essai++;
		}

		if(indexBufferStart == indexBufferStop)
			throw new MissingCharacterException();
		else
		{
			int out = bufferReading[indexBufferStart++];
			indexBufferStart &= 0xFF;

			if(Config.debugSerieTrame)
			{
				String s = Integer.toHexString(out).toUpperCase();
				if(s.length() == 1)
					log.debug("Reçu : "+"0"+s+" ("+(char)(out)+")");
				else
					log.debug("Reçu : "+s.substring(s.length()-2, s.length())+" ("+(char)(out)+")");	
			}

			return out;
		}
	}

	/**
	 * Fermeture du flux d'arrivée
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		input.close();
	}
}

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

package serie;

import java.io.IOException;
import java.io.InputStream;

import utils.Log;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.SerialClass;
import container.Service;
import exceptions.serie.MissingCharacterException;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * Buffer très bas niveau qui récupère les octets sur la série
 * @author pf
 *
 */

public class BufferIncomingBytes implements Service, SerialPortEventListener, Configurable, SerialClass
{
	private Log log;
	
	private InputStream input;

	private int bufferReading[] = new int[256];
	
	private volatile int indexBufferStart = 0;
	private volatile int indexBufferStop = 0;
	
	private boolean debugSerieTrame;
	
	public BufferIncomingBytes(Log log)
	{
		this.log = log;
	}
	
	@Override
	public void useConfig(Config config)
	{
		debugSerieTrame = config.getBoolean(ConfigInfo.DEBUG_SERIE_TRAME);
	}
	
	public void setInput(InputStream input)
	{
		this.input = input;
	}

	/**
	 * Gestion d'un évènement sur la série.
	 */
	@Override
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

		int out = bufferReading[indexBufferStart++];
		indexBufferStart &= 0xFF;

		if(debugSerieTrame)
		{
			String s = Integer.toHexString(out).toUpperCase();
			if(s.length() == 1)
			{
				if(out >= 32 && out < 127)
					log.debug("Reçu : "+"0"+s+" ("+(char)(out)+")");
				else
					log.debug("Reçu : "+"0"+s);
			}
			else
			{
				if(out >= 32 && out < 127)
					log.debug("Reçu : "+s.substring(s.length()-2, s.length())+" ("+(char)(out)+")");	
				else
					log.debug("Reçu : "+s.substring(s.length()-2, s.length()));	
			}
		}

		return out;
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

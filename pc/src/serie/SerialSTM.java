package serie;

import utils.Config;
import utils.Log;
import utils.Sleep;
import container.Service;
import enums.SerialProtocol;

/**
 * Série de la STM
 * Il n'y a pas besoin de "synchronized" car il y deux threads qui utilisent cette série : le thread de lecture et celui d'écriture.
 * Et comme la série peut gérer les deux en parallèle… (full duplex)
 * Le seul moment où il faut bloquer les threads c'est quand la série ping, et utilise les deux sens (lecture et écriture)
 * @author pf
 *
 */

public class SerialSTM extends SerialConnexion implements Service, SerialInterface
{
	private static byte[] question, reponse;
	private int premierID;
	// Il faut qu'ils soient définis avant l'appel au constructeur…
	static
	{
		question = new byte[6];
		question[0] = (byte) 0x55;
		question[1] = (byte) 0xAA;
		question[2] = 0;
		question[3] = 0;
		question[4] = SerialProtocol.OUT_PING_NEW_CONNECTION.code;
		question[5] = (byte) ~question[4]; // checksum
		
		reponse = new byte[3];
		reponse[0] = SerialProtocol.IN_PONG1.code;
		reponse[1] = SerialProtocol.IN_PONG2.code;
		reponse[2] = 0;
	}
	
	public SerialSTM(Log log, int baudrate) {
		super(log, baudrate);
		
		// le problème du ping, c'est que SerialConnexion ne connaît pas l'id du paquet à envoyer
		// du coup, on met l'id 0. Comme ça, la STM pensera que c'est un vieux paquet mais y répondra quand même,
		// et ça ne posera pas problème.

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
			output.write(question);

			if(Config.debugSerie)
			{
				log.debug("Question : ");
				afficheMessage(question);
			}

			// on laisse le temps au périphérique de réagir
			Sleep.sleep(100);
			
			byte[] lu = new byte[input.available()];
			int nbLu = input.read(lu);
			if(Config.debugSerie)
			{
				log.debug("Réponse : ");
				afficheMessage(lu);
			}
			
			// le +4 vient du fait qu'on ne vérifie pas l'id du paquet qui arrive ni l'entete
			if(nbLu != reponse.length + 4)	// vérification du nombre de byte lu
			{
//				log.debug("Mauvaise taille");
				return false;
			}

			if(lu[0] != (byte)0x55 || lu[1] != (byte)0xAA) // vérification de l'entete
			{
//				log.debug("Mauvais entête "+lu[0]+" "+(byte)0x55+" "+lu[1]+" "+(byte)0xAA);
				return false;
			}
			
			premierID = (((int)lu[2] & 0xFF) << 8) + ((int)lu[3] & 0xFF);
			
			// on ne vérifie pas le checksum qui dépend de l'id
			for(int i = 0; i < reponse.length-1; i++)
				if(reponse[i] != lu[i+4]) // on ne vérifie pas l'ID
				{
						log.debug("Erreur au caractère "+(i+4));
					return false;
				}
			
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
	
	public void communiquer(byte[] out)
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

		super.attendSiPing();
		
		try
		{
			if(Config.debugSerie)
				afficheMessage(out);

			output.write(0x55);
			output.write(0xAA);
			output.write(out);
			// calcul du checksum
			int c = 0;
			for(int i = 0; i < out.length; i++)
				c += out[i];
/*			if((new Random()).nextInt(5) == 0)
			{
				log.debug("Erreur d'envoi simulée");
				c = 1;
			}*/
			output.write((byte)~c);
		}
		catch (Exception e)
		{
			/**
			 * Si la STM ne répond vraiment pas, on recommence de manière infinie.
			 * De toute façon, on n'a pas d'autre choix...
			 */
			log.critical("Ne peut pas parler à la STM. Tentative de reconnexion.");
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
	public void useConfig(Config config)
	{}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public int getFirstID()
	{
		return premierID;
	}

}

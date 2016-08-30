package serie.trame;

import exceptions.serie.IncorrectChecksumException;

/**
 * Une trame qu'on a reçue
 * @author pf
 *
 */

public class IncomingFrame extends Frame
{
	public IncomingCode code;

	public int[] message;

	/**
	 * Constructeur d'une trame reçue
	 * @return
	 */
	public IncomingFrame(int code, int id, int checksum, int longueur, int[] message) throws IncorrectChecksumException
	{
		/**
		 * On cherche à quel type de trame correspond la valeur reçue
		 */
		if(code == IncomingCode.EXECUTION_BEGIN.code)
			this.code = IncomingCode.EXECUTION_BEGIN;
		else if(code == IncomingCode.EXECUTION_END.code)
			this.code = IncomingCode.EXECUTION_END;
		else if(code == IncomingCode.STATUS_UPDATE.code)
			this.code = IncomingCode.STATUS_UPDATE;
		else if(code == IncomingCode.VALUE_ANSWER.code)
			this.code = IncomingCode.VALUE_ANSWER;
		else
			throw new IllegalArgumentException("Type de trame inconnu : "+code);

		this.id = id;
		this.message = message;
		
		int c = code + id + longueur;
		for(int i = 0; i < message.length; i++)
			c += message[i];
		c = c & 0xFF;
		if(c != checksum)
			throw new IncorrectChecksumException("Checksum attendu : "+checksum+", checksum calculé : "+c);
	}

	@Override
	public String toString()
	{
		String m = "Incoming : "+code+" // ";
		for(int i = 0; i < message.length; i++)
		{
			String s = Integer.toHexString(message[i]).toUpperCase();
			if(s.length() == 1)
				m += "0"+s+" ";
			else
				m += s.substring(s.length()-2, s.length())+" ";
		}
		return m;
	}
	
}

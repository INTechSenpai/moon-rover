package serie.trame;

import exceptions.IncorrectChecksumException;

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
		for(IncomingCode c : IncomingCode.values())
		{
			if(code == c.codeInt)
				this.code = c;
		}
		if(this.code == null)
			throw new IllegalArgumentException("Type de trame inconnu : "+code);

		this.id = (byte) id;
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

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
	public IncomingFrame(int code, int id, int compteur, int checksum, int[] message) throws IncorrectChecksumException
	{
		for(IncomingCode c : IncomingCode.values())
		{
			if(code == c.codeInt)
				this.code = c;
		}
		if(this.code == null)
		{
			System.out.println("Type de trame inconnu : "+code);
			throw new IllegalArgumentException();
		}
		this.compteur = (byte) compteur;
		this.message = message;
		// TODO vérifier checksum
	}
	
}

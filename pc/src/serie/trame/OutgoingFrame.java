package serie.trame;

/**
 * Trame qu'on envoie
 * @author pf
 *
 */

public class OutgoingFrame extends Frame
{
	public byte[] message;

	/**
	 * Constructeur d'une trame à envoyer
	 * @param o
	 */
	public OutgoingFrame(Order o)
	{
		this.deathDate = System.currentTimeMillis() + timeout;
		compteur = compteurReference;
		compteurReference++;
		
		code = o.orderType == Order.Type.LONG ? Code.NEW_ORDER : Code.VALUE_REQUEST;
		message = new byte[o.message.length + 5];
		message[0] = code.code;
		message[1] = id;
		message[2] = compteur;
		
		int c = 0;
		for(int i = 0; i < message.length; i++)
			if(i != 3)
				c += message[i];
		message[3] = (byte) (c);
	}

}

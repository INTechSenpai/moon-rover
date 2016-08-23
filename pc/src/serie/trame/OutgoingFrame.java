package serie.trame;

import serie.Ticket;

/**
 * Trame qu'on envoie
 * @author pf
 *
 */

public class OutgoingFrame extends Frame
{
	public OutgoingCode code;
	public byte[] message;
	private long deathDate; // date d'envoi + timeout (toutes les trames provenant du haut niveau doivent être acquittées)
	public final Ticket ticket = new Ticket();
	
	/**
	 * Constructeur d'une trame à envoyer
	 * @param o
	 */
	public OutgoingFrame(Order o)
	{
		this.deathDate = System.currentTimeMillis() + timeout;
		compteur = compteurReference;
		compteurReference++;
		
		code = o.orderType == Order.Type.LONG ? OutgoingCode.NEW_ORDER : OutgoingCode.VALUE_REQUEST;
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

	protected static int timeout;
	
	public static void setTimeout(int timeout_p)
	{
		timeout = timeout_p;
	}

	/**
	 * Faut-il renvoyer cette trame ?
	 * @return
	 */
	public boolean needResend()
	{
		return deathDate < System.currentTimeMillis();
	}

	/**
	 * Récupère le temps restant avant sa mort
	 * @return
	 */
	public int timeBeforeDeath()
	{
		return (int) (deathDate - System.currentTimeMillis());
	}

}

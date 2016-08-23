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
	public byte[] message, trame;
	private long deathDate; // date d'envoi + timeout (toutes les trames provenant du haut niveau doivent être acquittées)
	public final Ticket ticket;
	
	/**
	 * Trame de END_ORDER
	 */
	public OutgoingFrame(byte compteur)
	{
		ticket = null;
		deathDate = 0;
		this.compteur = compteur;
		code = OutgoingCode.END_ORDER;
		message = new byte[0];
	}
	
	/**
	 * Constructeur d'une trame à envoyer (NEW_ORDER ou VALUE_REQUEST)
	 * @param o
	 */
	public OutgoingFrame(Order o)
	{
		ticket = o.ticket;
		deathDate = System.currentTimeMillis() + timeout;
		compteur = compteurReference;
		compteurReference++;
		
		code = o.orderType == Order.Type.LONG ? OutgoingCode.NEW_ORDER : OutgoingCode.VALUE_REQUEST;
		trame = new byte[o.message.length + 5];
		trame[0] = code.code;
		trame[1] = id;
		trame[2] = compteur;
		
		for(int i = 0; i < message.length; i++)
			trame[i+4] = message[i];
		
		int c = 0;
		for(int i = 0; i < trame.length; i++)
			if(i != 3)
				c += trame[i];
		trame[3] = (byte) (c);
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

	/**
	 * La trame sous forme de byte[], prête à être envoyée
	 * @return
	 */
	public byte[] getBytes()
	{
		return trame;
	}

}

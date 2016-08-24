package serie.trame;

/**
 * Trame qu'on envoie
 * @author pf
 *
 */

public class OutgoingFrame extends Frame
{
	public OutgoingCode code;
	public byte[] message, trame;
	
	/**
	 * Trame de END_ORDER
	 */
	public OutgoingFrame(byte compteur)
	{
		this.compteur = compteur;
		code = OutgoingCode.END_ORDER;
		message = new byte[4];
		message[0] = OutgoingCode.END_ORDER.code;
		message[1] = 4; // longueur de la trame
		message[2] = compteur;
		message[3] = (byte) (message[0] + message[1] + message[2]);
	}
	
	/**
	 * Constructeur d'une trame à envoyer (NEW_ORDER ou VALUE_REQUEST)
	 * @param o
	 */
	public OutgoingFrame(Order o)
	{
		compteur = compteurReference;
		compteurReference++;
		
		int longueur = o.message.length + 4;
		if(longueur > 255)
			throw new IllegalArgumentException("La trame est trop grande ! ("+longueur+" octets)");
		
		code = o.orderType == Order.Type.LONG ? OutgoingCode.NEW_ORDER : OutgoingCode.VALUE_REQUEST;
		trame = new byte[longueur];
		trame[0] = code.code;
		trame[1] = (byte) (longueur);
		trame[2] = compteur;
		
		for(int i = 0; i < message.length; i++)
			trame[i+3] = message[i];
		
		int c = 0;
		for(int i = 0; i < trame.length-1; i++)
			c += trame[i];
		trame[trame.length-1] = (byte) (c);
	}

	@Override
	public void afficheMessage()
	{
		String m = "";
		for(int i = 0; i < message.length; i++)
		{
			String s = Integer.toHexString(message[i]).toUpperCase();
			if(s.length() == 1)
				m += "0"+s+" ";
			else
				m += s.substring(s.length()-2, s.length())+" ";
		}
		System.out.println("Outgoing : "+m);
	}
	
}

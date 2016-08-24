package serie.trame;

/**
 * Trame qu'on envoie
 * @author pf
 *
 */

public class OutgoingFrame extends Frame
{
	public OutgoingCode code;
	public byte[] trame;
	public Order.Type type;
	/**
	 * Trame de END_ORDER
	 */
	public OutgoingFrame(byte compteur)
	{
		this.compteur = compteur;
		code = OutgoingCode.END_ORDER;
		trame = new byte[4];
		trame[0] = OutgoingCode.END_ORDER.code;
		trame[1] = 4; // longueur de la trame
		trame[2] = compteur;
		trame[3] = (byte) (trame[0] + trame[1] + trame[2]);
	}
	
	/**
	 * Constructeur d'une trame à envoyer (NEW_ORDER ou VALUE_REQUEST)
	 * @param o
	 */
	public OutgoingFrame(Order o, byte compteur)
	{
		this.compteur = compteur;
		int longueur = o.message.length + 4;
		if(longueur > 255)
			throw new IllegalArgumentException("La trame est trop grande ! ("+longueur+" octets)");
		type = o.orderType;
		code = o.orderType == Order.Type.LONG ? OutgoingCode.NEW_ORDER : OutgoingCode.VALUE_REQUEST;
		trame = new byte[longueur];
		trame[0] = code.code;
		trame[1] = (byte) (longueur);
		trame[2] = compteur;
		
		for(int i = 0; i < o.message.length; i++)
			trame[i+3] = o.message[i];
		
		int c = 0;
		for(int i = 0; i < trame.length-1; i++)
			c += trame[i];
		trame[trame.length-1] = (byte) (c);
	}

	@Override
	public void afficheMessage()
	{
		String m = "";
		for(int i = 0; i < trame.length; i++)
		{
			String s = Integer.toHexString(trame[i]).toUpperCase();
			if(s.length() == 1)
				m += "0"+s+" ";
			else
				m += s.substring(s.length()-2, s.length())+" ";
		}
		System.out.println("Outgoing : "+m);
	}
	
}


public class Log extends Service {
	
	private static Log INSTANCE = null;

	private String 	couleurDebug 	= "\u001B[32m",
					couleurWarning 	= "\u001B[33m",
					couleurCritical = "\u001B[31m";

	private Log()
	{
	}
	
	public void debug(String message)
	{
		ecrire(message, couleurDebug);
	}

	public void warning(String message)
	{
		ecrire(message, couleurWarning);
	}

	public void critical(String message)
	{
		ecrire(message, couleurCritical);
	}

	private void ecrire(String message, String couleur)
	{
		System.out.println(couleur+message+"\u001B[0m");
	}
	
	public static Log initialiser()
	{
		if (INSTANCE == null)
		 	INSTANCE = new Log();
		return INSTANCE;
	}
}

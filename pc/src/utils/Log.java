package utils;
import factories.FactoryProduct;

public class Log implements FactoryProduct
{
	
	private String 	couleurDebug 	= "\u001B[32m",
					couleurWarning 	= "\u001B[33m",
					couleurCritical = "\u001B[31m";

	public FactoryProduct Clone()
	{
		return new Log();
	}

	public String TypeName()
	{
		return "Log";
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
	
}

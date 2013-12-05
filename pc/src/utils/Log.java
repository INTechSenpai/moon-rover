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
	
	public void debug(String message, Object objet)
	{
		ecrire(objet.getClass().getName()+": "+message, couleurDebug);
	}

	public void warning(String message, Object objet)
	{
		ecrire(objet.getClass().getName()+": "+message, couleurWarning);
	}

	public void critical(String message, Object objet)
	{
		ecrire(objet.getClass().getName()+": "+message, couleurCritical);
	}

	private void ecrire(String message, String couleur)
	{
		System.out.println(couleur+message+"\u001B[0m");
	}
	
}

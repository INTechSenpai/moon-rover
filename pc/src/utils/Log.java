package utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import container.Service;

/**
 * Service de log, affiche à l'écran des informations avec différents niveaux de couleurs
 * @author pf
 *
 */

public class Log implements Service
{
	// Dépendances
	private Read_Ini config;

	private String 	couleurDebug 	= "\u001B[32m",
					couleurWarning 	= "\u001B[33m",
					couleurCritical = "\u001B[31m";

	java.util.GregorianCalendar calendar = new GregorianCalendar();
	
	public Log(Service config)
	{
		this.config = (Read_Ini) config;
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
		String heure = calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+":"+calendar.get(Calendar.MILLISECOND);
		System.out.println(couleur+heure+" "+message+"\u001B[0m");
	}
	
}

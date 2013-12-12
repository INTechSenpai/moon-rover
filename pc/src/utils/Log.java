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

	// Ne pas afficher les messages de bug permet d'économiser du temps CPU
	private boolean affiche_debug = true;
	
	public Log(Read_Ini config)
	{
		this.config = config;
		
		try {
		affiche_debug = Boolean.parseBoolean(this.config.get("affiche_debug"));
		}
		catch(Exception e)
		{
			warning(e, this);
		}
	}
	
	public void debug(Object message, Object objet)
	{
		debug(message.toString(), objet);
	}
	
	public void debug(String message, Object objet)
	{
		if(affiche_debug)
			ecrire(objet.getClass().getName()+": "+message, couleurDebug);
	}

	public void warning(Object message, Object objet)
	{
		warning(message.toString(), objet);
	}

	public void warning(String message, Object objet)
	{
		ecrire(objet.getClass().getName()+": "+message, couleurWarning);
	}

	public void critical(Object message, Object objet)
	{
		critical(message.toString(), objet);
	}
	
	public void critical(String message, Object objet)
	{
		ecrire(objet.getClass().getName()+": "+message, couleurCritical);
	}

	private void ecrire(String message, String couleur)
	{
		java.util.GregorianCalendar calendar = new GregorianCalendar();
		String heure = calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+","+calendar.get(Calendar.MILLISECOND);
		System.out.println(couleur+heure+" "+message+"\u001B[0m");
	}
	
}

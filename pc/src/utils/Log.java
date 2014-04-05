package utils;

import java.io.FileWriter;
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

	FileWriter writer = null;

	private String 	couleurDebug 	= "\u001B[32m",
					couleurWarning 	= "\u001B[33m",
					couleurCritical = "\u001B[31m";

	// Ne pas afficher les messages de bug permet d'économiser du temps CPU
	private boolean affiche_debug = true;
	
	// Sauvegarder les logs dans un fichier
	private boolean sauvegarde_fichier = false;
	
	public Log(Read_Ini config)
	{
		this.config = config;
		
		try {
			affiche_debug = Boolean.parseBoolean(this.config.get("affiche_debug"));
		}
		catch(Exception e)
		{
			critical(e, this);
		}
		try {
			sauvegarde_fichier = Boolean.parseBoolean(this.config.get("sauvegarde_fichier"));
		}
		catch(Exception e)
		{
			critical(e, this);
		}

		if(sauvegarde_fichier)
			try {
				java.util.GregorianCalendar calendar = new GregorianCalendar();
				String heure = calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
				writer = new FileWriter("logs/LOG-"+heure+".txt", true); 
			}
			catch(Exception e)
			{
				critical(e, this);
			}
	
	}
	
	public void special(Object message)
	{
		special(message.toString());
	}
	
	public void special(String message)
	{
		if(affiche_debug)
			ecrire("Lanceur: "+message, couleurDebug);
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
		if(couleur != couleurDebug || affiche_debug)
			System.out.println(couleur+heure+" "+message+"\u001B[0m");
		if(sauvegarde_fichier)
			ecrireFichier(couleur+heure+" "+message+"\u001B[0m");
	}
	
	private void ecrireFichier(String message)
	{
		message += "\n";
		try{
		     writer.write(message,0,message.length());
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	public void destructeur()
	{
		if(sauvegarde_fichier)
			try {
				debug("Sauvegarde du fichier de logs", this);
				if(writer != null)
					writer.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
	}
	
	public void maj_config()
	{
		// TODO
	}

}

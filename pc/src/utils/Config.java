package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import container.Service;
import enums.RobotColor;


/**
 * Gère le fichier de configuration externe.
 * @author pf, marsu
 *
 */
public class Config implements Service
{
	// Permet de savoir si le match a démarré et quand
	public static final String path = "./config/";

	private String name_config_file = "config.ini";
	private Properties properties = new Properties();
	private Log log;
	
	private boolean needUpdate = false;
	
	public Config(Log log)
	{
		this.log = log;

		//	log.debug("Loading config from current directory : " +  System.getProperty("user.dir"), this)
		try
		{
			properties.load(new FileInputStream(path+name_config_file));
		}
		catch  (IOException e)
		{
			log.critical("Erreur lors de l'ouverture de config.ini. Utilisation des valeurs par défaut.");
		}
		
		completeConfig();
		if(getBoolean(ConfigInfo.AFFICHE_DEBUG))
			afficheTout();
	}
	
	/**
	 * Récupère un entier de la config
	 * @param nom
	 * @return
	 * @throws NumberFormatException 
	 */
	public int getInt(ConfigInfo nom) throws NumberFormatException
	{
		return Integer.parseInt(getString(nom));
	}
	
	/**
	 * Récupère un booléen de la config
	 * @param nom
	 * @return
	 */
	public boolean getBoolean(ConfigInfo nom)
	{
		return Boolean.parseBoolean(getString(nom));
	}
	
	/**
	 * Récupère un double de la config
	 * @param nom
	 * @return
	 * @throws NumberFormatException 
	 */	
	public double getDouble(ConfigInfo nom) throws NumberFormatException
	{
		return Double.parseDouble(getString(nom));
	}
	
	/**
	 * Méthode de récupération des paramètres de configuration
	 * @param nom
	 * @return
	 */
	public String getString(ConfigInfo nom)
	{
		return properties.getProperty(nom.toString());
	}

	/**
	 * Méthode utilisée seulement par les tests
	 * @param nom
	 * @return
	 */
	private void set(ConfigInfo nom, String value)
	{
		needUpdate |= value.compareTo(properties.getProperty(nom.toString())) != 0;
		log.debug(nom+" = "+value+" (ancienne valeur: "+properties.getProperty(nom.toString())+")");
		properties.setProperty(nom.toString(), value);
	}
	
	/**
	 * Set en version user-friendly
	 * @param nom
	 * @param value
	 */
	public void set(ConfigInfo nom, Object value)
	{
		set(nom, value.toString());
	}

	/**
	 * Affiche toute la config.
	 * Appelé au début du match.
	 */
	private void afficheTout()
	{
		log.debug("Configuration initiale");
		for(ConfigInfo info: ConfigInfo.values())
			log.debug(info+": "+getString(info));
	}
	
	/**
	 * Complète avec les valeurs par défaut le fichier de configuration
	 */
	private void completeConfig()
	{
		for(ConfigInfo info: ConfigInfo.values())
		{
			if(!properties.containsKey(info.toString()))
				properties.setProperty(info.toString(), info.getDefaultValue());
			log.warning(info+" surchargé par config.ini");
		}
		for(String cle: properties.stringPropertyNames())
		{
			boolean found = false;
			for(ConfigInfo info: ConfigInfo.values())
				if(info.toString().compareTo(cle) == 0)
				{
					found = true;
					break;
				}
			if(!found)
				log.warning("Config "+cle+" inutilisée. Veuillez le retirer de config.ini");
		}
	}
		
	/**
	 * Récupère la symétrie
	 * @return
	 */
	public boolean getSymmetry()
	{
		return RobotColor.parse(getString(ConfigInfo.COULEUR)).isSymmetry();
	}

	/**
	 * Met à jour les config de tous les services
	 */
	public void updateConfigServices()
	{
		synchronized(this)
		{
			if(needUpdate)
				notifyAll();
			needUpdate = false;
		}
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}

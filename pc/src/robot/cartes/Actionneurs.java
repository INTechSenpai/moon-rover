package robot.cartes;

import robot.serial.Serial;
import utils.Log;
import utils.Read_Ini;
import container.Service;


/**
 * Classe des actionneurs. Utilisée par robot pour bouger les actionneurs.
 * @author pf
 */
public class Actionneurs implements Service {

	// Dépendances
	private Log log;
	private Serial serie;

	public Actionneurs(Read_Ini config, Log log, Serial serie)
	{
		this.log = log;
		this.serie = serie;
	}
	
	public void bac_bas()
	{
		log.debug("Bac baissé", this);
		serie.communiquer("bb", 0);
	}

	public void bac_haut()
	{
		log.debug("Bac levé", this);
		serie.communiquer("bh", 0);
	}

	public void rateau_ranger_gauche()
	{
		log.debug("Rateau gauche rangé", this);
		serie.communiquer("rrg", 0);
	}
	
	public void rateau_ranger_droit()
	{
		log.debug("Rateau droit rangé", this);
		serie.communiquer("rrd", 0);
	}
	
	public void rateau_bas_droit()
	{
		log.debug("Rateau droit baissé", this);
		serie.communiquer("rbd", 0);
	}
	
	public void rateau_bas_gauche()
	{
		log.debug("Rateau gauche baissé", this);
		serie.communiquer("rbg", 0);
	}
	
	public void rateau_haut_droit()
	{
		log.debug("Rateau droit monté", this);
		serie.communiquer("rhd", 0);
	}
	
	public void rateau_haut_gauche()
	{
		log.debug("Rateau gauche monté", this);
		serie.communiquer("rhg", 0);
	}

	public void rateau_super_bas_gauche()
	{
		log.debug("Rateau gauche vraiment baissé", this);
		serie.communiquer("rbbg", 0);
	}

	public void rateau_super_bas_droit()
	{
		log.debug("Rateau droit vraiment baissé", this);
		serie.communiquer("rbbd", 0);
	}

	
}

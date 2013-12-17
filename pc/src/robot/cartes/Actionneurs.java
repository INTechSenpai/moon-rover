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
	
	public void baisser_bac()
	{
		log.debug("Bac baissé", this);
		serie.communiquer("bb", 0);
	}

	public void lever_bac()
	{
		log.debug("Bac levé", this);
		serie.communiquer("bh", 0);
	}

	public void ranger_rateau_gauche()
	{
		log.debug("Rateau gauche rangé", this);
		serie.communiquer("rrg", 0);
	}
	
	public void ranger_rateau_droit()
	{
		log.debug("Rateau droit rangé", this);
		serie.communiquer("rrd", 0);
	}
	
	public void rateau_droit_bas()
	{
		log.debug("Rateau droit baissé", this);
		serie.communiquer("rbd", 0);
	}
	
	public void rateau_gauche_bas()
	{
		log.debug("Rateau gauche baissé", this);
		serie.communiquer("rbg", 0);
	}
	
	public void rateau_droit_haut()
	{
		log.debug("Rateau droit monté", this);
		serie.communiquer("rhd", 0);
	}
	
	public void rateau_gauche_haut()
	{
		log.debug("Rateau gauche monté", this);
		serie.communiquer("rhg", 0);
	}

	public void rateau_gauche_super_bas()
	{
		log.debug("Rateau gauche vraiment baissé", this);
		serie.communiquer("rbbg", 0);
	}

	public void rateau_droit_super_bas()
	{
		log.debug("Rateau gauche vraiment baissé", this);
		serie.communiquer("rbbd", 0);
	}

	
}

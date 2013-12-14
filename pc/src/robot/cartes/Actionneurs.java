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
		log.debug("Bac levé", this);
		serie.communiquer("bb", 0);
	}

	public void lever_bac()
	{
		log.debug("Bac baissé", this);
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
		serie.communiquer("rbd", 0);
	}
	
	public void rateau_gauche_bas()
	{
		serie.communiquer("rbg", 0);
	}
	
	public void rateau_droit_haut()
	{
		serie.communiquer("rhd", 0);
	}
	
	public void rateau_gauche_haut()
	{
		serie.communiquer("rhg", 0);
	}

	
}

package robot.cartes;

import robot.serial.Serial;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exception.SerialException;


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
	
	public void bac_bas() throws SerialException
	{
		log.debug("Bac baissé", this);
		serie.communiquer("bb", 0);
	}

	public void bac_haut() throws SerialException
	{
		log.debug("Bac levé", this);
		serie.communiquer("bh", 0);
	}

	public void rateau_ranger_gauche() throws SerialException
	{
		log.debug("Rateau gauche rangé", this);
		serie.communiquer("rrg", 0);
	}
	
	public void rateau_ranger_droit() throws SerialException
	{
		log.debug("Rateau droit rangé", this);
		serie.communiquer("rrd", 0);
	}
	
	public void rateau_bas_droit() throws SerialException
	{
		log.debug("Rateau droit baissé", this);
		serie.communiquer("rbd", 0);
	}
	
	public void rateau_bas_gauche() throws SerialException
	{
		log.debug("Rateau gauche baissé", this);
		serie.communiquer("rbg", 0);
	}
	
	public void rateau_haut_droit() throws SerialException
	{
		log.debug("Rateau droit monté", this);
		serie.communiquer("rhd", 0);
	}
	
	public void rateau_haut_gauche() throws SerialException
	{
		log.debug("Rateau gauche monté", this);
		serie.communiquer("rhg", 0);
	}

	public void rateau_super_bas_gauche() throws SerialException
	{
		log.debug("Rateau gauche vraiment baissé", this);
		serie.communiquer("rbbg", 0);
	}

	public void rateau_super_bas_droit() throws SerialException
	{
		log.debug("Rateau droit vraiment baissé", this);
		serie.communiquer("rbbd", 0);
	}

	
}

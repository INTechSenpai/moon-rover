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
	
	public void ouvrir_pince_gauche() throws SerialException
	{
		log.debug("Pince gauche ouverte", this);
		serie.communiquer("og", 0);
	}

	public void ouvrir_pince_droite() throws SerialException
	{
		log.debug("Pince droite ouverte", this);
		serie.communiquer("od", 0);
	}

	public void fermer_pince_gauche() throws SerialException
	{
		log.debug("Pince gauche fermée", this);
		serie.communiquer("fg", 0);
	}

	public void fermer_pince_droite() throws SerialException
	{
		log.debug("Pince droite fermée", this);
		serie.communiquer("fd", 0);
	}
	public void ouvrir_bas_pince_gauche() throws SerialException
	{
		log.debug("Pince gauche ouvert en bas", this);
		serie.communiquer("obg", 0);
	}
	public void ouvrir_bas_pince_droite() throws SerialException
	{
		log.debug("Pince droite ouvert en bas", this);
		serie.communiquer("obd", 0);
	}
	public void presque_fermer_pince_gauche() throws SerialException
	{
		log.debug("Pince gauche presque fermée", this);
		serie.communiquer("pfg", 0);
	}
	public void presque_fermer_pince_droite() throws SerialException
	{
		log.debug("Pince droite presque fermée", this);
		serie.communiquer("pfd", 0);
	}

	public void milieu_pince_gauche() throws SerialException
	{
		log.debug("Pince gauche milieu", this);
		serie.communiquer("mg", 0);
	}

	public void milieu_pince_droite() throws SerialException
	{
		log.debug("Pince droite milieu", this);
		serie.communiquer("md", 0);
	}

	public void lever_pince_gauche() throws SerialException
	{
		log.debug("Pince gauche levée", this);
		serie.communiquer("hg", 0);
	}

	public void lever_pince_droite() throws SerialException
	{
		log.debug("Pince droite levée", this);
		serie.communiquer("hd", 0);
	}
	
	public void baisser_pince_gauche() throws SerialException
	{
		log.debug("Pince gauche baissée", this);
		serie.communiquer("bg", 0);
	}

	public void baisser_pince_droite() throws SerialException
	{
		log.debug("Pince droite baissée", this);
		serie.communiquer("bd", 0);
	}
	public void tourner_pince_gauche() throws SerialException
	{
		log.debug("Pince gauche rotation 180°", this);
		serie.communiquer("tg",0);
	}
	public void tourner_pince_droite() throws SerialException
	{
		log.debug("Pince droite rotation 180°", this);
		serie.communiquer("td",0);
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

	public void tirerBalle() throws SerialException
	{
		// TODO (protocole)
		log.debug("Balle tirée", this);
		serie.communiquer("b", 0);
	}
	public void lancerFilet() throws SerialException
	{
		log.debug("Filet lancé", this);
		serie.communiquer("lf", 0);
		//lf -> lancer filet
	}

}

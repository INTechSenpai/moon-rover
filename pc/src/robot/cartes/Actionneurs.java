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
	private Read_Ini config;
	private Log log;
	private Serial serie;

	public Actionneurs(Read_Ini config, Log log, Serial serie)
	{
		this.config = config;
		this.log = log;
		this.serie = serie;
	}
	// un exemple de méthode
	public void bouger_bras(int angle)
	{
		// ABWABWA
	}
	
	public void baisser_bac()
	{
		serie.communiquer("bb", 0);
	}

	public void lever_bac()
	{
		serie.communiquer("bh", 0);
	}

	public void ranger_rateau_gauche()
	{
		serie.communiquer("rrg", 0);
	}
	
	public void ranger_rateau_droit()
	{
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

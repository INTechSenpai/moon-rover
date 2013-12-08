package robot;
import container.Service;
import utils.Log;


import gnu.io.CommPortIdentifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Instancie toutes les s�ries, si on lui demande gentillement!
 * @author pierre
 *
 */
public class SerialManager 
{
	// Dépendances
	private Log log;
	
	//Series a instancier
	public Serial serieAsservissement = new Serial();
	public Serial serieCapteursActionneurs = new Serial();
	public Serial serieLaser = new Serial();
	
	//Pour chaque carte, on connait a l'avance son nom, son ping et son baudrate
	private SpecificationCard asservissement = new SpecificationCard("deplacements", 0, 9600);
	private SpecificationCard capteurs_actionneurs = new SpecificationCard("capteurs_actionneurs", 3, 9600);
	private SpecificationCard laser = new SpecificationCard("laser", 4, 38400);
	
	//On stock les cartes dans une liste
	private Hashtable<String, SpecificationCard> cards = new Hashtable<String, SpecificationCard>();
	
	//Liste pour stocker les series qui sont connectees au pc 
	private ArrayList<String> connectedSerial = new ArrayList<String>();
	
	//Liste pour stocker les baudrates des differentes serie
	
	private ArrayList<Integer> baudrate = new ArrayList<Integer>();

	/**
	 * Recuperation de toutes les cartes dans cards et des baudrates dans baudrate
	 */
	public SerialManager(Service log)
	{
		this.log = (Log)log;
		
		cards.put("asservissement", asservissement);
		cards.put("capteurs_actionneurs", capteurs_actionneurs);
		cards.put("laser", laser);

		Enumeration<SpecificationCard> e = cards.elements();
		while (e.hasMoreElements())
		{
			int baud = e.nextElement().baudrate;
			if (!this.baudrate.contains(baud))
				this.baudrate.add(baud);
		}
	}

	/**
	 * Regarde toutes les series qui sont branchees dans /dev/ttyUSB*
	 */
	public  void checkSerial()
	{
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements())
		{
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
			this.connectedSerial.add(port.getName());
		}
	}
	/**
	 * Création des series (il faut au prealable faire un checkSerial())
	 */
	public void createSerial()
	{
		int id = -1;
		//Liste des series deja attribues
		ArrayList<Integer> deja_attribues = new ArrayList<Integer>();
		String pings[] = new String[5];
		for (int baudrate : this.baudrate)
		{
			System.out.println("liste des pings pour le baudrate " + baudrate);

			for(int k = 0; k < this.connectedSerial.size(); k++)
			{
				if (!deja_attribues.contains(k))
				{
					//Creation d'une serie de test
					Serial serialTest = new Serial();
					
					serialTest.initialize(this.connectedSerial.get(k), baudrate);

					id = Integer.parseInt(serialTest.ping());
					if(!isKnownPing(id))
						continue;

					//On stock le port de la serie (connectedSerial) dans le tabeau à la case [id]
					pings[id] = this.connectedSerial.get(k);

					//Après les tests de pings sur la serie, on ferme la communication
					serialTest.close();

					deja_attribues.add(k);

					System.out.println(id + " sur: " + connectedSerial.get(k));

				}
			}
		}
		//Association de chaque serie a son port
		Enumeration<SpecificationCard> e = cards.elements();
		while (e.hasMoreElements())
		{
			SpecificationCard serial = e.nextElement();
			if(serial.id == 0 && pings[serial.id] != null)
			{
				this.serieAsservissement.initialize(pings[serial.id], serial.baudrate);
			}
			else if(serial.id == 3 && pings[serial.id] != null)
			{
				this.serieCapteursActionneurs.initialize(pings[serial.id], serial.baudrate);
			}
			else if(serial.id == 4 && pings[serial.id] != null)
			{
				this.serieLaser.initialize(pings[serial.id], serial.baudrate);
			}
		}
	}
	/**
	 * Permet de savoir si une carte a d�j� ete ping�e, utilis� que par SerialManager
	 * @param id
	 * @return
	 */
	private boolean isKnownPing(int id)
	{
		Enumeration<SpecificationCard> e = cards.elements();
		while(e.hasMoreElements())
		{
			if(id == e.nextElement().id)
				return true;
		}
		return false;
	}
}

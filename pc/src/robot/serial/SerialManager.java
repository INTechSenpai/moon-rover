package robot.serial;
import container.Service;
import utils.Log;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import exception.SerialManagerException;

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
	public Serial serieAsservissement;
	public Serial serieCapteursActionneurs;
	public Serial serieLaser;

	public boolean series_pretes = false;
	
	//On stock les series dans une liste
	private Hashtable<String, Serial> series = new Hashtable<String, Serial>();
	
	//Pour chaque carte, on connait a l'avance son nom, son ping et son baudrate
	private SpecificationCard carteAsservissement = new SpecificationCard("serieAsservissement", 0, 9600);
	private SpecificationCard carteCapteursActionneurs = new SpecificationCard("serieCapteursActionneurs", 3, 9600);
	private SpecificationCard carteLaser = new SpecificationCard("serieLaser", 4, 38400);

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
		this.log = (Log) log;

		cards.put(this.carteAsservissement.name, this.carteAsservissement);
		cards.put(this.carteCapteursActionneurs.name, this.carteCapteursActionneurs);
		cards.put(this.carteLaser.name, this.carteLaser);

		Enumeration<SpecificationCard> e = cards.elements();
		while (e.hasMoreElements())
		{
			int baud = e.nextElement().baudrate;
			if (!this.baudrate.contains(baud))
				this.baudrate.add(baud);
		}

		this.serieAsservissement = new Serial(log, this.carteAsservissement.name);
		this.serieCapteursActionneurs = new Serial(log, this.carteCapteursActionneurs.name);
		this.serieLaser = new Serial(log, this.carteLaser.name);
		
		this.series.put(this.carteAsservissement.name, this.serieAsservissement);
		this.series.put(this.carteCapteursActionneurs.name, this.serieCapteursActionneurs);
		this.series.put(this.carteLaser.name, this.serieLaser);

		checkSerial();
		createSerial();
		
		series_pretes = true;
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
					Serial serialTest = new Serial(log, "carte de test");

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
	 * Permet de savoir si une carte a déjà été pingée, utilisé que par SerialManager
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
	
	/**
	 * Permet d'obtenir une série
	 * @param name
	 * 				Nom de la série
	 * @return
	 * 				L'instance de la série
	 */
	public Serial getSerial(String name)	throws SerialManagerException
	{
		if (this.series.containsKey(name))
		{
			return this.series.get(name);
		}
		else
		{
			log.critical("Aucune série du nom : " + name + " n'existe", this);
			throw new SerialManagerException("serie non trouvée");
		}
	}
}

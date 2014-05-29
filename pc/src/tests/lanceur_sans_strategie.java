package tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import robot.RobotVrai;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import robot.hautniveau.DeplacementsHautNiveau;
import scripts.Script;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.GameState;
import threads.ThreadManager;
import threads.ThreadTimer;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;
import container.Container;

public class lanceur_sans_strategie {

	static Container container;
	static Read_Ini config;
	static GameState<RobotVrai> real_state;
	static ScriptManager scriptmanager;
	static DeplacementsHautNiveau deplacements;
	static Deplacements dep;
	static Capteurs capteurs;
	static ThreadManager threadManager;
	static Log log;

	public static void main(String[] args) throws Exception
	{
		container = new Container();
		config = (Read_Ini) container.getService("Read_Ini");
		log = (Log) container.getService("Log");	// les logs sont fais sous l'identité de container, c'est ptet un peu crade...

		log.warning("LANCEUR SANS STRATEGIE : initialisation",container);

		//Début des paramétrages
		configCouleur();


		// initialise les singletons
		//real_state = (GameState<RobotVrai>) container.getService("RealGameState");
		scriptmanager = (ScriptManager) container.getService("ScriptManager");
		deplacements = (DeplacementsHautNiveau)container.getService("DeplacementsHautNiveau");
		dep = (Deplacements)container.getService("Deplacements");
		capteurs = (Capteurs) container.getService("Capteur");
		threadManager = (ThreadManager) container.getService("ThreadManager");

		real_state.robot.initialiser_actionneurs_deplacements();

		// Threads
		try 
		{
			log.debug("Création du Thread Capteur",container);
			container.getService("threadCapteurs");
			log.debug("Création du Thread Timer",container);
			container.getService("threadTimer");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		threadManager.demarreThreads();
		
		recalerRobot();
	

		// attends que le jumper soit retiré
		attendreDebutMatch();

		System.out.println("Le robot commence le match");
		
        real_state.robot.avancer(300);
        real_state.robot.tourner(Math.PI);
        real_state.robot.avancer(200);
        
        Script tree = (Script)scriptmanager.getScript("ScriptTree");
		Script deposer_fruits = (Script)scriptmanager.getScript("ScriptDeposerFruits");
		Script lances = (Script)scriptmanager.getScript("ScriptLances");
		
		
        // Boucle principale du match
        while(true)
        {
        	// fait tout les arbres du plus proche au plus loin
        	for(int version_arbre = 0; version_arbre < 4; version_arbre++)
        	{
	        	try
	        	{
			        tree.agit(version_arbre, real_state, false);
	        	}
				catch(Exception e)
				{
				}
	        	
	        	// va immédiatement déposer les fruits
	        	for(int version_depose = 0; version_depose < 2; version_depose++)
	        	{
		        	try
		        	{
		        		deposer_fruits.agit(1, real_state, false);
					}
		        	catch(Exception e)
					{
					}
	        	}
        	}
/*        	try
        	{
				lances.agit(0, real_state, true);
        	}
        	catch(Exception e)
			{}
        	try
        	{
				lances.agit(1, real_state, true);
        	}
        	catch(Exception e)
			{
			}*/
        }
		
		
		
	}

	/**
	 * Demande si la couleur est rouge au jaune
	 * @throws Exception
	 */
	static void configCouleur()  throws Exception
	{

		String couleur = "";
		while(!couleur.contains("rouge") && !couleur.contains("jaune"))
		{
			System.out.println("Rentrez \"jaune\" ou \"rouge\" : ");
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); 

			couleur = keyboard.readLine(); 
			if(couleur.contains("rouge"))
				config.set("couleur","rouge");
			else if(couleur.contains("jaune"))
				config.set("couleur", "jaune");

		}

	}

	/**
	 * Recale le robot
	 * @throws Exception
	 */
	static void recalerRobot()  throws Exception
	{

		System.out.println("Pret au recalage, appuyez sur entrée pour continuer");
		config.set("capteurs_on", false);	// pas de capteurs durant le recalage
		capteurs.maj_config();

		// attends la pression sur entrée
		new BufferedReader(new InputStreamReader(System.in)).readLine();

		//recale
		real_state.robot.recaler();

	}
	
	/**
	 * Attends que le match soit lancé
	 * @throws Exception
	 */
	static void attendreDebutMatch()  throws Exception
	{

		System.out.println("Robot pret pour le match, attente du retrait du jumper");
		
		// hack si le jumper est inopérant
		//ThreadTimer.match_demarre = true;

		while(!capteurs.demarrage_match())
				Sleep.sleep(100);
	}


	/**
	 * initialise le départ non Rapide
	 * @throws Exception
	 */
	static void initialiserDepartStandard()  throws Exception
	{
		real_state.robot.avancer(50);
		real_state.robot.tourner(-1.8);
		real_state.robot.avancer(100);
	}

	

	/**
	 * effectue le jépart non Rapide
	 * @throws Exception
	 */
	static void faireDepartStandard()  throws Exception
	{

		real_state.robot.avancer(150);
	}
	
	
}

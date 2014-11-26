//import hook.sortes.HookGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//import org.junit.runner.JUnitCore;











import robot.Locomotion;
import robot.RobotReal;
import robot.cardsWrappers.LocomotionCardWrapper;
import robot.cardsWrappers.SensorsCardWrapper;
import scripts.ScriptManager;
import strategie.GameState;
import threads.ThreadTimer;
//import tests.JUnit_StrategieThreadTest;
//import sun.rmi.runtime.Log;
//import threads.ThreadTimer;
import utils.Config;
import container.Container;
import exceptions.ContainerException;
import exceptions.ThreadException;
import exceptions.Locomotion.UnableToMoveException;
import exceptions.serial.SerialManagerException;


/**
 * Code qui démarre le robot en début de match
 * @author marsu
 *
 */

		
public class Main
{
	static Container container;
	static Config config;
	static GameState<RobotReal> real_state;
	static ScriptManager scriptmanager;
	static Locomotion deplacements;
	static LocomotionCardWrapper dep;
	static SensorsCardWrapper capteurs;
	static boolean doitFaireDepartRapide;
	
	
// dans la config de debut de match, toujours demandé une entrée clavier assez longue (ex "oui" au lieu de "o", pour éviter les fautes de frappes. Une erreur a ce stade coûte cher.
	
	/**
	 * Point d'entrée du programme. C'est ici que le code commence par être exécuté 
	 * @param args chaine de caractère des arguments de la ligne de commande
	 * @throws Exception TODO : quels sont les exeptions lancés ?
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		

		System.out.println("=== Robot INTech 2015 : initialisation ===");
		System.out.println("LANCEUR DE TEST: version de refactoring et de documentation");
		
        // si on veut exécuter un test unitaire sur la rapbe, recopier test.nomDeLaClasseDeTest
		//JUnitCore.main(		"tests.JUnit_DeplacementsTest");  
		
		
		// Système d'injection de dépendances
		try {

			container = new Container();
			container.getService("Log");
			config = (Config) container.getService("Config");
			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
			
			//Début des paramétrages
			//configCouleur();
		
			
			// initialise les singletons
		    dep = (LocomotionCardWrapper)container.getService("Deplacements");
			real_state = (GameState<RobotReal>) container.getService("RealGameState");
		    scriptmanager = (ScriptManager) container.getService("ScriptManager");
		    deplacements = (Locomotion)container.getService("DeplacementsHautNiveau");
		    capteurs = (SensorsCardWrapper) container.getService("Capteur");
		    

		
		} catch (ContainerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ThreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SerialManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Threads
		//
	//	container.demarreTousThreads();

		
		// mettre le checkup du robot.
		// ici juste des tests de déplcement
		/*System.out.println("deplacement bas niveau");
		dep.avancer(300);
		Thread.sleep(1000);
		dep.avancer(-300);
		Thread.sleep(1000);
		dep.tourner(1.0);
		Thread.sleep(1000);
		dep.tourner(1.57);
		Thread.sleep(1000);
		System.out.println("fini !");*/
		System.out.println("deplacement haut niveau");
		try 
		{
			deplacements.moveForward(100, null, true);
		} 
		catch (UnableToMoveException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("fini !");


		
		
		
		// attends que le jumper soit retiré
		try {
			attendreDebutMatch();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Le robot commence le match");
		 
		
		
		//Le match s'arrête
		container.destructor();
	}
	
	
	
	 * Demande si la couleur est rouge au jaune
	 * @throws Exception
	 */
	static void configCouleur()
	{

		String couleur = "";
		while(!couleur.contains("rouge") && !couleur.contains("jaune"))
		{
			System.out.println("Rentrez \"jaune\" ou \"rouge\" : ");
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); 
			 
			try {
				couleur = keyboard.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
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
	static void recalerRobot()
	{

		// System.out.println("Pret au recalage, appuyez sur entrée pour continuer");
		System.out.println("TODO : code de recalage");// TODO

		
	}	
	

	/**
	 * Attends que le match soit lancé
	 * @throws Exception
	 */
	static void attendreDebutMatch()
	{

		System.out.println("Robot pret pour le match, attente du retrait du jumper");
		
		// hack si le jumper est inopérant
		ThreadTimer.match_demarre = true;

		// while(!capteurs.demarrage_match())
		//	 	Sleep.sleep(100);
	}
	

	
}
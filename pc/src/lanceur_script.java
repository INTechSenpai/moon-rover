import java.io.BufferedReader;
import java.io.InputStreamReader;

import robot.RobotVrai;
import robot.cartes.Capteurs;
import robot.cartes.Deplacements;
import robot.hautniveau.DeplacementsHautNiveau;
import scripts.ScriptManager;
import smartMath.Vec2;
import strategie.GameState;
import utils.Read_Ini;
import utils.Sleep;
import container.Container;


public class lanceur_script
{
	static Container container;
	static Read_Ini config;
	static GameState<RobotVrai> real_state;
	static ScriptManager scriptmanager;
	static DeplacementsHautNiveau deplacements;
	static Deplacements dep;
	static Capteurs capteurs;

	public static void main(String[] args) throws Exception
	{
		container = new Container();
		config = (Read_Ini) container.getService("Read_Ini");


		//Début des paramétrages
		configCouleur();


		// initialise les singletons
		real_state = (GameState<RobotVrai>) container.getService("RealGameState");
		scriptmanager = (ScriptManager) container.getService("ScriptManager");
		deplacements = (DeplacementsHautNiveau)container.getService("DeplacementsHautNiveau");
		dep = (Deplacements)container.getService("Deplacements");
		capteurs = (Capteurs) container.getService("Capteur");

		real_state.robot.initialiser_actionneurs_deplacements();

		// Threads
		container.demarreTousThreads();

		//recalerRobot();

		initialiserDepartStandard();

		// attends que le jumper soit retiré
		attendreDebutMatch();

		faireDepartStandard();


		System.out.println("Le robot commence le match");
		
		real_state.robot.va_au_point_pathfinding(real_state.pathfinding, new Vec2(0,1643), null, false);
		
		
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
		real_state.robot.avancer(150);
	}

	

	/**
	 * effectue le départ non Rapide
	 * @throws Exception
	 */
	static void faireDepartStandard()  throws Exception
	{

		real_state.robot.avancer(150);
	}
	
	
}

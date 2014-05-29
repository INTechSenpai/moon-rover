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
import threads.ThreadTimer;
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

	public static void main(String[] args) throws Exception
	{
		container = new Container();
		config = (Read_Ini) container.getService("Read_Ini");


		//Début des paramétrages
		configCouleur();


		// initialise les singletons
		//real_state = (GameState<RobotVrai>) container.getService("RealGameState");
		scriptmanager = (ScriptManager) container.getService("ScriptManager");
		deplacements = (DeplacementsHautNiveau)container.getService("DeplacementsHautNiveau");
		dep = (Deplacements)container.getService("Deplacements");
		capteurs = (Capteurs) container.getService("Capteur");

		real_state.robot.initialiser_actionneurs_deplacements();

		// Threads
		container.demarreTousThreads();


		System.out.println("LANCEUR HOMOLO SCRIPT");
		
		
		recalerRobot();
		
		
		//real_state.robot.setPosition(new Vec2(1225,1725));

		// attends que le jumper soit retiré
		attendreDebutMatch();


		System.out.println("Le robot commence le match");
		

        real_state.robot.avancer(300);
        while(!ThreadTimer.fin_match)
        {
        	try
        	{
		        Script s0 = (Script)scriptmanager.getScript("ScriptTree");
				s0.agit(0, real_state, true);
        	}
			catch(Exception e)
			{
			}
        	try
        	{
				Script s1 = (Script)scriptmanager.getScript("ScriptTree");
				s1.agit(1, real_state, true);
        	}
        	catch(Exception e)
			{}
        	try
        	{
				Script s2 = (Script)scriptmanager.getScript("ScriptDeposerFruits");
				s2.agit(1, real_state, true);
			}
        	catch(Exception e)
			{
			}
        	try
        	{
				Script s3 = (Script)scriptmanager.getScript("ScriptTree");
				s3.agit(2, real_state, true);
        	}
        	catch(Exception e)
			{}
        	try
        	{
				Script s4 = (Script)scriptmanager.getScript("ScriptTree");
				s4.agit(3, real_state, true);
        	}
        	catch(Exception e)
			{}
        	try
        	{
				Script s5 = (Script)scriptmanager.getScript("ScriptDeposerFruits");
				s5.agit(0, real_state, true);			
        	}
        	catch(Exception e)
			{}
        	try
        	{
				Script s6 = (Script)scriptmanager.getScript("ScriptLances");
				s6.agit(0, real_state, true);
        	}
        	catch(Exception e)
			{}
        	try
        	{
				Script s7 = (Script)scriptmanager.getScript("ScriptFresque");
				s7.agit(0, real_state, true);
        	}
        	catch(Exception e)
			{}
        	try
        	{
				Script s8 = (Script)scriptmanager.getScript("ScriptLances");
				s8.agit(1, real_state, true);
        	}
        	catch(Exception e)
			{
			}
        	try
        	{
				Script s9 = (Script)scriptmanager.getScript("ScriptFresque");
				s9.agit(0, real_state, true);
        	}
        	catch(Exception e)
			{
			}
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


import robot.cartes.Actionneurs;
import robot.cartes.Deplacements;
import utils.Sleep;
import container.Container;

import javax.swing.JFrame;

public class lanceur_journee_gate
{
	public static boolean avance = false, recule = false;
	public static int codetourne;
	public static int codeactionneurs;
	public static void main(String[] args) throws Exception
	{
		Container container = new Container();
		Deplacements deplacements = (Deplacements) container.getService("Deplacements");
		Actionneurs actionneurs = (Actionneurs) container.getService("Actionneurs");
		deplacements.set_orientation(0);
		deplacements.set_x(0);
		deplacements.set_y(1000);
		deplacements.stopper();
		arcade demo = new arcade();
		
		JFrame fenetre = new JFrame();
        
	    //Définit un titre pour notre fenêtre
	    fenetre.setTitle("INTech - Journée GATE");
	    //Définit sa taille : 400 pixels de large et 100 pixels de haut
	    fenetre.setSize(400, 400);
	    //Nous demandons maintenant à notre objet de se positionner au centre
	    fenetre.setLocationRelativeTo(null);
	    //Termine le processus lorsqu'on clique sur la croix rouge
	    fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    //Et enfin, la rendre visible        
	    fenetre.setVisible(true);
		
	    fenetre.addKeyListener(demo);
	    
	    while(true)
	    {
			while(!avance)
			{
			    
				System.out.println("ça avance pas");
				codeactionneurs = -1 ;
				if(codeactionneurs == 0)
					actionneurs.bac_haut();
				else if(codeactionneurs == 1)
					actionneurs.bac_bas();
				else if(codeactionneurs == 2)
				{
					actionneurs.rateau_ranger_gauche();
					actionneurs.rateau_ranger_droit();
				}
				else if(codeactionneurs == 3)
				{
					actionneurs.rateau_bas_gauche();
					actionneurs.rateau_bas_droit();
				}
				else if(codeactionneurs == 4)
				{
					actionneurs.tirerBalle();
					actionneurs.allume_ventilo();
					Sleep.sleep(500);
					actionneurs.eteint_ventilo();
				}
				Sleep.sleep(200);
			}
	
			if(codetourne >= 0)
				deplacements.tourner(codetourne*Math.PI/2);
			codetourne = -1;
			
			while(avance)
			{
				System.out.println("Ca avance");
				deplacements.avancer(10);
				Sleep.sleep(100);
			}
	    }
	}
}
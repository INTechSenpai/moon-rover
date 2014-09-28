package robot.hautniveau;

import java.util.ArrayList;

import container.Service;
//import hook.Callback;
//import hook.Executable;
import hook.Hook;
import enums.Vitesse;
//import hook.methodes.ChangeConsigne;
//import hook.sortes.HookGenerator;
import exceptions.deplacements.BlocageException;
import exceptions.deplacements.CollisionException;
import exceptions.deplacements.MouvementImpossibleException;
import exceptions.serial.SerialException;
import robot.cartes.Deplacements;
import smartMath.Vec2;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import utils.Sleep;

/**
 * Entre Deplacement (appels à la série) et RobotVrai (déplacements haut niveau), RobotBasNiveau
 * s'occupe de la position, de la symétrie, des hooks, des trajectoires courbes et des blocages.
 * Structure, du bas au haut niveau: symétrie, hook, trajectoire courbe et blocage.
 * Les méthodes "non-bloquantes" se finissent alors que le robot roule encore.
 * (les méthodes non-bloquantes s'exécutent très rapidement)
 * Les méthodes "bloquantes" se finissent alors que le robot est arrêté.
 * @author pf
 *
 */

public class DeplacementsHautNiveau implements Service
{

    private Log log;
    private Read_Ini config;
    private Table table;
    private int largeur_robot;
    private int distance_detection;
    private Vec2 position = new Vec2();  // la position tient compte de la symétrie
    private Vec2 consigne = new Vec2(); // La consigne est un attribut car elle peut être modifiée au sein d'un même mouvement.
    private boolean trajectoire_courbe = false;
    
    private double orientation; // l'orientation tient compte de la symétrie
    private Deplacements deplacements;
//    private HookGenerator hookgenerator;
    private boolean symetrie;
    private int sleep_boucle_acquittement = 10;
    private int nb_iterations_max = 30;
    private int distance_degagement_robot = 50;
//    private int anticipation_trajectoire_courbe = 200;
    private double angle_degagement_robot;
    private boolean insiste = false;
    private long debut_mouvement_fini;
    private boolean fini = true;
    private double[] old_infos;
    
    public DeplacementsHautNiveau(Log log, Read_Ini config, Table table, Deplacements deplacements)
    {
        this.log = log;
        this.config = config;
        this.deplacements = deplacements;
//        this.hookgenerator = hookgenerator;
        this.table = table;
        updateConfig();
    }
    
    public void recaler()
    {
        try {
            if(symetrie)
                setOrientation(0f);
            else
                setOrientation(Math.PI);

            log.debug("recale X",this);
            Sleep.sleep(2000);
            avancer(-200, null, true);
            deplacements.set_vitesse_translation(200);
            deplacements.desactiver_asservissement_rotation();
            Sleep.sleep(1000);
            avancer(-200, null, true);
            deplacements.activer_asservissement_rotation();
            deplacements.set_vitesse_translation(Vitesse.RECALER.PWM_translation);

            position.x = 1500 - 165;
            if(symetrie)
            {
                setOrientation(0f);
                deplacements.set_x(-1500+165);
            }
            else
            {
                deplacements.set_x(1500-165);
                setOrientation(Math.PI);
            }


            Sleep.sleep(500);
            avancer(40, null, true);
            tourner(-Math.PI/2, null, false);

            
        	log.debug("recale Y",this);
            avancer(-600, null, true);
            deplacements.set_vitesse_translation(200);
            deplacements.desactiver_asservissement_rotation();
            Sleep.sleep(1000);
            avancer(-200, null, true);
            deplacements.activer_asservissement_rotation();
            deplacements.set_vitesse_translation(Vitesse.RECALER.PWM_translation);
            position.y = 2000 - 165;
            deplacements.set_y(2000 - 165);
            

        	log.debug("Done !",this);
            Sleep.sleep(500);
            avancer(100, null, false);
            orientation = -Math.PI/2;
            setOrientation(-Math.PI/2);
            //Normalement on se trouve à (1500 - 165 - 100 = 1225 ; 2000 - 165 - 100 = 1725)
            deplacements.activer_asservissement_rotation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Fait tourner le robot (méthode bloquante)
     * @throws MouvementImpossibleException 
     */
    public void tourner(double angle, ArrayList<Hook> hooks, boolean mur) throws MouvementImpossibleException
    {
        log.debug("Symétrie: "+symetrie, this);

        if(symetrie)
            angle = Math.PI-angle;

        // Tourne-t-on dans le sens trigonométrique?
        // C'est important de savoir pour se dégager.
        boolean trigo = angle > orientation;

        try {
            old_infos = deplacements.get_infos_x_y_orientation();
            deplacements.tourner(angle);
            while(!mouvement_fini()) // on attend la fin du mouvement
            {
                Sleep.sleep(sleep_boucle_acquittement);
             //   log.debug("abwa?", this);
            }
        } catch(BlocageException e)
        {
            try
            {
                update_x_y_orientation();
                if(!mur)
                {
                    if(trigo ^ symetrie)
                        deplacements.tourner(orientation+angle_degagement_robot);
                    else
                        deplacements.tourner(orientation-angle_degagement_robot);
                }
            } catch (SerialException e1)
            {
                e1.printStackTrace();
            }
            throw new MouvementImpossibleException();
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
    }
   
    /**
     * Fait avancer le robot de "distance" (en mm).
     * @param distance
     * @param hooks
     * @param insiste
     * @throws MouvementImpossibleException
     */
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws MouvementImpossibleException
    {
        log.debug("Avancer de "+Integer.toString(distance), this);

        System.out.println(position);
        update_x_y_orientation();
        System.out.println(position);

        consigne.x = (int) (position.x + distance*Math.cos(orientation));
        consigne.y = (int) (position.y + distance*Math.sin(orientation));
        System.out.println(consigne);        
        if(symetrie)
            consigne.x = -consigne.x;

        va_au_point_gestion_exception(hooks, false, distance < 0, mur);
    }
        
    /**
     * Suit un chemin. Crée les hooks de trajectoire courbe si besoin est.
     * @param chemin
     * @param hooks
     * @param insiste
     * @throws MouvementImpossibleException
     */
    public void suit_chemin(ArrayList<Vec2> chemin, ArrayList<Hook> hooks) throws MouvementImpossibleException
    {
        if(trajectoire_courbe)
        {
        	log.critical("Désactive la trajectoire courbe, pauvre fou!", this);
/*            consigne = chemin.get(0).clone();
            ArrayList<Hook> hooks_trajectoire = new ArrayList<Hook>();
            for(int i = 0; i < chemin.size()-2; i++)
            {
                Hook hook_trajectoire_courbe = hookgenerator.hook_position(chemin.get(i), anticipation_trajectoire_courbe);
                Executable change_consigne = new ChangeConsigne(chemin.get(i+1), this);
                hook_trajectoire_courbe.ajouter_callback(new Callback(change_consigne, true));
                hooks_trajectoire.add(hook_trajectoire_courbe);
            }

            // TODO: en cas de choc avec un bord, recommencer sans trajectoire courbe?
            
            // Cette boucle est utile si on a "raté" des hooks.
            boolean nouvel_essai = false;
            do {
                if(nouvel_essai)
                    va_au_point_marche_arriere(hooks, hooks_trajectoire, false, false);
                va_au_point_marche_arriere(hooks, hooks_trajectoire, true, false);
                nouvel_essai = false;
                if(hooks_trajectoire.size() != 0)
                    nouvel_essai = true;
            } while(nouvel_essai);

            log.debug("Fin en: "+position, this);
            // Le dernier trajet est exact (sans trajectoire courbe)
            // afin d'arriver exactement au bon endroit.
            consigne = chemin.get(chemin.size()-1).clone();
            va_au_point_marche_arriere(hooks, null, false, false);         */   
        }
        else
            for(Vec2 point: chemin)
            {
                consigne = point.clone();
                va_au_point_marche_arriere(hooks, false, false);
            }
    }

    /**
     * Bloquant. Gère la marche arrière automatique.
     * @param hooks
     * @param insiste
     * @throws MouvementImpossibleException
     */
    private void va_au_point_marche_arriere(ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean mur) throws MouvementImpossibleException
    {
        // choisit de manière intelligente la marche arrière ou non
        // mais cette année, on ne peut aller, de manière automatique, que tout droit.
        // Donc on force marche_arriere à false.
        // Dans les rares cas où on veut la marche arrière, c'est en utilisant avancer.
        // Or, avancer parle directement à va_au_point_gestion_exception

        /*
         * Ce qui suit est une méthode qui permet de choisir si la marche arrière
         * est plus rapide que la marche avant. Non utilisé, mais bon à savoir.
         */
        /*
        Vec2 delta = consigne.clone();
        if(symetrie)
            delta.x *= -1;
        delta.Minus(position);
        // Le coeff 1000 vient du fait que Vec2 est constitué d'entiers
        Vec2 orientationVec = new Vec2((int)(1000*Math.cos(orientation)), (int)(1000*Math.sin(orientation)));

        // On regarde le produit scalaire; si c'est positif, alors on est dans le bon sens, et inversement
        boolean marche_arriere = delta.dot(orientationVec) > 0;
        */
        
        va_au_point_gestion_exception(hooks, trajectoire_courbe, false, mur);
    }
    
    /**
     * Gère les exceptions, c'est-à-dire les rencontres avec l'ennemi et les câlins avec un mur.
     * @param hooks
     * @param trajectoire_courbe
     * @param marche_arriere
     * @param insiste
     * @throws MouvementImpossibleException 
     */
    public void va_au_point_gestion_exception(ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean marche_arriere, boolean mur) throws MouvementImpossibleException
    {
        int nb_iterations_ennemi;
        int nb_iterations_deblocage = 2;
        if(insiste)
            nb_iterations_ennemi = nb_iterations_max;
        else
            nb_iterations_ennemi = 6; // 600 ms
        boolean recommence;
        do {
            recommence = false;
            try
            {
                va_au_point_hook_correction_detection(hooks, trajectoire_courbe, marche_arriere);
            } catch (BlocageException e)
            {
                nb_iterations_deblocage--;
                stopper();
                /*
                 * En cas de blocage, on recule (si on allait tout droit) ou on avance.
                 */
                // Si on insiste, on se dégage. Sinon, c'est juste normal de prendre le mur.
                if(!mur)
                {
                    try
                    {
                        log.warning("On n'arrive plus à avancer. On se dégage", this);
                        if(marche_arriere)
                            deplacements.avancer(distance_degagement_robot);
                        else
                            deplacements.avancer(-distance_degagement_robot);
                        recommence = true;
                    } catch (SerialException e1)
                    {
                        e1.printStackTrace();
                    }
                    try
                    {
                        try
                        {
                            old_infos = deplacements.get_infos_x_y_orientation();
                        } catch (SerialException e1)
                        {
                            e1.printStackTrace();
                        }
                        while(!mouvement_fini());
                    } catch (BlocageException e1)
                    {
                        stopper();
                        log.critical("On n'arrive pas à se dégager.", this);
                        throw new MouvementImpossibleException();
                    }
                    if(nb_iterations_deblocage <= 0)
                        throw new MouvementImpossibleException();
                }
            } catch (CollisionException e)
            {
                nb_iterations_ennemi--;
                /*
                 * En cas d'ennemi, on attend (si on demande d'insiste) ou on abandonne.
                 */
                if(nb_iterations_ennemi <= 0)
                {
                    /* TODO: si on veut pouvoir enchaîner avec un autre chemin, il
                     * ne faut pas arrêter le robot.
                     * ATTENTION! ceci peut être dangereux, car si aucun autre chemin
                     * ne lui est donné, le robot va continuer sa course et percuter
                     * l'obstacle!
                     */
                    stopper();
                    log.critical("Détection d'un ennemi! Abandon du mouvement.", this);
                    throw new MouvementImpossibleException();
                } //TODO: vérifier fréquemment, puis attendre
                else
                {
                    log.warning("Détection d'un ennemi! Attente.", this);
                    stopper();
                    Sleep.sleep(100);
                    recommence = true;
                }
            }
        } while(recommence); // on recommence tant qu'on n'a pas fait trop d'itérations.

    // Tout s'est bien passé
    }
    
    /**
     * Bloquant. Gère les hooks, la correction de trajectoire et la détection.
     * @param point
     * @param hooks
     * @param trajectoire_courbe
     * @throws BlocageException 
     * @throws CollisionException 
     */
    public void va_au_point_hook_correction_detection(ArrayList<Hook> hooks, boolean trajectoire_courbe, boolean marche_arriere) throws BlocageException, CollisionException
    {
        boolean relancer;
        va_au_point_symetrie(trajectoire_courbe, marche_arriere, false);
        try
        {
            old_infos = deplacements.get_infos_x_y_orientation();
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
        do
        {
            relancer = false;
            detecter_collision(!marche_arriere);

            if(hooks != null)
                for(Hook hook : hooks)
                {
                    Vec2 sauv_consigne = consigne.clone();
                    relancer |= hook.evaluate();
                    consigne = sauv_consigne;
                }

            // Correction de la trajectoire ou reprise du mouvement
            // Si on ne fait que relancer et qu'on a interdit la trajectoire courbe, on attend à la rotation.
            if(relancer)
            {
                log.debug("On relance", this);
                va_au_point_symetrie(false, marche_arriere, trajectoire_courbe);
            }
            else
                update_x_y_orientation();

        } while(!mouvement_fini());
        
    }

    /**
     * Non bloquant. Gère la symétrie et la marche arrière.
     * @param point
     * @param sans_lever_exception
     * @param trajectoire_courbe
     * @param marche_arriere
     * @throws BlocageException 
     */
    public void va_au_point_symetrie(boolean trajectoire_courbe, boolean marche_arriere, boolean correction) throws BlocageException
    {
        Vec2 delta = consigne.clone();
        if(symetrie)
            delta.x = -delta.x;
        
        long t1 = System.currentTimeMillis();
        update_x_y_orientation();
        long t2 = System.currentTimeMillis();

        delta.Minus(position);
        double distance = delta.Length();
        if(correction)
            distance -= (t2-t1);
        
        //gestion de la marche arrière du déplacement (peut aller à l'encontre de marche_arriere)
        double angle =  Math.atan2(delta.y, delta.x);
        if(marche_arriere)
        {
            distance *= -1;
            angle += Math.PI;
        }        
        
        va_au_point_courbe(angle, distance, trajectoire_courbe, correction);
        
    }
    
    /**
     * Non bloquant. Avance, de manière courbe ou non.
     * @param angle
     * @param distance
     * @param trajectoire_courbe
     * @throws BlocageException 
     */
    public void va_au_point_courbe(double angle, double distance, boolean trajectoire_courbe, boolean correction) throws BlocageException
    {
        // On interdit la trajectoire courbe si on doit faire un virage trop grand.
        if(Math.abs(angle - orientation) > Math.PI/2)
//            if(correction)
 //               return;
 //           else
                trajectoire_courbe = false;
        try
        {
            deplacements.tourner(angle);
            old_infos = deplacements.get_infos_x_y_orientation();
            if(!trajectoire_courbe) // sans virage : la première rotation est bloquante
                while(!mouvement_fini()) // on attend la fin du mouvement
                    Sleep.sleep(sleep_boucle_acquittement);
            
            deplacements.avancer(distance);
        } catch (SerialException e) {
            e.printStackTrace();
        }
    }

    /**
     * Faux si le robot bouge encore, vrai si arrivée au bon point, exception si blocage
     * @return
     * @throws BlocageException
     */
    
    // TODO: wut ?
    private boolean mouvement_fini() throws BlocageException
    {
        boolean out = false;
        try
        {
            double[] new_infos = deplacements.get_infos_x_y_orientation();
            /*
            System.out.println("x: "+new_infos[0]);
            System.out.println("y: "+new_infos[1]);
            System.out.println("o: "+new_infos[2]);
            System.out.println("distance² diff: "+new Vec2((int)old_infos[0], (int)old_infos[1]).SquaredDistance(new Vec2((int)new_infos[0], (int)new_infos[1])));
            System.out.println("angle diff: "+Math.abs(new_infos[2] - old_infos[2]));
*/
            if(new Vec2((int)old_infos[0], (int)old_infos[1]).SquaredDistance(new Vec2((int)new_infos[0], (int)new_infos[1])) > 20 || Math.abs(new_infos[2] - old_infos[2]) > 20)
                out = false;
            else if(new Vec2((int)new_infos[0], (int)new_infos[1]).SquaredDistance(consigne) < 10)
                out = true;
            else
                throw new BlocageException();
   
            old_infos = new_infos;
        } catch (SerialException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Surcouche de mouvement_fini afin de ne pas freezer
     * @return
     * @throws BlocageException
     */
/*    private boolean mouvement_fini() throws BlocageException
    {
        if(nouveau_mouvement)
            debut_mouvement_fini = System.currentTimeMillis();
        nouveau_mouvement = false;
        fini = mouvement_fini_routine();
        if(!fini && ((System.currentTimeMillis() - debut_mouvement_fini) > 2000))
        {
            log.critical("Erreur d'acquittement. On arrête l'attente du robot.", this);
            fini = true;
        }
        return fini;
    }*/
    
    /**
     * Boucle d'acquittement générique. Retourne des valeurs spécifiques en cas d'arrêt anormal (blocage, capteur)
     *  	
     *  	false : si on roule
     *  	true : si arrivé a destination
     *  	exeption : si patinage
     * 
     * 
     * @param detection_collision
     * @param sans_lever_exception
     * @return oui si le robot est arrivé à destination, non si encore en mouvement
     * @throws BlocageException
     * @throws CollisionException
     */
    private boolean mouvement_fini_routine() throws BlocageException
    {
        // récupérations des informations d'acquittement
        try {
        	
        	// met a jour: 	l'écart entre la position actuelle et la position sur laquelle on est asservi
        	//				la variation de l'écart a la position sur laquelle on est asservi
        	//				la puissance demandée par les moteurs 	
            deplacements.maj_infos_stoppage_enMouvement();
            
            // lève une exeption de blocage si le robot patine (ie force sur ses moteurs sans bouger) 
            deplacements.leverExeptionSiPatinage();
            
            // robot arrivé?
//            System.out.println("deplacements.update_enMouvement() : " + deplacements.isRobotMoving());
            return !deplacements.isRobotMoving();

        } 
        catch (SerialException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * fonction vérifiant que l'on ne va pas taper dans le robot adverse. 
     * @param devant: fait la détection derrière le robot si l'on avance à reculons 
     * @throws CollisionException si obstacle sur le chemin
     */
    private void detecter_collision(boolean devant) throws CollisionException
    {
        int signe = -1;
        if(devant)
            signe = 1;
        
        int rayon_detection = largeur_robot/2 + distance_detection;
        Vec2 centre_detection = new Vec2((int)(signe * rayon_detection * Math.cos(orientation)), (int)(signe * rayon_detection * Math.sin(orientation)));
        centre_detection.Plus(position);
        if(table.gestionobstacles.obstaclePresent(centre_detection, distance_detection))
        {
            log.warning("Ennemi détecté en : " + centre_detection, this);
            throw new CollisionException();
        }

    }

    /**
     * Met à jour position et orientation via la carte d'asservissement.
     * @throws SerialException
     */
    private void update_x_y_orientation()
    {
        try {
            double[] infos = deplacements.get_infos_x_y_orientation();
            position.x = (int)infos[0];
            position.y = (int)infos[1];
            orientation = infos[2]/1000; // car get_infos renvoie des milliradians
        }
        catch(SerialException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateConfig()
    {
        nb_iterations_max = Integer.parseInt(config.get("nb_tentatives"));
        distance_detection = Integer.parseInt(config.get("distance_detection"));
        distance_degagement_robot = Integer.parseInt(config.get("distance_degagement_robot"));
        sleep_boucle_acquittement = Integer.parseInt(config.get("sleep_boucle_acquittement"));
        angle_degagement_robot = Double.parseDouble(config.get("angle_degagement_robot"));
//        anticipation_trajectoire_courbe = Integer.parseInt(config.get("anticipation_trajectoire_courbe"));
        trajectoire_courbe = Boolean.parseBoolean(config.get("trajectoire_courbe"));
        symetrie = config.get("couleur").equals("rouge");
    }

    /**
     * Arrête le robot.
     */
    public void stopper()
    {
        log.debug("Arrêt du robot en "+position, this);
        try {
            deplacements.stopper();
        } catch (SerialException e) {
            e.printStackTrace();
        }           
    }
    
    /**
     * Met à jour la consigne (utilisé par les hooks)
     * @param point
     */
    public void setConsigne(Vec2 point)
    {
        log.debug("Nouvelle consigne: "+point, this);
        consigne = point.clone();
    }

    /**
     * Met à jour la position. A ne faire qu'en début de match.
     * @param position
     */
    public void setPosition(Vec2 position) {
        this.position = position.clone();
        try {
            deplacements.set_x(position.x);
            deplacements.set_y(position.y);
        } catch (SerialException e) {
            e.printStackTrace();
        }
        Sleep.sleep(300);
    }

    /**
     * Met à jour l'orientation. A ne faire qu'en début de match.
     * @param orientation
     */
    public void setOrientation(double orientation) {
        this.orientation = orientation;
        try {
            deplacements.set_orientation(orientation);
        } catch (SerialException e) {
            e.printStackTrace();
        }
    }

    public Vec2 getPosition()
    {
        update_x_y_orientation();
        return position.clone();
    }

    public Vec2 getPositionFast()
    {
        return position.clone();
    }

    public double getOrientation()
    {
        update_x_y_orientation();
        return orientation;
    }

    public double getOrientationFast()
    {
        return orientation;
    }

    public void desasservit()
    {
        try
        {
            deplacements.desactiver_asservissement_rotation();
            deplacements.desactiver_asservissement_translation();
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
    }

    public void set_vitesse_rotation(int pwm_max)
    {
        try
        {
            deplacements.set_vitesse_rotation(pwm_max);
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
    }

    public void set_vitesse_translation(int pwm_max)
    {
        try
        {
            deplacements.set_vitesse_translation(pwm_max);
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
    }

    
    public void asservit()
    {
        try
        {
            deplacements.activer_asservissement_rotation();
            deplacements.activer_asservissement_translation();
        } catch (SerialException e)
        {
            e.printStackTrace();
        }
    }
    
    public void initialiser_deplacements()
    {}
    
    public void setInsiste(boolean insiste)
    {
        this.insiste = insiste;
    }

    public void desactiver_asservissement_rotation()
    {
    	try
		{
			deplacements.desactiver_asservissement_rotation();
		}
		catch (SerialException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void activer_asservissement_rotation()
    {
    	try
		{
			deplacements.activer_asservissement_rotation();
		}
		catch (SerialException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}

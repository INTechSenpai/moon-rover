package hook.methodes;

import hook.Executable;
import robot.hautniveau.DeplacementsHautNiveau;
import smartMath.Vec2;

/**
 * Classe implémentant la méthode changement de consigne, utilisée pour avoir une trajectoire courbe.
 * @author pf
 *
 */

public class ChangeConsigne implements Executable {

        private Vec2 nouvelle_consigne;
        private DeplacementsHautNiveau robot;
        
        public ChangeConsigne(Vec2 nouvelle_consigne, DeplacementsHautNiveau robot)
        {
            this.robot = robot;
            this.nouvelle_consigne = nouvelle_consigne;
        }
        
        @Override
        public boolean execute()
        {
            robot.setConsigne(nouvelle_consigne);
            return true; // le robot doit bouger
        }
        
}

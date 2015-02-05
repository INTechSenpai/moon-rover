package tests;

import org.junit.Before;
import org.junit.Test;

import enums.Side;
import robot.RobotReal;
import utils.Sleep;

public class JUnit_RobotReal extends JUnit_Test
{
    RobotReal robotvrai;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        robotvrai = (RobotReal) container.getService("RobotVrai");
    }

    @Test
    public void test_recaler() throws Exception
    {
        robotvrai.recaler();
    }
    
    // TODO : tester chaque action de cette facon
    /*
    @Test
    public void test_takefire() throws Exception
    {
        robotvrai.initialiser_actionneurs_deplacements();
        Sleep.sleep(2000);
        robotvrai.takefire(Cote.GAUCHE, Cote.GAUCHE);
    }
*/
}

package tests;

import org.junit.Before;
import org.junit.Test;

import enums.Cote;
import robot.RobotVrai;
import utils.Sleep;

public class JUnit_RobotVraiTest extends JUnit_Test
{
    RobotVrai robotvrai;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        robotvrai = (RobotVrai) container.getService("RobotVrai");
    }

    @Test
    public void test_recaler() throws Exception
    {
        robotvrai.initialiser_actionneurs_deplacements();
        robotvrai.recaler();
    }
    
    @Test
    public void test_takefire() throws Exception
    {
        robotvrai.initialiser_actionneurs_deplacements();
        Sleep.sleep(2000);
        robotvrai.takefire(Cote.GAUCHE, Cote.GAUCHE);
    }

}

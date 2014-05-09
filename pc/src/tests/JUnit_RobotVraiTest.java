package tests;

import org.junit.Before;
import org.junit.Test;

import robot.RobotVrai;

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
    
}

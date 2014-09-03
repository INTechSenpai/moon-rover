package tests;

import org.junit.Before;
import org.junit.Test;

import robot.hautniveau.ActionneursHautNiveau;

public class JUnit_ActionneursHautNiveauTest extends JUnit_Test
{
    ActionneursHautNiveau actionneurs;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        actionneurs = (ActionneursHautNiveau) container.getService("ActionneursHautNiveau");
    }

    @Test
    public void test_initialisation() throws Exception
    {
        actionneurs.initialiser_actionneurs();
    }
    
}

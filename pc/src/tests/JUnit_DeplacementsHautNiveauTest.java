package tests;

import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.types.HookGenerator;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import robot.RobotReal;
import robot.highlevel.LocomotionHiLevel;
import smartMath.Vec2;
import strategie.GameState;
import utils.Sleep;

/**
 * Teste les fonctions de déplacement de haut niveau
 * @author pf
 *
 */

// TODO : comprendre ce système

public class JUnit_DeplacementsHautNiveauTest extends JUnit_Test
{
    private LocomotionHiLevel robot;
    private HookGenerator hookgenerator;
    private GameState<RobotReal> real_state;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        super.setUp();
        robot = (LocomotionHiLevel) container.getService("DeplacementsHautNiveau");
        hookgenerator = (HookGenerator) container.getService("HookGenerator");
        real_state = (GameState<RobotReal>) container.getService("RealGameState");
        robot.setPosition(new Vec2(1000, 900));
        robot.setOrientation(Math.PI/2);
        Vec2 consigne = new Vec2(700, 1400);
        robot.setConsigne(consigne);
    }
    
    @Test
    public void test_va_au_point_courbe() throws Exception
    {
//        robot.va_au_point_courbe((float) Math.PI, 500, false);
        robot.moveForwardInDirection((float) (Math.PI/4), 500, true);
    }

    @Test
    public void test_va_au_point_symetrie() throws Exception
    {
        robot.va_au_point_symetrie(false, true, false);
    }
    
    @Test
    public void test_va_au_point_hook() throws Exception
    {
    	// TODO
    }

    @Test
    public void test_va_au_point_correction() throws Exception
    {
        robot.va_au_point_hook_correction_detection(null, false, false);
    }

    @Test
    public void test_va_au_point_detection() throws Exception
    {
        container.startAllThreads();
        robot.va_au_point_gestion_exception(null, true, false, false);
    }

    @Test
    public void test_va_au_point_relancer() throws Exception
    {
    	// TODO : une classe de type Executable
    	/*
        ArrayList<Hook> hooks = new ArrayList<Hook>();
        Executable takefire = new TakeFire(real_state.robot);
        Hook hook = hookgenerator.hook_position(new Vec2(850, 1150), 100);
        hook.ajouter_callback(new Callback(takefire, true));
        hooks.add(hook);
        robot.va_au_point_hook_correction_detection(hooks, false, false);
        */
    }

    @Test
    public void test_recaler() throws Exception
    {
        robot.readjust();
    }
    
    @Test
    public void test_suit_chemin() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            ArrayList<Vec2> chemin = new ArrayList<Vec2>();
            chemin.add(new Vec2(1000, 1200));
            chemin.add(new Vec2(0, 1300));
            chemin.add(new Vec2(-1000, 1200));
            chemin.add(new Vec2(0, 500));
            chemin.add(new Vec2(1000, 1200));
            robot.suit_chemin(chemin, null);
        }
    }

    @Test
    public void test_avancer() throws Exception
    {
        robot.avancer(50, null, false);
        Sleep.sleep(1000);
        robot.avancer(-50, null, false);
    }

    @Test
    public void test_avancer_mur() throws Exception
    {
        container.startAllThreads();
//        robot.avancer(1500, null, true);
        real_state.robot.avancer_dans_mur(1500);
    }

    @Test
    public void test_vitesse_avancer() throws Exception
    {
        real_state.robot.avancer(200);
        Sleep.sleep(1000);
        real_state.robot.avancer_dans_mur(200);
        Sleep.sleep(1000);
        real_state.robot.avancer(200);
        Sleep.sleep(1000);
        real_state.robot.avancer_dans_mur(200);
        Sleep.sleep(1000);
        real_state.robot.avancer(200);
    }

}

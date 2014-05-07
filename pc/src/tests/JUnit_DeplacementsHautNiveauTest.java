package tests;

import hook.Callback;
import hook.Executable;
import hook.Hook;
import hook.methodes.TakeFire;
import hook.methodes.TirerBalles;
import hook.sortes.HookGenerator;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import enums.Cote;
import robot.RobotVrai;
import robot.hautniveau.DeplacementsHautNiveau;
import smartMath.Vec2;
import strategie.GameState;
import utils.Sleep;

public class JUnit_DeplacementsHautNiveauTest extends JUnit_Test
{
    private DeplacementsHautNiveau robot;
    private HookGenerator hookgenerator;
    private GameState<RobotVrai> real_state;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        super.setUp();
        robot = (DeplacementsHautNiveau) container.getService("DeplacementsHautNiveau");
        hookgenerator = (HookGenerator) container.getService("HookGenerator");
        real_state = (GameState<RobotVrai>) container.getService("RealGameState");
        robot.setPosition(new Vec2(1000, 900));
        robot.setOrientation(Math.PI/2);
        Sleep.sleep(500);
        Vec2 consigne = new Vec2(700, 1400);
        robot.setConsigne(consigne);
    }
    
    @Test
    public void test_va_au_point_courbe() throws Exception
    {
//        robot.va_au_point_courbe((float) Math.PI, 500, false);
        robot.va_au_point_courbe((float) (Math.PI/4), 500, true, false);
    }

    @Test
    public void test_va_au_point_symetrie() throws Exception
    {
        robot.va_au_point_symetrie(false, true, false);
    }

    @Test
    public void test_va_au_point_hook() throws Exception
    {
        int nb_balles = real_state.robot.getNbrLances();
        ArrayList<Hook> hooks = new ArrayList<Hook>();
        Executable tirerballes = new TirerBalles(real_state.robot);
        Hook hook = hookgenerator.hook_position(new Vec2(850, 1150), 200);
        hook.ajouter_callback(new Callback(tirerballes, true));
        hooks.add(hook);
        robot.va_au_point_hook_correction_detection(hooks, null, false, false);
        Assert.assertTrue(nb_balles != real_state.robot.getNbrLances());
    }

    @Test
    public void test_va_au_point_correction() throws Exception
    {
        robot.va_au_point_hook_correction_detection(null, null, false, false);
    }

    @Test
    public void test_va_au_point_detection() throws Exception
    {
        container.demarreTousThreads();
        robot.setInsiste(true);
        robot.va_au_point_gestion_exception(null, null, false, false, false);
    }

    @Test
    public void test_va_au_point_relancer() throws Exception
    {
        ArrayList<Hook> hooks = new ArrayList<Hook>();
        Executable takefire = new TakeFire(real_state.robot, Cote.DROIT);
        Hook hook = hookgenerator.hook_position(new Vec2(850, 1150), 100);
        hook.ajouter_callback(new Callback(takefire, true));
        hooks.add(hook);
        robot.va_au_point_hook_correction_detection(hooks, null, false, false);
    }
    
    @Test
    public void test_suit_chemin() throws Exception
    {
        ArrayList<Vec2> chemin = new ArrayList<Vec2>();
        chemin.add(new Vec2(1000, 1200));
        chemin.add(new Vec2(0, 1300));
        chemin.add(new Vec2(-1000, 1200));
        chemin.add(new Vec2(0, 500));
        chemin.add(new Vec2(1000, 1200));
        robot.setInsiste(true);
        container.demarreTousThreads();
        robot.suit_chemin(chemin, null);
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
        robot.avancer(200, null, true);
    }

}

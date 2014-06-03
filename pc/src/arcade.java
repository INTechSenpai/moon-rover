
import utils.Sleep;

import java.awt.event.*;
				

public class arcade implements KeyListener {

	KeyEvent key;
	
	public arcade ()  {
	}

		
	@Override
	public void keyPressed(KeyEvent e) {
		lanceur_journee_gate.avance = true;
		if (e.getKeyCode() == KeyEvent.VK_UP)
		{
			if (lanceur_journee_gate.codetourne != 1)
			{
				lanceur_journee_gate.codetourne = 1;
				Sleep.sleep(1000);
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			if (lanceur_journee_gate.codetourne != 3)
			{
			lanceur_journee_gate.codetourne = 3;
			Sleep.sleep(1000);
			}
		}
			
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			if (lanceur_journee_gate.codetourne != 0)
			{
			lanceur_journee_gate.codetourne = 0;
			Sleep.sleep(1000);
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
			if (lanceur_journee_gate.codetourne != 2)
			{
			lanceur_journee_gate.codetourne = 2;
			Sleep.sleep(1000);
			}
		}
		else
			lanceur_journee_gate.avance = false;

		}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		lanceur_journee_gate.avance = false;
	}
	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			lanceur_journee_gate.codeactionneurs = 0;
		}
		else if (e.getKeyCode() == KeyEvent.VK_Z)
		{
			lanceur_journee_gate.codeactionneurs = 1;
		}
		else if (e.getKeyCode() == KeyEvent.VK_X)
		{
			lanceur_journee_gate.codeactionneurs = 2;
		}
		else if (e.getKeyCode() == KeyEvent.VK_ALT)
		{
			lanceur_journee_gate.codeactionneurs = 3;
		}
		else if (e.getKeyChar() == KeyEvent.VK_1)
		{
			lanceur_journee_gate.codeactionneurs = 4;
		}
		// TODO Auto-generated method stub
		
	}
}


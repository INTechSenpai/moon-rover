package scripts;

import robot.RobotChrono;
import strategie.GameState;
import enums.ScriptNames;

/**
 * Classe qui caractérise une décision.
 * C'est un script et ses paramètres.
 * @author pf
 *
 */

public class Decision {

	public ScriptNames script_name;
	public int id_version;
	public double note;
	public GameState<RobotChrono> state; // état du jeu juste après cette décision
	
	public Decision(ScriptNames s, int id_version, double note)
	{
		this.script_name = s;
		this.id_version = id_version;
		this.note = note;
	}
	
}

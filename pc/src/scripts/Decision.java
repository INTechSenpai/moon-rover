package scripts;

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
	public boolean shoot_game_element;
	
	public Decision(ScriptNames s, int id_version, double note, boolean shoot_game_element)
	{
		this.script_name = s;
		this.id_version = id_version;
		this.note = note;
		this.shoot_game_element = shoot_game_element;
	}
	
	public void copy(Decision other)
	{
		other.script_name = script_name;
		other.id_version = id_version;
		other.note = note;
		other.shoot_game_element = shoot_game_element;
	}
	
}

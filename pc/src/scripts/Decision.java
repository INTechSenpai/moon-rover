package scripts;

import pathfinding.Arc;
import enums.ScriptNames;

/**
 * Classe qui caractérise une décision.
 * C'est un script et ses paramètres.
 * @author pf
 *
 */

public class Decision implements Arc {

	public ScriptNames script_name;
	public int meta_version;
	public boolean shoot_game_element;
	
	public Decision(ScriptNames s, int meta_version, boolean shoot_game_element)
	{
		this.script_name = s;
		this.meta_version = meta_version;
		this.shoot_game_element = shoot_game_element;
	}
	
	public String toString()
	{
		return script_name+", version "+meta_version+", shoot? "+shoot_game_element;
	}
	
}

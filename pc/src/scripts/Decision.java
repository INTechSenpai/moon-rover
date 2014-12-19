package scripts;

import java.util.ArrayList;

import pathfinding.Arc;
import enums.PathfindingNodes;
import enums.ScriptNames;

/**
 * Classe qui caractérise une décision.
 * C'est un script et ses paramètres.
 * @author pf
 *
 */

public class Decision implements Arc {

	public ScriptNames script_name;
	public int version;
	public boolean shoot_game_element;
	public ArrayList<PathfindingNodes> chemin;
	
	public Decision(ArrayList<PathfindingNodes> chemin, ScriptNames s, int meta_version, boolean shoot_game_element)
	{
		this.chemin = chemin;
		this.script_name = s;
		this.version = meta_version;
		this.shoot_game_element = shoot_game_element;
	}
	
	public String toString()
	{
		return script_name+", version "+version+", shoot? "+shoot_game_element;
	}
	
}

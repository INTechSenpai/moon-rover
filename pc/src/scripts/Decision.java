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
	
	public Decision(ScriptNames s, int id_version)
	{
		this.script_name = s;
		this.id_version = id_version;
	}
	
}

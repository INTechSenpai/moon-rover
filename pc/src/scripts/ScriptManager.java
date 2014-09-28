package scripts;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import utils.Log;
import utils.Read_Ini;
import container.Service;
import exceptions.ScriptException;

 /**
  * Classe enregistrée comme service qui fournira les scripts
  * @author pf, marsu
  */
 
public class ScriptManager implements Service
{
	
	private Log log;

	// pour retrouver un script a partir de son nom
	private Map<String,Script> instancesScripts = new Hashtable<String,Script>(); // ce commentaire est inutile

	// TODO : effacer ?
	private ArrayList<String> scripts_robot;
	
	public ScriptManager(Read_Ini config, Log log)
	{
		this.log = log;
		scripts_robot = new ArrayList<String>();
	}
	
	public Script getScript(String nom) throws ScriptException
	{
		Script script = instancesScripts.get(nom);
		if(script == null)
		{
			log.warning("Script inconnu: "+nom, this);
			throw new ScriptException();
		}
		return script;
	}
	
	public void updateConfig()
	{
	}

}
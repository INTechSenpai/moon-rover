package robot;

import scripts.ScriptManager;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

public class Strategie implements Service {

	private ScriptManager scriptmanager;
	private Table table;
	private Read_Ini config;
	private Log log;
	
	public Strategie(Service scriptmanager, Service table, Service config, Service log)
	{
		this.scriptmanager = (ScriptManager) scriptmanager;
		this.table = (Table) table;
		this.config = (Read_Ini) config;
		this.log = (Log) log;
	}
	
	
}

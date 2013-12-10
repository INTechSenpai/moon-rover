package scripts;

import hook.HookGenerator;
import robot.Robot;
import robot.RobotChrono;
import robot.RobotVrai;
import table.Table;
import utils.Log;
import utils.Read_Ini;
import container.Service;

/**
 * Classe enregistrée comme service qui fournira les scripts
 * @author pf
 */


public class ScriptManager implements Service {
	
	Robot robot;
	RobotVrai robotvrai;
	RobotChrono robotchrono;
	HookGenerator hookgenerator;
	Table table;
	Read_Ini config;
	Log log;
	
	// TODO mettre les vrais scripts...
	Script scriptTest;
	
	public ScriptManager(Service robotvrai, Service robotchrono, Service hookgenerator, Service table, Service config, Service log) {
		this.robotvrai = (RobotVrai) robotvrai;
		this.robotchrono = (RobotChrono) robotchrono;
		this.hookgenerator = (HookGenerator) hookgenerator;
		this.table = (Table) table;
		this.config = (Read_Ini) config;
		this.log = (Log) log;

	}
	
}

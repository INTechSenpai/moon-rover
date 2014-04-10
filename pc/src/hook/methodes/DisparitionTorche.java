package hook.methodes;

import hook.Executable;
import robot.Cote;
import table.Table;

/**
 * Classe implémentant la méthode de disparition de torche, utilisée lors des déplacements
 * @author pf
 *
 */

public class DisparitionTorche implements Executable {

		private Cote cote;
		private Table table;
		
		public DisparitionTorche(Table table, Cote cote)
		{
			this.table = table;
			this.cote = cote;
		}
		
		@Override
		public void execute()
		{
			table.torche_disparue(cote);
		}

		@Override
		public boolean bougeRobot() {
			return false;
		}	
		
}

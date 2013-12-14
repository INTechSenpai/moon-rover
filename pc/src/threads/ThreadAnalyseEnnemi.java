package threads;

import smartMath.Vec2;
import table.Table;

/**
 * Thread qui analyse le comportement de l'ennemi à partir de sa position
 * @author pf
 *
 */
public class ThreadAnalyseEnnemi extends AbstractThread  {

	Table table;
	ThreadTimer threadtimer;
	
	long[] date_freeze;
	Vec2[] positionsfreeze;
	int tolerance = 1000;
	
	public ThreadAnalyseEnnemi(Table table, ThreadTimer threadtimer)
	{
		this.table = table;
		this.threadtimer = threadtimer;
		positionsfreeze = table.get_positions_ennemis();
	}
	
	@Override
	public void run() {
		while(!threadtimer.match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Stoppage du thread laser", this);
				return;
			}
			sleep(100);
		}

		date_freeze[0] = System.currentTimeMillis();
		date_freeze[1] = System.currentTimeMillis();
		
		while(!threadtimer.fin_match)
		{
			Vec2[] positionsEnnemi = table.get_positions_ennemis();
			for(int i = 0; i < 2; i++)
			{
				// défreeze
				if(positionsfreeze[i].SquaredDistance(positionsEnnemi[i]) > 0)
				{
					date_freeze[i] = System.currentTimeMillis();
					positionsfreeze[i] = positionsEnnemi[i];
				}
			
			}
			sleep(500); // le sleep peut être long, le robot adverse ne bouge de toute façon pas très vite...
		}
	}

	public int[] duree_freeze()
	{
		int[] duree_freeze = new int[2];
		duree_freeze[0] = (int)(System.currentTimeMillis() - date_freeze[0]);
		duree_freeze[1] = (int)(System.currentTimeMillis() - date_freeze[1]);
		return duree_freeze;
	}
	
}

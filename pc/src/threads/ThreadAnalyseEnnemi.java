package threads;

import smartMath.Vec2;
import strategie.Strategie;
import table.Table;
import utils.Sleep;

/**
 * Thread qui analyse le comportement de l'ennemi à partir de sa position
 * @author pf, krissprolls
 *
 */
public class ThreadAnalyseEnnemi extends AbstractThread  {

	Table table;
	ThreadTimer threadtimer;
	Strategie strategie;
	
	private long[] date_freeze = new long[2];
	public Vec2[] positionsfreeze = new Vec2[2];
//	private int tolerance = 1000;
	
	public ThreadAnalyseEnnemi(Table table, ThreadTimer threadtimer, Strategie strategie)
	{
		this.table = table;
		this.threadtimer = threadtimer;
		this.strategie = strategie;
		positionsfreeze = table.get_positions_ennemis();
	}
	
	@Override
	public void run() {
		log.debug("Lancement du thread d'analyse de l'ennemi", this);

		while(!threadtimer.match_demarre)
		{
			if(stop_threads)
			{
				log.debug("Arrêt du thread d'analyse de l'ennemi", this);
				return;
			}
			Sleep.sleep(100);
		}

		date_freeze[0] = System.currentTimeMillis();
		date_freeze[1] = System.currentTimeMillis();
		
		while(!threadtimer.fin_match)
		{
			if(stop_threads)
			{
				log.debug("Arrêt du thread d'analyse de l'ennemi", this);
				return;
			}

			Vec2[] positionsEnnemi = table.get_positions_ennemis();
			for(int i = 0; i < 2; i++)
			{
				// défreeze
				// TODO
				if(positionsfreeze[i].SquaredDistance(positionsEnnemi[i]) > 30)
				{
					date_freeze[i] = System.currentTimeMillis();
					positionsfreeze[i] = positionsEnnemi[i];
				}			
			}
			
			strategie.analyse_ennemi(positionsfreeze, duree_freeze());
			
			Sleep.sleep(500); // le sleep peut être long, le robot adverse ne bouge de toute façon pas très vite...
		}
		log.debug("Arrêt du thread d'analyse de l'ennemi", this);

	}


	/**
	 * Donne à la stratégie les durées de freeze de chaque robot
	 * @return
	 */
	public int[] duree_freeze()
	{
		int[] duree_freeze = new int[2];
		duree_freeze[0] = (int)(System.currentTimeMillis() - date_freeze[0]);
		duree_freeze[1] = (int)(System.currentTimeMillis() - date_freeze[1]);
		return duree_freeze;
	}
	
}

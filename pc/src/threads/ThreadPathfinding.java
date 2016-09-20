/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package threads;

import exceptions.PathfindingException;
import pathfinding.astarCourbe.AStarCourbe;
import pathfinding.chemin.CheminPathfinding;
import utils.Config;
import utils.Log;

/**
 * Thread qui recalcule l'itinéraire à emprunter. Surveille ObstaclesMemory.
 * @author pf
 *
 */

public class ThreadPathfinding extends ThreadService
{
	protected Log log;
	private AStarCourbe pathfinding;
	private CheminPathfinding chemin;

	public ThreadPathfinding(Log log, AStarCourbe pathfinding, CheminPathfinding chemin)
	{
		this.log = log;
		this.pathfinding = pathfinding;
		this.chemin = chemin;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		log.debug("Démarrage de "+Thread.currentThread().getName());
		try {
			synchronized(chemin)
			{
				chemin.wait();
				try {
					pathfinding.updatePath();
				} catch (PathfindingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			log.debug("Arrêt de "+Thread.currentThread().getName());
		}
	}

	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{}
	
}

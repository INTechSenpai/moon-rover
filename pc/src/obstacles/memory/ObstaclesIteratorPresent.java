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

package obstacles.memory;

import utils.Log;

/**
 * Itérator permettant de manipuler facilement les obstacles mobiles du présent
 * @author pf
 *
 */

public class ObstaclesIteratorPresent extends ObstaclesIterator
{
    public ObstaclesIteratorPresent(Log log, ObstaclesMemory memory)
    {
    	super(log, memory);
    }
    
	/**
	 * Calcule l'entrée où commencent les obstacles maintenant
	 */
	public void reinit()
	{
		nbTmp = memory.getFirstNotDeadNow() - 1;
	}
	
	/**
	 * Pour parcourir tous ceux qui sont morts (utilisé par le GridSpace)
	 * @return
	 */
	public boolean hasNextDead()
	{
		while(nbTmp + 1 < memory.getFirstNotDeadNow() && memory.getObstacle(nbTmp + 1) == null)
			nbTmp++;
		
		return nbTmp + 1 < memory.getFirstNotDeadNow();
	}
}

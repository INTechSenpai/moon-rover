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

package serie;

/**
 * Un ticket. Tu tires un numéro et tu attends ton tour.
 * Utilisé par la série pour notifier des infos.
 * @author pf
 *
 */

public class Ticket
{
	public enum State
	{
		OK, KO;
	}

	private volatile State type;
	
	public synchronized State getAndClear()
	{
		State out = type;
		type = null;
		return out;
	}
	
	public synchronized boolean isEmpty()
	{
		return type == null;
	}
	
	public synchronized void set(State type)
	{
		this.type = type;
		notify();
	}
	
}

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

package scripts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import container.Service;
import utils.Log;

/**
 * Le gestionnaire de scripts
 * @author pf
 *
 */

public class ScriptManager implements Service, Iterator<Script>
{
	private List<Script> scripts = new ArrayList<Script>();
	private Iterator<Script> iter;
	protected Log log;
	
	public ScriptManager(Log log)
	{
		this.log = log;
	}
	
	public void reinit()
	{
		iter = scripts.iterator();
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public Script next() {
		return iter.next();
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
}

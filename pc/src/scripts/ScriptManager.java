/*
Copyright (C) 2013-2017 Pierre-François Gimenez

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

import java.util.HashMap;
import java.util.Iterator;

import container.Service;
import container.dependances.CoreClass;
import pathfinding.astar.arcs.CercleArrivee;
import table.GameElementNames;
import utils.Log;

/**
 * Le gestionnaire de scripts
 * @author pf
 *
 */

public class ScriptManager implements Service, Iterator<Script>, CoreClass
{
	private HashMap<String, Script> scripts = new HashMap<String, Script>();
	private Iterator<Script> iter;
	protected Log log;
	
	public ScriptManager(Log log, CercleArrivee cercle)
	{
		this.log = log;
		for(GameElementNames n : GameElementNames.values())
			if(n.toString().startsWith("MINERAI_CRATERE"))
				scripts.put(n.toString(), new ScriptPetitCratere(log, cercle, n));
		scripts.put("MINERAI_GROS_CRATERE_GAUCHE", new ScriptGrosCratere(log, cercle, "MINERAI_GROS_CRATERE_GAUCHE"));
		scripts.put("MINERAI_GROS_CRATERE_DROITE", new ScriptGrosCratere(log, cercle, "MINERAI_GROS_CRATERE_DROITE"));
		scripts.put("DEPOSE", new ScriptDeposeMinerai(log, cercle));
		scripts.put("DEPOSE_SIMPLE", new ScriptDeposeMineraiSimple(log, cercle));
	}
	
	public HashMap<String, Script> getScripts()
	{
		return scripts;
	}
	
	public void reinit()
	{
		iter = scripts.values().iterator();
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

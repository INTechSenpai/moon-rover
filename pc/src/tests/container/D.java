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

package tests.container;

import config.Config;
import config.Configurable;
import config.DynamicConfigurable;
import container.Service;

/**
 * Classe utilisée pour le test de l'appel de config
 * @author pf
 *
 */
public class D implements Service, Configurable, DynamicConfigurable
{
	public boolean useConfigOk = false;
	public boolean updateConfigOk = false;
	
	public D()
	{}
	
	@Override
	public void updateConfig(Config config)
	{
		updateConfigOk = true;
	}

	@Override
	public void useConfig(Config config)
	{
		useConfigOk = true;
	}
}

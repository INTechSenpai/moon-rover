/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package remoteControl;

import java.io.Serializable;

/**
 * Commande de contrôle à distance
 * @author pf
 *
 */

public enum Commandes implements Serializable
{
	SPEED_UP(38), SPEED_DOWN(40), TURN_RIGHT(39), TURN_LEFT(37), STOP(32), RESET_WHEELS(10), SHUTDOWN(-1), PING(-1);
	
	private Commandes(int code)
	{
		this.code = code;
	}
	
	public int code;
	
	public void setCode(int code)
	{
		this.code = code;
	}
	
}

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

import serie.trame.Order.Type;

/**
 * Protocole série entre le bas niveau et la Java
 * @author pf
 *
 */

public class SerialProtocol {
	
	public enum OutOrder
	{
		/**
		 * Protocole Java vers bas niveau
		 */
		STOP(0x07, Type.LONG),
	
		SEND_ARC(0x2E, Type.SHORT),

		SET_MAX_SPEED(0, Type.SHORT),
		
		FOLLOW_TRAJECTORY(0, Type.LONG),
		
		START_STREAM_ALL(0x10, Type.LONG),
		WAIT_FOR_JUMPER(0x11, Type.LONG),
		START_MATCH_CHRONO(0x12, Type.LONG),
		ASK_COLOR(0x13, Type.SHORT),
	
		PING(0x5A, Type.SHORT);
		
		public final byte code;
		public final Type type;
		
		private OutOrder(int code, Type type)
		{
			this.type = type;
			this.code = (byte) code;
		}
	}
	
	public enum InOrder
	{
		/**
		 * Protocole bas niveau vers Java
		 */
	
		ROBOT_ARRIVE(0x00),
		ROBOT_BLOQUE(0x01),
		PLUS_DE_POINTS(0x02),
		
		// Couleur
		COULEUR_ROBOT_GAUCHE(0x01),
		COULEUR_ROBOT_DROITE(0x00),
		COULEUR_ROBOT_INCONNU(0x02);

		public final int codeInt;
		
		private InOrder(int code)
		{
			codeInt = code;
		}
	}
	
}

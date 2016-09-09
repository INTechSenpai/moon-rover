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

package serie.trame;

import serie.Ticket;
import serie.SerialProtocol.OutOrder;

public class Conversation
{
	private long deathDate; // date d'envoi + 2*timeout
	private long resendDate; // date d'envoi + timeout
	public Ticket ticket;
	public boolean libre = true;
	private OutgoingFrame firstFrame;
	private static int timeout;
	public OutOrder origine;
	
	/**
	 * Construction d'une conversation
	 * @param id
	 */
	public Conversation(int id)
	{
		firstFrame = new OutgoingFrame(id);
	}
	
	/**
	 * Mise à mort !
	 */
	public void setDeathDate()
	{
		deathDate = System.currentTimeMillis() + 2*timeout;
	}
	
	/**
	 * Mise à jour de la date de renvoi.
	 * A chaque fois que la trame est renvoyée, on remet à jour cette date.
	 */
	public void updateResendDate()
	{
		resendDate = System.currentTimeMillis() + timeout;
	}
	
	/**
	 * Faut-il renvoyer cette trame ?
	 * @return
	 */
	public boolean needResend()
	{
		return resendDate < System.currentTimeMillis();
	}

	/**
	 * Faut-il supprimer cette conversation ?
	 * @return
	 */
	public boolean needDeath()
	{
		return deathDate < System.currentTimeMillis();
	}
	
	/**
	 * Récupère le temps restant avant son réenvoi
	 * @return
	 */
	public int timeBeforeResend()
	{
		return (int) (resendDate - System.currentTimeMillis());
	}
	
	/**
	 * Récupère le temps restant avant sa mort
	 * @return
	 */
	public int timeBeforeDeath()
	{
		return (int) (deathDate - System.currentTimeMillis());
	}

	public static void setTimeout(int timeout_p)
	{
		timeout = timeout_p;
	}

	public OutgoingFrame getFirstTrame()
	{
		return firstFrame;
	}

	public void update(Order o)
	{
		origine = o.ordre;
		ticket = o.ticket;
		firstFrame.update(o);
		resendDate = System.currentTimeMillis() + 10000;
	}
}

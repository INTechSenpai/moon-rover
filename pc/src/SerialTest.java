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

import config.Config;
import config.ConfigInfo;
import container.Container;
import exceptions.ContainerException;
import graphic.Fenetre;
import serie.BufferOutgoingOrder;
import serie.Ticket;
import threads.ThreadShutdown;
import utils.Log;

/**
 * Test de série
 * @author pf
 *
 */

public class SerialTest
{
	protected static Container container;
	protected static Config config;
	protected static Log log;
	private static BufferOutgoingOrder data;
	
	public static void setUp() throws ContainerException, InterruptedException
	{
		container = new Container();
		config = container.getService(Config.class);
		log = container.getService(Log.class);
		synchronized(config)
		{
			config.set(ConfigInfo.MATCH_DEMARRE, true);
			config.set(ConfigInfo.DATE_DEBUT_MATCH, System.currentTimeMillis());
		}
        data = container.getService(BufferOutgoingOrder.class);
	}

	public static void tearDown() throws InterruptedException, ContainerException
	{
		container.getService(Fenetre.class).waitUntilExit();
		Runtime.getRuntime().removeShutdownHook(container.getService(ThreadShutdown.class));
		container.destructor(true);
	}
	
	public static void main(String[] args) throws ContainerException, InterruptedException
	{
		if(args.length == 0 || args.length > 1)
		{
			System.out.println("Tests possibles : ");
			System.out.println("	court");
			System.out.println("	long");
			System.out.println("	actionneur");
			return;
		}
		
		String param = args[0];
		if(param.equals("court"))
		{
			setUp();
			test_ask_color();
			tearDown();
		}
		else if(param.equals("long"))
		{
			setUp();
			test_stream();
			tearDown();			
		}
		else if(param.equals("actionneurs"))
		{
			setUp();
			test_actionneurs();
			tearDown();		
		}
			
	}

	public static void test_ask_color() throws InterruptedException
	{
		Ticket.State etat;
		do {
			Ticket t = data.demandeCouleur();
			synchronized(t)
			{
				if(t.isEmpty())
					t.wait();
			}
			etat = t.getAndClear();
		} while(etat != Ticket.State.OK);
	}
	
	public static void test_stream() throws InterruptedException
	{
		data.startStream();
		Thread.sleep(10000);
	}
	
	public static void test_actionneurs() throws InterruptedException
	{
		data.baisseFilet();
		Thread.sleep(2000);
		data.leveFilet();
	}
}

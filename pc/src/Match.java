import container.Container;
import container.Container.ErrorCode;
import exceptions.ContainerException;

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

/**
 * Code de match
 * 
 * @author pf
 *
 */

public class Match
{

	public static void main(String[] args)
	{
		Container container = null;
		try {
			container = new Container();
			Thread.sleep(5000);
			container.interruptWithCodeError(ErrorCode.DOUBLE_DESTRUCTOR);
			Thread.sleep(500);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				System.out.println("WOLOLOLO");
				System.exit(container.destructor().code);
			} catch (ContainerException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

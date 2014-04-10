package asservissement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import container.Container;
import exception.ConfigException;
import exception.ContainerException;
import exception.SerialException;
import exception.SerialManagerException;
import exception.ThreadException;
import robot.cartes.Deplacements;

public class SearchConst
{

	public static void main(String[] args)
	{
		float kp = 0, kd = 0;
		int pwm_max = 0;
		Container container;
		Deplacements deplacements = null;
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			container = new Container();
			deplacements = (Deplacements) container.getService("Deplacements");
		}
		catch (ContainerException | ThreadException | ConfigException
				| SerialManagerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("r ou t?");
		char asserv = 'a';
		try
		{
			asserv = (char) System.in.read();
		}
		catch (IOException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		float distance = -1000;
		float angle = - (float) Math.PI;

		while (true)
		{
			if(asserv == 't')
			{
				distance = - distance;
				try
				{
					String s;
					if (bufferRead.ready())
					{
						bufferRead.readLine();
					}
					System.out.println("kp ?");
					s = bufferRead.readLine();
					kp = Float.parseFloat(s);
					System.out.println(kp);
					System.out.println("kd ?");
					s = bufferRead.readLine();
					kd = Float.parseFloat(s);
					System.out.println(kd);
					System.out.println("pwm_max ?");
					s = bufferRead.readLine();
					pwm_max = Integer.parseInt(s);
					System.out.println(pwm_max);

				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try
				{
					deplacements.change_const_translation(kp, kd, pwm_max);
				}
				catch (SerialException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try
				{
					System.out.println(distance);
					deplacements.avancer(distance);
				}
				catch (SerialException e2)
				{
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
			else if(asserv == 'r')
			{
				angle = - angle;
				try
				{
					deplacements.tourner(angle);
				}
				catch (SerialException e2)
				{
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				try
				{
					System.out.println("kp ?");
					kp = (float) System.in.read();
					System.out.println("kd ?");
					kd = (float) System.in.read();
					System.out.println("pwm_max ?");
					pwm_max = (int) System.in.read();
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try
				{
					deplacements.change_const_rotation(kp, kd, pwm_max);
				}
				catch (SerialException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
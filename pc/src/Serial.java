import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Serial implements SerialPortEventListener
{
	SerialPort serialPort;
	
	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	BufferedReader input;
	/** The output stream to the port */
	OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */

	public void initialize(String port_name, int baudrate) throws NoSuchPortException
	{
		CommPortIdentifier portId = null;
		portId = CommPortIdentifier.getPortIdentifier(port_name);

		try
		{
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(baudrate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
	}
	
	public synchronized String communiquer(String message, int nb_lignes_reponse)
	{

		message+="\r";
		String inputLine = "";
		char acquittement = ' ';

		try
		{
			output.write(message.getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			while (acquittement != '_')
			{
				acquittement = input.readLine().charAt(0);
				if (acquittement != '_')
				{
					output.write(message.getBytes());
				}
			}
		}
		catch (Exception e)
		{
			return e.toString();
		}

		try
		{
			for (int i = 0 ; i < nb_lignes_reponse; i++)
			{
				inputLine += input.readLine();
			}
		}
		catch (Exception e)
		{
			return e.toString();
		}
		return inputLine;
	}
	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close()
	{
		if (serialPort != null)
		{
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent)
	{
	}
	
	public synchronized String ping()
	{
		try
		{
			//On vide le buffer de la serie cote PC
			output.flush();
			
			//On vide le buffer de la serie cote avr
			output.write("\r".getBytes());
			input.readLine();
			
			//ping
			output.write("?\r".getBytes());
			//evacuation de l'acquittement
			input.readLine();
			
			//recuperation de l'id de la carte
			return input.readLine();
			
		}
		catch (IOException e)
		{
			return e.toString();
		}
	}

}
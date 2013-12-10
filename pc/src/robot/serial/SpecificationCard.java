package robot.serial;

/**
 * 
 * @author pierre
 * N'est utilise que par le SerialManager afin de connaitre les attributs des cartes
 */
class SpecificationCard 
{
	String name;
	int id;
	int baudrate;
	SpecificationCard(String name, int id, int baudrate)
	{
		this.name = name;
		this.id = id;
		this.baudrate = baudrate;
	}
}

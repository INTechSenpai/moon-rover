/**
 * 
 * @author pierre
 * N'est utilise que par le SerialManager afin de connaitre les attributs des cartes
 */
public class SpecificationCard 
{
	public String name;
	public int id;
	public int baudrate;
	public SpecificationCard(String name, int id, int baudrate)
	{
		this.name = name;
		this.id = id;
		this.baudrate = baudrate;
	}
}

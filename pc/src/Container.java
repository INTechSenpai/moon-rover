import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Container {

	private static Container INSTANCE = null;
	private ArrayList<Service> instances_des_services = new ArrayList<Service>();
	
	private Container()
	{
	}
	
	public Service get_service(String nom)
	{
		for(Service instance : instances_des_services)
		{
			if(instance.getClass().getName() == nom)
				return instance;
		}

		int a = 1/0;
		return null;
	}

	/* Construction des services
	 * Les instanciations de services sont placées dans instances_des_services
	 */
	private void construire_services()
	{
		Log instance_log = Log.initialiser();
		if(instance_log == null)
			System.out.println("Erreur lors de l'instanciation de Log");
		instances_des_services.add(instance_log);

		Serie instance_serie = Serie.initialiser();
		if(instance_serie == null)
			System.out.println("Erreur lors de l'instanciation de Serie");
		instances_des_services.add(instance_serie);
}

	/*	Instanciation du container (singleton)
	 *  Il construit les services s'il n'a jamais été appelé
	 */
	public static Container initialiser()
	{
		if (INSTANCE == null)
		{
		 	INSTANCE = new Container();
		 	INSTANCE.construire_services();
		}
		return INSTANCE;
	}	
	
}

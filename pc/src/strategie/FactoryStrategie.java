package strategie;

import utils.Log;
import container.Service;
import factories.AbstractFactory;
import factories.FactoryProduct;

public class FactoryStrategie extends AbstractFactory implements Service {

	private Log log;
	
	public FactoryStrategie(Service log)
	{
		this.log = (Log) log;
	}
	
	public boolean LearnToClone(FactoryProduct model) {
		RegisterNewProduct(model);
		return true;
	}

	public boolean CanMake(Object toCheck) {
		return false;
	}

	public boolean CanMakeFromString(String className) {
		return IsRegistered(className);
	}

	public Object MakeFromString(String className) {
		Object potentialClone = Clone(className);
		if (potentialClone == null) 	
			log.warning("Erreur MakeFromString pour "+className, this);
		return potentialClone; 
	}

}

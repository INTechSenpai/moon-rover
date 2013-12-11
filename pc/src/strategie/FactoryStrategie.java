package strategie;

import factories.AbstractFactory;
import factories.FactoryProduct;

public class FactoryStrategie extends AbstractFactory {

	@Override
	public boolean LearnToClone(FactoryProduct model) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean CanMake(Object toCheck) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean CanMakeFromString(String className) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object MakeFromString(String className) {
		// TODO Auto-generated method stub
		return null;
	}

}

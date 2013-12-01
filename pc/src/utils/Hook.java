package utils;

import java.util.ArrayList;

/**
 * Classe-mère abstraite des hooks, utilisés pour la programmation évènementielle
 * @author pf
 *
 */

abstract public class Hook {

	private ArrayList<Callback> callbacks;

	public void ajouter_callback(Callback callback)
	{
		callbacks.add(callback);
	}
	
	public void declencher()
	{
		for(Callback callback : callbacks)
			callback.appeler();
	}
	
}


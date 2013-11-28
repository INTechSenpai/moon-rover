package Table;

import SmartMath.Orientation;
import SmartMath.Vec2;

public class Feu extends Elements_de_jeu {

	private int id ;
	private int height=0 ; //0=sol, chaque unité supplémentaire représente un feu en-dessous	
	private Orientation orientation = Orientation.UNDETERMINED ;
	private Couleur colour = Couleur.UNDETERMINED ;
	private boolean onFireplace = false ;	// what is ?
	private boolean taken  = false ;	// IDEM
	
	public Feu(int id, int height, Orientation orientation, Couleur couleur, Vec2 position)
	{
		this.id = id;
		this.height = height;
		this.orientation = orientation;
		this.colour = couleur;
		this.position = position;
	}

	public void pickFire()
	{
		this.taken = true;
	}
	
	public void ejectFire()
	{
		this.onFireplace = true;
	}
}

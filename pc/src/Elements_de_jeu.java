public class Elements_de_jeu {
	public Elements_de_jeu() {}
	protected Vector position ;
	protected static int nbStoredFires = 0;
	
	static public void create_fires(Feu[] vect)
	{
		for(int i=0; i <= 15 ; i++)
		{
			vect[i]= new Feu() ;
			(vect[i]).define_id(i);
		}
	}
	static public int get_nbStoredFires()
	{
		return nbStoredFires;
	}
	
}

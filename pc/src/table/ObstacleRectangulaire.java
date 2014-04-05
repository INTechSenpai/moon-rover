package table;

import smartMath.Vec2;

public class ObstacleRectangulaire extends Obstacle {

	protected int longueur;
	protected int largeur;
	
	public ObstacleRectangulaire(Vec2 position, int largeur, int longueur)
	{
		super(position);
		this.largeur = largeur; // en x
		this.longueur = longueur; // en y
	}

	public ObstacleRectangulaire clone()
	{
		return new ObstacleRectangulaire(position.clone(), largeur, longueur);
	}
	public String toString()
	{
		return "ObstacleRectangulaire";
	}
	public int getLongueur()
	{
		return this.longueur;
	}
	public int getLargeur()
	{
		return this.largeur;
	}
	
	/**
	 * Fourni la distance au carré d'un point à l'obstacle
	 * @param point
	 * @return
	 */
	public float SquaredDistance(Vec2 point)
	{
		// Si le point est à un des coins
		Vec2 coinBasGauche = position.PlusNewVector((new Vec2(0,(float)longueur)));
		Vec2 coinHautGauche = position.PlusNewVector((new Vec2(0,0)));
		Vec2 coinBasDroite = position.PlusNewVector((new Vec2((float)largeur,(float)longueur)));
		Vec2 coinHautDroite = position.PlusNewVector((new Vec2((float)largeur,0)));

		if(point.x < coinBasGauche.x && point.y < coinBasGauche.y)
			return point.SquaredDistance(coinBasGauche);
		
		else if(point.x < coinHautGauche.x && point.y > coinHautGauche.y)
			return point.SquaredDistance(coinHautGauche);
		
		else if(point.x > coinBasDroite.x && point.y < coinBasDroite.y)
			return point.SquaredDistance(coinBasDroite);

		else if(point.x > coinHautDroite.x && point.y > coinHautDroite.y)
			return point.SquaredDistance(coinHautDroite);

		// Si le point est sur un côté
		if(point.x > position.x)
			return (point.x - coinHautDroite.x)*(point.x - coinHautDroite.x);
		
		else if(point.x < position.x)
			return (point.x - coinBasGauche.x)*(point.x - coinBasGauche.x);

		else if(point.y > position.y)
			return (point.y - coinHautDroite.y)*(point.y - coinHautDroite.y);
		
		else if(point.y < position.y)
			return (point.y - coinBasGauche.y)*(point.y - coinBasGauche.y);
		
		// Cas impossible
		return 0f;
	}
	
}

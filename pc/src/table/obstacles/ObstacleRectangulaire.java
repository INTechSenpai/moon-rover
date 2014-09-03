package table.obstacles;

import smartMath.Vec2;

/**
 * Convention: la "position" d'un ObstacleRectangulaire est son coin supérieur gauche.
 * C'est une convention tout à fait absurde et idiote (le coin inférieur gauche aurait été bien mieux...)
 * @author pf
 *
 */
public class ObstacleRectangulaire extends Obstacle {

	protected int longueur_en_x;
	protected int longueur_en_y;
	
	public ObstacleRectangulaire(Vec2 position, int longueur_en_x, int longueur_en_y)
	{
		super(position);
		this.longueur_en_y = longueur_en_y;
		this.longueur_en_x = longueur_en_x;
	}

	public ObstacleRectangulaire clone()
	{
		return new ObstacleRectangulaire(position.clone(), longueur_en_x, longueur_en_y);
	}
	public String toString()
	{
		return "ObstacleRectangulaire";
	}
	
	/**
	 * En y
	 * @return
	 */
	public int getLongueur_en_y()
	{
		return this.longueur_en_y;
	}
	
	/**
	 * En x
	 * @return
	 */
	public int getLongueur_en_x()
	{
		return this.longueur_en_x;
	}
	
	public float distance(Vec2 point)
	{
		return (float) Math.sqrt(SquaredDistance(point));
	}
	
	/**
	 * Fourni la distance au carré d'un point à l'obstacle
	 * @param point
	 * @return
	 */
	public float SquaredDistance(Vec2 point)
	{
		// Si le point est à un des coins
		Vec2 coinBasGauche = position.PlusNewVector((new Vec2(0,-longueur_en_y)));
		Vec2 coinHautGauche = position.PlusNewVector((new Vec2(0,0)));
		Vec2 coinBasDroite = position.PlusNewVector((new Vec2(longueur_en_x,-longueur_en_y)));
		Vec2 coinHautDroite = position.PlusNewVector((new Vec2(longueur_en_x,0)));
		
		if(point.x < coinBasGauche.x && point.y < coinBasGauche.y)
			return point.SquaredDistance(coinBasGauche);
		
		else if(point.x < coinHautGauche.x && point.y > coinHautGauche.y)
			return point.SquaredDistance(coinHautGauche);
		
		else if(point.x > coinBasDroite.x && point.y < coinBasDroite.y)
			return point.SquaredDistance(coinBasDroite);

		else if(point.x > coinHautDroite.x && point.y > coinHautDroite.y)
			return point.SquaredDistance(coinHautDroite);

		// Si le point est sur un côté
		if(point.x > coinHautDroite.x)
			return (point.x - coinHautDroite.x)*(point.x - coinHautDroite.x);
		
		else if(point.x < coinBasGauche.x)
			return (point.x - coinBasGauche.x)*(point.x - coinBasGauche.x);

		else if(point.y > coinHautDroite.y)
			return (point.y - coinHautDroite.y)*(point.y - coinHautDroite.y);
		
		else if(point.y < coinBasGauche.y)
			return (point.y - coinBasGauche.y)*(point.y - coinBasGauche.y);

		// Sinon, on est dans l'obstacle
		return 0f;
	}
	
}

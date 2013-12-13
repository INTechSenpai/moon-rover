package table;

import smartMath.Vec2;

class ObstacleRectangulaire extends Obstacle {

	protected int longueur;
	protected int largeur;
	
	public ObstacleRectangulaire(Vec2 position, int largeur, int longueur)
	{
		super(position);
		this.largeur = largeur;
		this.longueur = longueur;
	}

	public ObstacleRectangulaire clone()
	{
		return new ObstacleRectangulaire(position.clone(), largeur, longueur);
	}
	
}

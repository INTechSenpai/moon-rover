package table;

import smartMath.Vec2;

public class ObstacleRectangulaire extends Obstacle {

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
	
}

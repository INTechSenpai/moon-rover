package table;

import smartMath.Vec2;

public abstract class Game_Element {//j'ai rendu ça public, c'est grave?
	protected Vec2 position;
	protected int p; //protected, pour quoi faire?
	public Game_Element(Vec2 position)
	{
		this.position = position;
		this.p = 1; //probab pas encore pris
	}
	
	public Vec2 getPosition()
	{
		return position;
	}
	
	public int getp()
	{
		return p; // p
	}
	
}

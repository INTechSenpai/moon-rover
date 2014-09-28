package table;

import smartMath.Vec2;

abstract class Game_Element
{
	protected Vec2 position;
	
	public Game_Element(Vec2 position)
	{
		this.position = position;
	}
	
	public Vec2 getPosition()
	{
		return position;
	}
}

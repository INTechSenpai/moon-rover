package table;

import smartMath.Vec2;

abstract class Game_Element {
	protected Vec2 position;
	protected float probaFaitParEnnemi = 0.05f;
	public Game_Element(Vec2 position)
	{
		this.position = position;
	}
	
	public Vec2 getPosition()
	{
		return position;
	}
	public float getProbaFaitParEnnemi()
	{
		return probaFaitParEnnemi;
	}
	public void setProbaFaitParEnnemi(float proba)
	{
		probaFaitParEnnemi = proba;
	}
	
	

}

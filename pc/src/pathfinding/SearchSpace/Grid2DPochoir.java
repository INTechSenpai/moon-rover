package pathfinding.SearchSpace;

import smartMath.Vec2;

public class Grid2DPochoir {

	public boolean[][] datas;
	int radius;
	
	public Grid2DPochoir(int radius)
	{
		this.radius = radius;
		datas = new boolean[radius*2][radius*2];
		Vec2 centre = new Vec2(radius, radius);
		for(int i = 0; i < radius*2; i++)
			for(int j = 0; j < radius*2; j++)
				datas[i][j] = (new Vec2(i, j)).distance(centre) > radius;
	}
	
	public String toString()
	{
		String s = new String();
		for(int i = 0; i < radius*2; i++)
		{
			for(int j = 0; j < radius*2; j++)
			{
				if(datas[i][j])
					s+=".";
				else
					s+="X";
			}
			s+="\n";
		}
		return s;
	}
	
}

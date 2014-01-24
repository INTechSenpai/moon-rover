package smartMath;

public class Matrn {
	
	private float[][] matrice;
	private int[] taille;
	public Matrn(float[][] t)
	{
		this.matrice = t;
		this.taille[0] = t.length ;
		this.taille[1] = t[0].length;
	}
	public Matrn(int n)
	{
		this.matrice = new float[n][n];
		this.taille[0] = n;
		this.taille[1] = n;
	}
	public Matrn(int n,int p)
	{
		this.matrice = new float[p][n];
		this.taille[0] = n;
		this.taille[1] = p;
	}
	void addition (Matrn A)
	{	
		for(int i = 0; i <= taille[0]; i++)
		{
			for(int j = 0; j <=taille[1]; j++)
			{
				 matrice[j][i]= matrice[j][i]+A.matrice[j][i];
			}
		}
	}
	void multiplier(Matrn A)
	{//multiplier this. avec A
		if( this.taille[1] == A.taille[1])
		{
		Matrn m = new Matrn(taille[0], A.taille[1]);
		for(int i = 0; i< taille[0]; i++)
		{
			for(int j = 0;j < A.taille[1];j++)
			{	
				m.matrice[j][i] = 0;
				for(int k = 0; k <this.taille[1];k++)
				{
					m.matrice[j][i] += this.matrice[k][i]*A.matrice[j][k];
				}
			}
		}
		this.matrice = m.matrice;
		}
	}
	void transpose()
	{
		if(this.taille[0] == this.taille[1])
		{
		for(int i = 0; i < this.taille[0]; i++)
		{
			for(int j = 0; j < i;j++)
			{
				this.matrice[j][i] = this.matrice[i][j];
			}
		}
		}
	}
	
	
	
	
}
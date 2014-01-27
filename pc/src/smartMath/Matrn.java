package smartMath;

import exception.MatriceException;

/**
 * Classe de calcul matriciel
 * @author pf
 * @author clément
 *
 */

public class Matrn {
	
	private float[][] matrice;
	private int[] taille;

	public Matrn(float[][] t)
	{
		matrice = t;
		taille = new int[2];
		taille[0] = t.length ;
		taille[1] = t[0].length;
	}
	
	public Matrn(int n)
	{
		matrice = new float[n][n];
		taille = new int[2];
		taille[0] = n;
		taille[1] = n;
	}
	
	/**
	 * @param p nombre de lignes
	 * @param n nombre de colonnes
	 */
	public Matrn(int p,int n)
	{
		matrice = new float[p][n];
		taille = new int[2];
		taille[0] = n;
		taille[1] = p;
	}
	
	/**
	 * Modifie le coeff en (i,j)
	 * @param coeff
	 * @param i la ligne
	 * @param j la colonne
	 */
	public void setCoeff(float coeff, int i, int j)
	{
		matrice[i][j] = coeff;
	}
	
	/**
	 * Récupère le coeff de (i,j)
	 * @param i la ligne
	 * @param j la colonne
	 */
	public float getCoeff(int i, int j)
	{
		return matrice[i][j];
	}
	
	public int getNbLignes()
	{
		return taille[1];
	}

	public int getNbColonnes()
	{
		return taille[0];
	}

	public void addition (Matrn A) throws MatriceException
	{	
		if(taille[0] != A.taille[0] || taille[1] != A.taille[1])
			throw new MatriceException();
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < taille[1]; j++)
				 matrice[j][i]= matrice[j][i]+A.matrice[j][i];
	}
	
	public void multiplier(Matrn A) throws MatriceException
	{//multiplier this. avec A
		if( this.taille[0] != A.taille[1])
			throw new MatriceException();
		Matrn m = new Matrn(taille[0], A.taille[1]);
		for(int i = 0; i< taille[0]; i++)
		{
			for(int j = 0;j < A.taille[1];j++)
			{
				m.matrice[i][j] = 0;
				for(int k = 0; k <this.taille[1];k++)
				{
					m.matrice[i][j] += this.matrice[i][k]*A.matrice[k][j];
				}
			}
		}
		this.matrice = m.matrice;
	}
	
	public void transpose() throws MatriceException
	{
		if(taille[0] != taille[1])
			throw new MatriceException();
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < i; j++)
			{
				float tmp = matrice[j][i];
				matrice[j][i] = matrice[i][j];
				matrice[i][j] = tmp;
			}
	}
	
	
	
	
}
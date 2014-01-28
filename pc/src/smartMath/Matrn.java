package smartMath;

import exception.MatriceException;

/**
 * Classe de calcul matriciel
 * @author pf
 * @author clément
 *
 */

public class Matrn {
	
	private double[][] matrice;
	private int[] taille;

	public Matrn(double[][] t)
	{
		matrice = t;
		taille = new int[2];
		taille[0] = t.length ;
		taille[1] = t[0].length;
	}
	
	public Matrn(int n)
	{
		matrice = new double[n][n];
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
		matrice = new double[p][n];
		taille = new int[2];
		taille[0] = n;
		taille[1] = p;
	}
	
	/**
	 * la matrice aura une taille (p,n) et tous les éléments vaudront valeur
	 * @param p
	 * @param n
	 * @param valeur : la valeur par défaut
	 */
	public Matrn(int p,int n, int valeur)
	{
		matrice = new double[p][n];
		taille = new int[2];
		taille[0] = n;
		taille[1] = p;
		for(int i = 0; i< taille[0]; i++)
		{
			for(int j = 0; j < taille[1]; j++)
			{
				setCoeff(valeur ,i, j);
			}
		}		
	}
	
	/**
	 * Modifie le coeff en (i,j)
	 * @param coeff
	 * @param i la ligne
	 * @param j la colonne
	 */
	public void setCoeff(double coeff, int i, int j)
	{
		matrice[i][j] = coeff;
	}
	
	/**
	 * Récupère le coeff de (i,j)
	 * @param i la ligne
	 * @param j la colonne
	 */
	public double getCoeff(int i, int j)
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
	public void soustraire (Matrn A) throws MatriceException
	{	
		if(taille[0] != A.taille[0] || taille[1] != A.taille[1])
			throw new MatriceException();
		for(int i = 0; i < taille[0]; i++)
			for(int j = 0; j < taille[1]; j++)
				 matrice[j][i]= matrice[j][i] - A.matrice[j][i];
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
				for(int k = 0; k < taille[1];k++)
				{
					m.matrice[i][j] += matrice[i][k]*A.matrice[k][j];
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
				double tmp = matrice[j][i];
				matrice[j][i] = matrice[i][j];
				matrice[i][j] = tmp;
			}
	}
	
	/*
	public Matrn transpose_vecteur() throws MatriceException
	{
		if(taille[1] != 1)
		{
			throw new MatriceException();
		}
		else
		{
			Matrn vect_tran = new Matrn(taille[1], taille[0]);
			for(int i = 0; i < taille[0]; i++)
			{
				vect_tran.matrice[i][0] = matrice[0][i];
			}
			return vect_tran;
	}
	*/
		
	}
	

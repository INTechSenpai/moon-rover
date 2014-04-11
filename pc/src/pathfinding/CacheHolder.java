/**
 * 
 */
package pathfinding;

import java.io.Serializable;

/**
 * Classe contenant l'ensemble des données brutes du cache
 * C'est elle qui sera très grosse en mémoire.
 * 
 * Le cache contient toutes les distances entre tout les couples de points de la table avec obstacles fixes
 * On stocke les distances de mannathan, en int.
 * Il y a besoin de 2 parmètres pour le point de départ, et 2 paramètres pour le point d'arrivée.
 * Du coup on bosses sur une classe qui sera grosso modo un gros tableau d'int à 4 dimentions.
 * @author Marsu, pf
 *
 */

class CacheHolder implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int reduction;
	private int mm_per_unit;
	// 16 bits signés, soit une distance maximale de 32000 environ, ce qui est largement suffisant
	public byte[][][][] data;
	
	public CacheHolder(int sizeX, int sizeY, int reduction, int mm_per_unit)
	{
		this.mm_per_unit = mm_per_unit;
		this.reduction = reduction;
		data = new byte[sizeX][sizeY][sizeX][sizeY];
	}
	
	public int getReduction()
	{
		return reduction;
	}
	
	public int getMm_per_unit()
	{
		return mm_per_unit;
	}
	
	public static byte int2byte(int b)
	{
		return (byte)(b-128);
	}
	
	public static int byte2int(byte b)
	{
		return (int)(b+128);
	}
	
}

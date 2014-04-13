/**
 * 
 */
package pathfinding;

import java.io.Serializable;

import exception.PathfindingException;
import smartMath.Vec2;

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
	/* 8 bits non signés (grâce à byte2int et int2byte), avec une précision par défaut de 15 mm par unité, soit une distance maximale 254*15 = 3810mm
	 * 255 est la valeur infinie pour chemin impossible
	 */
	private byte[][][][] data;
	private int table_x;
	
	public CacheHolder(int sizeX, int sizeY, int reduction, int mm_per_unit, int table_x)
	{
		this.mm_per_unit = mm_per_unit;
		this.reduction = reduction;
		this.table_x = table_x;
		data = new byte[sizeX][sizeY][sizeX][sizeY];
	}

	public int getDistance(Vec2 depart, Vec2 arrivee) throws PathfindingException
	{
		int distance = byte2int(data[(depart.x+table_x/2)/reduction][depart.y/reduction][(arrivee.x+table_x/2)/reduction][arrivee.y/reduction]);
		if(distance == 255)
			throw new PathfindingException();
		else
			return distance * mm_per_unit;
	}
	
	public void setDistance(Vec2 depart, Vec2 arrivee, int distance)
	{
		data[(depart.x+table_x/2)/reduction][depart.y/reduction][(arrivee.x+table_x/2)/reduction][arrivee.y/reduction] = int2byte(distance/mm_per_unit);
	}

	public void setImpossible(Vec2 depart, Vec2 arrivee)
	{
		data[(depart.x+table_x/2)/reduction][depart.y/reduction][(arrivee.x+table_x/2)/reduction][arrivee.y/reduction] = int2byte(255);
	}

	private byte int2byte(int b)
	{
		return (byte)(b-128);
	}
	
	private int byte2int(byte b)
	{
		return (int)(b+128);
	}
	
}

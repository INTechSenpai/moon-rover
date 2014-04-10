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

// TODO: passer en visibilité default
class CacheHolder implements Serializable
{
	private static final long serialVersionUID = 1L;

	public int[][][][] data;
	
	public CacheHolder(int sizeX, int sizeY)
	{
		data = new int[sizeX][sizeY][sizeX][sizeY];
	}
	
}

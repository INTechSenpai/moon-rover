/**
 * 
 */
package pathfinding.cache;
import java.util.ArrayList;

/**
 * Classe contenant l'ensemble des données brutes du cache
 * C'est elle qui sera très grosse en mémoire.
 * 
 * Le cache contient toutes les distances entre tout les couples de points de la table avec obstacles fixes
 * On stocke les distances de mannathan, en int.
 * Il y a besoin de 2 parmètres pour le point de départ, et 2 paramètres pour le point d'arrivée.
 * Du coup on bosses sur une classe qui sera grosso modo un gros tableau d'int à 4 dimentions.
 * @author Marsu
 *
 */
public class CacheHolder 
{
	public 	ArrayList						// Depart.x
			<
				ArrayList					// Depart.y
				<
					ArrayList				// Arrivee.x
					<
						ArrayList			// Arrivee.y
						<
							Integer
						>
					>
				>
			> data;
}

/**
 * 
 */
package strategie.arbre;

/**
 * Classe formalisant le concept d'une branche (ou d'une sous-branche) de l'arbre des possibles.
 * En termes de graphes, l'arbre est défini commme suit :
 * 		chaque sommet de l'arbre est une table de jeu dans un certain état.
 * 		chaque arc de l'arbre est une action qui modifie l'état d'une table
 * Une branche est un arc avec une note suivit d'un sommet duquel partent à leurs tours une mutitude de sous-branches.
 * Une branche possède une note, égale a une combinaison des notes de ses sous-branches.
 * 		(ie branche = triplet note-action-table)
 * Le role de Stratégie est de faire calculer les notes toutes les branches de l'arbres en les considérants jusqu'à une certaine profondeur.
 * 
 * @author karton
 *
 */
public class Branche 
{
	private long date;			// date d'évaluation de cette branche
	private int duree_totale;	// temps d'exécution de la partie de l'arbre en amont de cette branche
	private int profondeur;		// profondeur a laquelle cettre branche sera explorée ( taille de l'arbre en aval de cette branche)
	private int id_robot;		// robot qui parcours la branche (?)
	
	public Table 
	
	/** Constructeur de la branche
	 * @param date
	 * @param duree_totale
	 * @param profondeur
	 * @param id_robot
	 */
	public Branche(long date, int duree_totale, int profondeur, int id_robot) 
	{
		this.date = date;
		this.duree_totale = duree_totale;
		this.profondeur = profondeur;
		this.id_robot = id_robot;
	}
	
	
	
	
	
}

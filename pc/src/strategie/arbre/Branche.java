/**
 * 
 */
package strategie.arbre;

import java.util.ArrayList;

import scripts.Script;
import table.Table;

/**
 * Classe formalisant le concept d'une branche (ou d'une sous-branche) de l'arbre des possibles.
 * En termes de graphes, l'arbre est défini commme suit :
 * 		chaque sommet de l'arbre est une table de jeu dans un certain état.
 * 		chaque arc de l'arbre est une action qui modifie l'état d'une table
 * Une branche est un arc avec une note suivit d'un sommet duquel partent à leurs tours une mutitude de sous-branches.
 * Une branche possède une note, égale a une combinaison des notes de ses sous-branches.
 * 		(ie branche = triplet note-action-table)
 * Le role de Stratégie est de faire calculer les notes de toutes les branches de l'arbres en les considérants jusqu'à une certaine profondeur.
 * 
 * @author karton
 *
 */
public class Branche 
{
	
	
	private long date;			// date d'évaluation de cette branche
	private int duree_totale;	// temps d'exécution de la partie de l'arbre en amont de cette branche
	private int profondeur;		// profondeur a laquelle cettre branche sera explorée (taille de l'arbre en aval de cette branche)
	private int id_robot;		// robot qui parcours la branche (?)
	
	// note attribuée a cette branche
	public float note;
	
	// Action a executer entre le sommet juste avant cette branche et l'état final
	public Script script;
	public int metaversion;
	
	// Table résultant de la première action de cette branche
	public Table etatFinal;
	
	// Sous branches contenant toutes les autres actions possibles a partir de l'état final
	public ArrayList<Branche> sousBranches;
	
	
	
	
	/** Constructeur de la branche
	 * @param date
	 * @param duree_totale
	 * @param profondeur
	 * @param id_robot
	 * @param script
	 * @param metaversion
	 */
	public Branche(long date, int duree_totale, int profondeur, int id_robot, Script script, int metaversion )
	{
		
		this.date = date;
		this.duree_totale = duree_totale;
		this.profondeur = profondeur;
		this.id_robot = id_robot;
		this.script = script;
		this.metaversion = metaversion;
	}
	
	/*
	
	Itterative DFS:
	
	Put origin in S
	
	while S.size
		v = S.first
		S.pop
		put all childs of v at the beginning of S
		
		
	REcursive DFS :
	
	For all origin chlidren :
		DFS(children[i])
	
	
	*/
	
	/* Méthode qui calcule la note de cette branche en calculant celles de ses sous branches, puis en combinant leur notes
	 * C'est là qu'est logé le DFS
	 * 
	 */
	public void evaluate()
	{
		// Si la profondeur est non nulle (ie s'il y a des sous-branches), on explore toutes les sous-branches
		if (profondeur > 0)
		{
			// Soucis : l'algo qu'on utilise est pas stricto sensu un DFS, car un traitement est effectué au débouclage (le calcul de la note)
			// a voir si utiliser un itterative DFS est plus performant ou non.
			
		}
		
		// Finalement, donne une note a toute cette branche
		computeNote();
	}
	
	/** Méthode qui prend les notes de chaque sous branche (en supposant qu'elles sont déjà calculés et qu'il n'y a plus qu'a
	 * les considérer) et les mélange popur produire la note de cette branche
	 * Il n'y a que cette méthode qui doit modifier this.note
	 */
	public void computeNote()
	{
		if(profondeur == 0)
		{
			// note = script.note
		}
		else
		{
			// TODO : mixer les notes des sous-branches
			
			// pour l'instant, un simple max ira bien :
			note = -1;	// Valeur incorecte pour forcer la réattribution
			for (int i = 0; i< sousBranches.size(); ++i)
				if(sousBranches.get(i).note > this.note)
					note = sousBranches.get(i).note; 
		}
		
	}

	
	
	
}

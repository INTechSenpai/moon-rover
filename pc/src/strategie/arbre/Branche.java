/**
 * 
 */
package strategie.arbre;

import java.util.ArrayList;
import java.util.Stack;

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

@SuppressWarnings("unused")
public class Branche 
{
	
	
	private long date;			// date d'évaluation de cette branche
	private int duree_totale;	// temps d'exécution de la partie de l'arbre en amont de cette branche
	private int profondeur;		// profondeur a laquelle cettre branche sera explorée (taille de l'arbre en aval de cette branche)
	private int id_robot;		// robot qui parcours la branche (?)
	
	// note attribuée a cette branche
	public float note;
	public boolean isNoteComputed;
	
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
		isNoteComputed = false;
	}
	
	
	/* Méthode qui calcule la note de cette branche en calculant celles de ses sous branches, puis en combinant leur notes
	 * C'est là qu'est logé le DFS
	 * 
	 * Note : cette méthode doit elle être dans Branche ou dans Stratégie ?
	 */
	public void evaluate()
	{
		/*
		 * 	Algorithme : Itterative Modified DFS
		 *  ( la différence entre un vrai DFS et l'algo qu'on utilise ici est que la branche parente
		 *    doit être évalué uniquement une fois que tout ses enfants ont étés évalués. Dans un
		 *    DFS normal, le parent est évalué d'abord, et ses enfants ensuite )
		 *    
		 * Psuedocode :
		 * 
		 * soit P une pile
		 * mettre la racine au dessus de P
		 * 
		 * tant que P est non vide
		 * 		v = l'élément du dessus de P
		 * 		si v a des enfants, et qu'ils sont non notés
		 * 			mettre tout les enfants de v au dessus de P
		 * 		sinon
		 * 			calculer la note de v
		 * 			enlever v de P 
		 * 
		 * 
		 */
		
		Stack<Branche> scope = new Stack<Branche>();
		scope.add(this);
		
		while (scope.size() != 0)
		{
			Branche current = scope.lastElement();
			// Condition d'ajout des sous-branches : ne pas dépasser le profondeur max, et ne pas les ajouter 2 fois.
			if ( current.profondeur > 0 && (current.sousBranches.size() == 0) )
			{
				// TODO : mettre les sous-branches au fur et a mesure dans la branche courante
				scope.addAll(current.sousBranches);
			}
			else
			{
				current.computeNote();
				scope.pop();
			}
		}
		
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
		
		// marque la note comme calculée 
		isNoteComputed = true;
		
	}

	
	
	
}

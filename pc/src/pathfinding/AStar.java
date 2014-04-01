/**
 *  Classe encapsulant le calcul par A* d'un chemin sur un graphe quelconque
 *
 * @author Marsya
 */
package pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;

import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.IntPair;

// Le test se trouve dans un test unitaire
public class AStar
{
	private boolean 	processed,	// le chemin a-t-il �t� calcul� ou pas encore ?
						isValid;	// indique si le chamin calcul� est valide ou non ( auquel cas une erreur a emp�ch� son calcul)
	private Grid2DSpace espace;		// espace de travail
	private ArrayList<IntPair> chemin;	// r�ceptacle du calcul
	
	private Set<IntPair> 	closedset,	// The set of nodes already evaluated.
							openset;	 // The set of tentative nodes to be evaluated
	private Map<IntPair, IntPair>	came_from; // The map of navigated nodes.
	private Map<IntPair, Integer>	g_score,
									f_score;
	
	private IntPair 	depart, 
						arrivee;
	


	public AStar( Grid2DSpace espaceVoulu, IntPair departVoulu, IntPair arriveeVoulue)
	{
		// Construit la demande d'un futur calcul
		processed = false;
		chemin = new ArrayList<IntPair>();
		
		depart = new IntPair(departVoulu.x, departVoulu.y);
		arrivee = new IntPair(arriveeVoulue.x, arriveeVoulue.y);
		
		espace = espaceVoulu.makeCopy();
		
		closedset = new LinkedHashSet<IntPair>();
		openset = new LinkedHashSet<IntPair>();
		
		came_from = new HashMap<IntPair, IntPair>();
		g_score = new HashMap<IntPair, Integer>();
		f_score = new HashMap<IntPair, Integer>();
		
	}
	/**
	 * From wikipedia :
	 * 
	 * 
function A*(start,goal)
    closedset := the empty set    // The set of nodes already evaluated.
    openset := {start}    // The set of tentative nodes to be evaluated, initially containing the start node
    came_from := the empty map    // The map of navigated nodes.
 
    g_score[start] := 0    // Cost from start along best known path.
    // Estimated total cost from start to goal through y.
    f_score[start] := g_score[start] + heuristic_cost_estimate(start, goal)
 
    while openset is not empty
        current := the node in openset having the lowest f_score[] value
        if current = goal
            return reconstruct_path(came_from, goal)
 
        remove current from openset
        add current to closedset
        for each neighbor in neighbor_nodes(current)
            if neighbor in closedset
                continue
            tentative_g_score := g_score[current] + dist_between(current,neighbor)
 
            if neighbor not in openset or tentative_g_score < g_score[neighbor] 
                came_from[neighbor] := current
                g_score[neighbor] := tentative_g_score
                f_score[neighbor] := g_score[neighbor] + heuristic_cost_estimate(neighbor, goal)
                if neighbor not in openset
                    add neighbor to openset
 
    return failure
 
function reconstruct_path(came_from, current_node)
    if current_node in came_from
        p := reconstruct_path(came_from, came_from[current_node])
        return (p + current_node)
    else
        return current_node
        
        
	 * 
	 */
	public void process()
	{
		closedset.clear();		// The set of nodes already evaluated.
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
		came_from.clear(); 		// The map of navigated nodes.

		
		g_score.put(depart, 0);	// Cost from start along best known path.
	    // Estimated total cost from start to goal through y.
	    f_score.put(depart, g_score.get(depart) + fastGridDistance(depart, arrivee));
	    
	    IntPair current = 	new IntPair(0,0),
	    		temp =		new IntPair(0,0);			
	    Iterator<IntPair> NodeIterator = openset.iterator();
	    int tentative_g_score = 0;
	    
	    while (openset.size() != 0)
	    {
	    	// current is affected by the node in openset having the lowest f_score[] value
	    	NodeIterator = openset.iterator();
	    	current.set(NodeIterator.next());
	    	while(NodeIterator.hasNext())
	    	{
	    		temp = NodeIterator.next();
	    		if (f_score.get(temp) < f_score.get(current))
	    			current  = temp;
	    	}
	    	
	    	if (current.x == arrivee.x && current.y == arrivee.y)
	    	{
	    		
	    		chemin.clear();
    			chemin.add( new IntPair(arrivee.x,arrivee.y));
    			if (arrivee.x != depart.x && arrivee.y != depart.y && came_from.get(current) != null)
    			{
		    		temp = came_from.get(current);	    		
		    		while ( temp.x != depart.x || temp.y != depart.y )
		    		{
		    			chemin.add(0, new IntPair(temp.x,temp.y)); // insert le point d'avant au debut du parcours
		    			current  = temp;
		    			temp = came_from.get(temp);
		    			
		    			if(temp == null)	// null pointer exeption
		    			{
		    				System.out.println("Depart : " + depart.x + " - " + depart.y);
		    				System.out.println("arrivee : " + arrivee.x + " - " + arrivee.y);
		    				System.out.println("current : " + current.x + " - " + current.y);
		    				
		    				//System.out.println(espace.stringForm());

		    				String out = "";
		    				Integer i = 1;
		    				for (int  j = 0; j < espace.getSizeX(); ++j)
		    				{
		    					for (int  k = espace.getSizeY() - 1; k >= 0; --k)
		    					{
		    						IntPair pos = new IntPair(j,k);
		    						if (depart.x ==j && depart.y ==k)
		    							out += 'D';
		    						else if (arrivee.x ==j && arrivee.y ==k)
		    							out += 'A';
		    						else if (chemin.contains(pos))
		    						{
		    							out += i.toString();
		    							i++;
		    						}
		    						else if(espace.canCross(j, k))
		    							out += '.';
		    						else
		    							out += 'X';	
		    					}
		    					
		    					out +='\n';
		    				}
		    				System.out.println(out);
		    				
		    			}
		    		}
    			}
    			chemin.add( new IntPair(depart.x,depart.y));
	    		
	    		processFinalisationWithSucess();
	    		return;	//  reconstruct path
	    	}
	    	
	    	openset.remove(current);
	    	closedset.add(new IntPair(current.x, current.y));
	    	
	    	for(int i = 1; i <= 4; ++i)
	    	{
	    		temp = neighbor_nodes(current, i);
	    		
	    		if (closedset.contains(temp) == false)
	    		{
	    			tentative_g_score = g_score.get(current) + 1;	// 1 �tant la distance entre le point courant et son voisin
	    			
	    			if(openset.contains(temp) == false || tentative_g_score < g_score.get(temp))
	    			{
	    				came_from.put(temp.makeCopy(), current.makeCopy());
	    				g_score.put(temp, tentative_g_score);
	    				f_score.put(temp, tentative_g_score + 5 * fastGridDistance(temp, arrivee));
	    				if(openset.contains(temp) == false)
	    					openset.add(new IntPair(temp.x, temp.y));
	    				
	    				
	    			}
	    		}	
	    	}
	    	
	    	
	    	
	    }// while
	    
	    processFinalisationWithError();
	    return;
	}	// process
	
	private void processFinalisationWithSucess()
	{
		isValid = true;
		processFinalisation();
	}
	
	private void processFinalisationWithError()
	{
		isValid = false;
		processFinalisation();
	}
	
	private void processFinalisation()
	{
		processed = true;
	}
	
	// =====================================  Utilitaires ===============================
	
	// Calcule rapidement la distance entre A et B en nombre de cases a traverser. Pas besoin d'op�rations en
	// virgule flottante ni de multiplication
	public int fastGridDistance( IntPair A, IntPair B)
	{
		return Math.abs(A.x - B.x) + Math.abs(A.y - B.y); 
	}
	
	// donne les voisins d'un node par index : 1, droite, 2, haut, 3, gauche, 4, bas
	public IntPair neighbor_nodes(IntPair center, int index)
	{
		if( index == 1 && espace.canCross(center.x + 1, center.y))
			return new IntPair(center.x + 1, center.y);
		if( index == 2 && espace.canCross(center.x, center.y + 1))
			return new IntPair(center.x, center.y + 1);
		if( index == 3 && espace.canCross(center.x - 1, center.y))
			return new IntPair(center.x - 1, center.y);
		if( index == 4 && espace.canCross(center.x, center.y - 1))
			return new IntPair(center.x, center.y - 1);
		return center;
	}
	
	// ======================================= Getters / Setters ========================================
	
	/**
	 * @return the chemin
	 */
	public ArrayList<IntPair> getChemin() 
	{
		return chemin;
	}
	
	public Grid2DSpace getEspace() {
		return espace;
	}
	public void setEspace(Grid2DSpace espace) {
		this.espace = espace;
	}
	public IntPair getDepart() {
		return depart;
	}
	public void setDepart(IntPair depart) {
		this.depart = depart;
	}
	public IntPair getArrivee() {
		return arrivee;
	}
	public void setArrivee(IntPair arrivee) {
		this.arrivee = arrivee;
	}
	/**
	 * @return the processed
	 */
	public boolean isProcessed()
	{
		return processed;
	}

	/**
	 * @param processed the processed to set
	 */
	public void setProcessed(boolean processed)
	{
		this.processed = processed;
	}
	public boolean isValid() {
		return isValid;
	}
}

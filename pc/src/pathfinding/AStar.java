/**
 *  Classe encapsulant le calcul par A* d'un chemin sur un graphe quelconque
 *
 * @author Marsya, PF
 */
package pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;

import exception.PathfindingException;
import pathfinding.SearchSpace.Grid2DSpace;
import smartMath.Vec2;

// Le test se trouve dans un test unitaire
class AStar
{
	public Grid2DSpace espace;		// espace de travail
	private ArrayList<Vec2> chemin;	// r�ceptacle du calcul
	
	private Set<Vec2> 	closedset,	// The set of nodes already evaluated.
							openset;	 // The set of tentative nodes to be evaluated
	private Map<Vec2, Vec2>	came_from; // The map of navigated nodes.
	private Map<Vec2, Integer>	g_score,
									f_score;
	
	/* Combien de millimètre aurait-on eu le temps de faire pendant cette rotation?
	 * Rotation moyenne: 1/6 de tour (PI/3)
	 * Vitesse angulaire entre script: 5.15 r/s
	 * Vitesse translatoire entre script: 725 mm/s
	 * La réponse est donc 150mm.
	 */
	// TODO intégrer coefficient_rotation
//	private int coefficient_rotation = 150;
	
	public AStar(Grid2DSpace espaceVoulu)
	{
		espace = espaceVoulu;

		chemin = new ArrayList<Vec2>();
		
		closedset = new LinkedHashSet<Vec2>();
		openset = new LinkedHashSet<Vec2>();
		
		came_from = new HashMap<Vec2, Vec2>();
		g_score = new HashMap<Vec2, Integer>();
		f_score = new HashMap<Vec2, Integer>();		
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
	public ArrayList<Vec2> process(Vec2 depart, Vec2 arrivee) throws PathfindingException
	{
		chemin.clear();

		// Si le départ ou l'arrivée est dans un obstacle, on lève une exception
		if(!espace.canCross(arrivee) || !espace.canCross(depart))
			throw new PathfindingException();
		else if(espace.canCrossLine(depart, arrivee))
		{
			chemin.add(arrivee);
			return chemin;
		}

		closedset.clear();		// The set of nodes already evaluated.
		openset.clear();
		openset.add(depart);	// The set of tentative nodes to be evaluated, initially containing the start node
		came_from.clear(); 		// The map of navigated nodes.
		g_score.clear();
		f_score.clear();
		
		g_score.put(depart, 0);	// Cost from start along best known path.
                           	    // Estimated total cost from start to goal through y.
	    f_score.put(depart, g_score.get(depart) + depart.manhattan_distance(arrivee));
	    
	    Vec2 current = 	new Vec2(0,0),
	    		temp =	new Vec2(0,0);			
	    Iterator<Vec2> NodeIterator = openset.iterator();
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
    			chemin.add( new Vec2(arrivee.x,arrivee.y));
    			if (arrivee.x != depart.x && arrivee.y != depart.y && came_from.get(current) != null)
    			{
		    		temp = came_from.get(current);
		    		while ( temp.x != depart.x || temp.y != depart.y )
		    		{
		    			chemin.add(0, new Vec2(temp.x,temp.y)); // insert le point d'avant au debut du parcours
		    			current  = temp;
		    			temp = came_from.get(temp);
		    			
		    		}
    			}
    			// Le chemin final ne doit pas contenir le point de départ (plus pratique pour Script et pour le HPA*)
//    			chemin.add(0, new Vec2(depart.x,depart.y));
	    		return chemin;	//  reconstruct path
	    	}
	    	
	    	openset.remove(current);
	    	closedset.add(new Vec2(current.x, current.y));
	    	
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
	    				// TODO: vérifier que 5 est bien le meilleur coefficient
	    				f_score.put(temp, tentative_g_score + 5 * temp.manhattan_distance(arrivee));
	    				if(openset.contains(temp) == false)
	    					openset.add(new Vec2(temp.x, temp.y));
	    				
	    				
	    			}
	    		}	
	    	}
	    		    	
	    }// while
	    throw new PathfindingException();
	}	// process

	
	// =====================================  Utilitaires ===============================
		
	// donne les voisins d'un node par index : 1, droite, 2, haut, 3, gauche, 4, bas
	private Vec2 neighbor_nodes(Vec2 center, int index)
	{
		if( index == 1 && espace.canCross(center.x + 1, center.y))
			return new Vec2(center.x + 1, center.y);
		else if( index == 2 && espace.canCross(center.x, center.y + 1))
			return new Vec2(center.x, center.y + 1);
		else if( index == 3 && espace.canCross(center.x - 1, center.y))
			return new Vec2(center.x - 1, center.y);
		else if( index == 4 && espace.canCross(center.x, center.y - 1))
			return new Vec2(center.x, center.y - 1);
		return center;
	}
	
}

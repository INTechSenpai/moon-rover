/*
Copyright (C) 2016 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package pathfinding.astar.arcs;

import pathfinding.DirectionStrategy;
import pathfinding.SensFinal;
import pathfinding.astar.AStarCourbeNode;
import pathfinding.dstarlite.DStarLite;
import robot.Cinematique;
import robot.Speed;
import table.GameElementNames;
import table.Table;
import table.EtatElement;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Service;
import graphic.PrintBuffer;
import obstacles.types.ObstacleArcCourbe;
import obstacles.types.ObstaclesFixes;
import utils.Log;

/**
 * Réalise des calculs pour l'A* courbe.
 * @author pf
 *
 */

public class ArcManager implements Service, Configurable
{
	protected Log log;
	private ClothoidesComputer clotho;
	protected BezierComputer bezier;
	private PrintBuffer buffer;
	private Table table;
	private AStarCourbeNode current;
	private DStarLite dstarlite;
	private double courbureMax;
	private boolean printObs;
	
	private DirectionStrategy directionstrategyactuelle;
	private SensFinal sens;
	private Cinematique arrivee = new Cinematique();
	private List<VitesseCourbure> listeVitesse = Arrays.asList(VitesseCourbure.values());
	private ListIterator<VitesseCourbure> iterator = listeVitesse.listIterator();
	
	public ArcManager(Log log, ClothoidesComputer clotho, Table table, PrintBuffer buffer, DStarLite dstarlite, BezierComputer bezier)
	{
		this.bezier = bezier;
		this.table = table;
		this.log = log;
		this.clotho = clotho;
		this.buffer = buffer;
		this.dstarlite = dstarlite;
	}

	private ObstacleArcCourbe obs = new ObstacleArcCourbe();
	
	/**
	 * Retourne faux si un obstacle est sur la route
	 * @param node
	 * @return
	 * @throws FinMatchException
	 */
	public boolean isReachable(AStarCourbeNode node, boolean shoot)
	{
		// le tout premier nœud n'a pas de parent
		if(node.parent == null)
			return true;

		/**
		 * On agrège les obstacles en un seul pour simplifier l'écriture des calculs
		 */
		obs.ombresRobot.clear();
		for(int i = 0; i < node.getArc().getNbPoints(); i++)
			obs.ombresRobot.add(node.getArc().getPoint(i).obstacle);
		
		if(printObs)
			buffer.addSupprimable(obs);
		
		// Collision avec un obstacle fixe?
    	for(ObstaclesFixes o: ObstaclesFixes.values())
    		if(o.getObstacle().isColliding(obs))
    		{
//				log.debug("Collision avec "+o);
    			return false;
    		}

    	// Collision avec un obstacle de proximité ?
    	node.state.iterator.reinit();
    	while(node.state.iterator.hasNext())
           	if(node.state.iterator.next().isColliding(obs))
    		{
//				log.debug("Collision avec un obstacle de proximité.");
    			return false;
    		}
    	
    	// On vérifie si on collisionne un élément de jeu (sauf si on shoot)
    	if(!shoot)
			for(GameElementNames g : GameElementNames.values())
				if(table.isDone(g) == EtatElement.INDEMNE && g.obstacle.isColliding(obs))
	    		{
	//    			log.debug("Collision avec "+g);
	    			return false;
	    		}

    	return true;
	}
	
	/**
	 * Renvoie la distance entre deux points. Et par distance, j'entends "durée".
	 * Heureusement, les longueurs des arcs de clothoïdes qu'on considère sont égales.
	 * Ne reste plus qu'à prendre en compte la vitesse, qui dépend de la courbure.
	 * Il faut exécuter tout ce qui se passe pendant ce trajet
	 */
	public double distanceTo(AStarCourbeNode node)
	{
		node.state.robot.suitArcCourbe(node.getArc());
		return node.getArc().getDuree(node.state.robot.getCinematique());
	}

	/**
	 * Fournit le prochain successeur. On suppose qu'il existe
	 * @param successeur
	 */
    public boolean next(AStarCourbeNode successeur, Speed vitesseMax)
    {
    	VitesseCourbure v = iterator.next();

		current.state.copyAStarCourbe(successeur.state);

		if(v == VitesseCourbure.BEZIER_QUAD)
		{
			ArcCourbeDynamique tmp;
			tmp = bezier.interpolationQuadratique(
					current.state.robot.getCinematique(),
					arrivee,
					vitesseMax);
			if(tmp == null)
				return false;

			successeur.cameFromArcDynamique = tmp;
		}
		
		else if(v == VitesseCourbure.BEZIER_CUBIQUE) // TODO vérifier qu'il y a une heuristique qui veut arriver quelque part
		{
			ArcCourbeDynamique tmp;
			tmp = bezier.interpolationCubique(
					current.state.robot.getCinematique(),
					arrivee,
					vitesseMax);
			if(tmp == null)
				return false;

			successeur.cameFromArcDynamique = tmp;
			return false; // TODO
		}
		
		/**
		 * Si on veut ramener le volant au milieu
		 */
		else if(v.ramene)
		{
			ArcCourbeDynamique tmp = clotho.getTrajectoireRamene(
					successeur.state.robot.getCinematique(),
					v,
					vitesseMax);
			if(tmp.getNbPoints() == 0)
				return false;
			successeur.cameFromArcDynamique = tmp;
		}

		/**
		 * Si on veut faire un demi-tour
		 */
		else if(v.demitour)
		{
			successeur.cameFromArcDynamique = clotho.getTrajectoireDemiTour(
					successeur.state.robot.getCinematique(),
					v,
					vitesseMax);
		}

		/**
		 * Si on fait une interpolation par clothoïde
		 */
		else
			clotho.getTrajectoire(
					successeur.state.robot.getCinematique(),
					v,
					vitesseMax,
					successeur.cameFromArcStatique);

		return true;
    }
    
    public void configureArcManager(SensFinal sens, DirectionStrategy directionstrategyactuelle, Cinematique arrivee)
    {
    	this.sens = sens;
    	this.directionstrategyactuelle = directionstrategyactuelle;
    	arrivee.copy(this.arrivee);
    }
    
    /**
     * Renvoie "true" si cette vitesse est acceptable par rapport à "current".
     * @param vitesse
     * @return
     */
    private final boolean acceptable(VitesseCourbure vitesse)
    {
    	// il y a un problème si :
    	// - on veut rebrousser chemin
    	// ET
    	// - si :
    	//      - on n'est pas en fast, donc pas d'autorisation
    	//      ET
    	//      - on est dans la bonne direction, donc pas d'autorisation exceptionnelle de se retourner
    	
    	if(vitesse.rebrousse && (directionstrategyactuelle != DirectionStrategy.FASTEST && directionstrategyactuelle.isPossible(current.state.robot.getCinematique().enMarcheAvant)))
    	{ 
//    		log.debug(vitesse+" n'est pas acceptable (rebroussement interdit");
    		return false;
    	}
    	
    	// Si on ne rebrousse pas chemin alors que c'est nécessaire
    	if(!vitesse.rebrousse && !directionstrategyactuelle.isPossible(current.state.robot.getCinematique().enMarcheAvant))
    	{
//    		log.debug(vitesse+" n'est pas acceptable (rebroussement nécessaire");
    		return false;
    	}
    	
    	// on évite les demi-tours absurdes
    	if(vitesse.demitour && ((vitesse.positif && current.state.robot.getCinematique().courbureGeometrique < -1) || (!vitesse.positif && current.state.robot.getCinematique().courbureGeometrique > 1)))
    		return false;
    	
    	// TODO
    	double courbureFuture = current.state.robot.getCinematique().courbureGeometrique + vitesse.vitesse * ClothoidesComputer.DISTANCE_ARC_COURBE_M;
    	if(!(courbureFuture >= -courbureMax && courbureFuture <= courbureMax))
    	{
//        	log.debug(vitesse+" n'est acceptable (courbure trop grande");
    		return false;
    	}
    	
    	double courbure = Math.abs(current.state.robot.getCinematique().courbureGeometrique);
    	if(vitesse.ramene && (courbure < 0.1 || courbure > 3))
    	{
//    		log.debug("Ne peut pas ramener le volant si la courbure est déjà nulle ou bien trop grande…");
    		return false;
    	}

		return true;
    }
    
    /**
     * Y a-t-il encore une vitesse possible ?
     * On vérifie ici si certaines vitesses sont interdites (à cause de la courbure trop grande ou du rebroussement)
     * @return
     */
    public boolean hasNext()
    {
    	while(iterator.hasNext())
    		if(acceptable(iterator.next()))
    		{
    			iterator.previous();
    			return true;
    		}
    	return false;
    }
    
    /**
     * Réinitialise l'itérateur à partir d'un nouvel état
     * @param current
     * @param directionstrategyactuelle
     */
    public void reinitIterator(AStarCourbeNode current)
    {
    	this.current = current;
    	iterator = listeVitesse.listIterator();
    }

	@Override
	public void useConfig(Config config)
	{
		courbureMax = config.getDouble(ConfigInfo.COURBURE_MAX);
		printObs = config.getBoolean(ConfigInfo.GRAPHIC_ROBOT_COLLISION);
	}

	public synchronized Double heuristicCostCourbe(Cinematique c, SensFinal sens)
	{
		return dstarlite.heuristicCostCourbe(c, sens);
	}

	public boolean isArrived(AStarCourbeNode successeur)
	{
		return successeur.getArc() != null && successeur.getArc().getLast().getPosition().squaredDistance(arrivee.getPosition()) < 1 && sens.isOK(successeur.getArc().getLast().enMarcheAvant);
	}

	/**
	 * heuristique de secours
	 * @param cinematique
	 * @return
	 */
	public double heuristicDirect(Cinematique cinematique)
	{
		return 3*cinematique.getPosition().distanceFast(arrivee.getPosition());
	}
}
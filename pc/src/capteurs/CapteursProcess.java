/*
Copyright (C) 2013-2017 Pierre-François Gimenez

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

package capteurs;

import graphic.PrintBuffer;
import obstacles.types.ObstacleProximity;
import obstacles.types.ObstacleRobot;
import obstacles.types.ObstaclesFixes;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.gridspace.GridSpace;
import robot.RobotReal;
import config.Config;
import config.ConfigInfo;
import config.Configurable;
import container.Container;
import container.Service;
import container.dependances.HighPFClass;
import container.dependances.LowPFClass;
import exceptions.ContainerException;
import table.GameElementNames;
import table.Table;
import table.EtatElement;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Cette classe contient les informations sur la situation
 * spatiale des capteurs sur le robot.
 * @author pf
 *
 */

public class CapteursProcess implements Service, Configurable, LowPFClass, HighPFClass
{
	protected Log log;
	private GridSpace gridspace;
	private Table table;
	private DStarLite dstarlite;
	private CheminPathfinding chemin;
	private PrintBuffer buffer;
	private Container container;
	private RobotReal robot;
	
	private int nbCapteurs;
	private int rayonEnnemi;
	private int distanceApproximation;
	private ObstacleRobot obstacleRobot;
	private Capteur[] capteurs;

	public CapteursProcess(Container container, Log log, GridSpace gridspace, Table table, DStarLite dstarlite, CheminPathfinding chemin, PrintBuffer buffer, RobotReal robot)
	{
		this.table = table;
		this.log = log;
		this.gridspace = gridspace;
		this.dstarlite = dstarlite;
		this.chemin = chemin;
		this.buffer = buffer;
		this.container = container;
		this.robot = robot;
		obstacleRobot = new ObstacleRobot(robot);
	}
	
	@Override
	public void useConfig(Config config)
	{
		rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);		
		nbCapteurs = CapteursRobot.values().length;
		
		capteurs = new Capteur[nbCapteurs];
				
		try {
			for(int i = 0; i < nbCapteurs; i++)
			{
				CapteursRobot c = CapteursRobot.values()[i];
				capteurs[i] = container.make(c.classe, c.pos, c.angle, c.type, c.sureleve);
			}			
		} catch(ContainerException e)
		{
			log.critical(e);
		}
		
		if(config.getBoolean(ConfigInfo.GRAPHIC_ROBOT_AND_SENSORS))
			for(Capteur c : capteurs)
				buffer.add(c);
	}

	/**
	 * Met à jour les obstacles mobiles
	 */
	public void updateObstaclesMobiles(SensorsData data)
	{
		double orientationRobot = data.cinematique.orientationReelle;
		Vec2RO positionRobot = data.cinematique.getPosition();

		obstacleRobot.update(positionRobot, orientationRobot);

		/**
		 * On update la table avec notre position
		 */
	    for(GameElementNames g: GameElementNames.values())
	        if(g.obstacle.isColliding(obstacleRobot))
	        	table.setDone(g, EtatElement.PRIS_PAR_NOUS); // on est sûr de l'avoir shooté
					
		/**
		 * Suppression des mesures qui sont hors-table ou qui voient un obstacle de table
		 */
		for(int i = 0; i < nbCapteurs; i++)
		{
			CapteursRobot c = CapteursRobot.values[i];
			
			/*
			 * Les deux capteurs ToF courts arrière voient le filet lorsqu'il est baissé
			 */
			if(robot.isFiletBaisse() && (c == CapteursRobot.ToF_ARRIERE_DROITE || c == CapteursRobot.ToF_ARRIERE_GAUCHE))
				continue;
				
			/*
			 * Le ToF arrière voit le filet et permet de servir de jauge de remplissage
			 */
			if(c == CapteursRobot.ToF_LONG_ARRIERE && !robot.isFiletBaisse())
			{
				if(data.mesures[i] < 60) // filet plein
				{
					robot.filetVuPlein();
					continue;
				}
				robot.filetVuVide(); // filet vide
			}
			
			capteurs[i].computePosOrientationRelative(data.cinematique);
			/**
			 * Si le capteur voit trop proche ou trop loin, on ne peut pas lui faire confiance
			 */
			if(data.mesures[i] < capteurs[i].distanceMin || data.mesures[i] > capteurs[i].portee)
				continue;

			/**
			 * Si ce qu'on voit est un obstacle de table, on l'ignore
			 */
			Vec2RO positionVue = new Vec2RO(data.mesures[i], capteurs[i].orientationRelativeRotate, true);
			
	    	for(ObstaclesFixes o: ObstaclesFixes.values())
	    		if(o.isVisible(capteurs[i].sureleve) && o.getObstacle().squaredDistance(positionVue) < distanceApproximation * distanceApproximation)
	                continue;

	    	for(GameElementNames o: GameElementNames.values())
	    		if(table.isDone(o) != EtatElement.PRIS_PAR_NOUS && o.isVisible(capteurs[i].sureleve) && o.obstacle.squaredDistance(positionVue) < distanceApproximation * distanceApproximation)
	                continue;

			/**
			 * Sinon, on ajoute
			 */
			Vec2RW positionEnnemi = new Vec2RW(data.mesures[i]+rayonEnnemi, capteurs[i].orientationRelativeRotate, true);
			positionEnnemi.plus(capteurs[i].positionRelativeRotate);
			positionEnnemi.rotate(orientationRobot);
			positionEnnemi.plus(positionRobot);
			
			if(positionEnnemi.isHorsTable())
				continue; // hors table
			
			ObstacleProximity o = gridspace.addObstacleAndRemoveNearbyObstacles(positionEnnemi);
			
			/**
			 * Mise à jour de l'état de la table : un ennemi est passé
			 */
		    for(GameElementNames g: GameElementNames.values())
		        if(table.isDone(g) == EtatElement.INDEMNE && g.obstacle.isProcheObstacle(o, o.radius))
		        	table.setDone(g, EtatElement.PRIS_PAR_ENNEMI);

		}
		dstarlite.updateObstaclesEnnemi();
		dstarlite.updateObstaclesTable();
		chemin.checkColliding();
	}
	
}

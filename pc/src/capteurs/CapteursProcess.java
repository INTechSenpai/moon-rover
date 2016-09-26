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

package capteurs;

import graphic.PrintBuffer;
import obstacles.types.ObstacleProximity;
import obstacles.types.ObstaclesFixes;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.dstarlite.DStarLite;
import pathfinding.dstarlite.gridspace.GridSpace;
import container.Service;
import table.GameElementNames;
import table.Table;
import table.Tribool;
import utils.Config;
import utils.ConfigInfo;
import utils.Log;
import utils.Vec2RO;
import utils.Vec2RW;

/**
 * Cette classe contient les informations sur la situation
 * spatiale des capteurs sur le robot.
 * @author pf
 *
 */

public class CapteursProcess implements Service {
	protected Log log;
	private GridSpace gridspace;
	private Table table;
	private DStarLite dstarlite;
	private CheminPathfinding chemin;
	private PrintBuffer buffer;
	
	private int nbCapteurs;
	private int rayonEnnemi;
    private int rayonRobot;
	private int distanceApproximation;

	private Capteur[] capteurs;

	public CapteursProcess(Log log, GridSpace gridspace, Table table, DStarLite dstarlite, CheminPathfinding chemin, PrintBuffer buffer)
	{
		this.table = table;
		this.log = log;
		this.gridspace = gridspace;
		this.dstarlite = dstarlite;
		this.chemin = chemin;
		this.buffer = buffer;
	}
	
	@Override
	public void updateConfig(Config config)
	{}

	@Override
	public void useConfig(Config config)
	{
		rayonEnnemi = config.getInt(ConfigInfo.RAYON_ROBOT_ADVERSE);
		rayonRobot = config.getInt(ConfigInfo.RAYON_ROBOT);
		distanceApproximation = config.getInt(ConfigInfo.DISTANCE_MAX_ENTRE_MESURE_ET_OBJET);		
		nbCapteurs = config.getInt(ConfigInfo.NB_CAPTEURS);
		
		Capteur.useConfig(config);
		capteurs = new Capteur[nbCapteurs];
				
		// TODO
		capteurs[0] = new CapteurMobile(new Vec2RO(200, 80), 0., TypeCapteur.ToF, true);
		capteurs[1] = new CapteurImmobile(new Vec2RO(200, -80), 0., TypeCapteur.IR);
		
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
		
		/**
		 * On update la table avec notre position
		 */
	    for(GameElementNames g: GameElementNames.values())
	        if(g.obstacle.isProcheObstacle(positionRobot, rayonRobot))
	        	table.setDone(g, Tribool.TRUE); // on est sûr de l'avoir shooté
					
		/**
		 * Suppression des mesures qui sont hors-table ou qui voient un obstacle de table
		 */
		for(int i = 0; i < nbCapteurs; i++)
		{
			/**
			 * Si le capteur voit trop proche ou trop loin, on ne peut pas lui faire confiance
			 */
			if(data.mesures[i] < capteurs[i].distanceMin || data.mesures[i] > capteurs[i].portee)
				continue;

			/**
			 * Si ce qu'on voit est un obstacle de table, on l'ignore
			 */
			Vec2RO positionVue = new Vec2RO(data.mesures[i], capteurs[i].getOrientationRelative(data.cinematique), true);
			
	    	for(ObstaclesFixes o: ObstaclesFixes.values())
	    		if(o.visible && o.getObstacle().squaredDistance(positionVue) < distanceApproximation * distanceApproximation)
	                continue;
			
			/**
			 * Sinon, on ajoute
			 */
			Vec2RW positionEnnemi = new Vec2RW(data.mesures[i]+rayonEnnemi, capteurs[i].getOrientationRelative(data.cinematique), true);
			positionEnnemi.plus(capteurs[i].positionRelative);
			positionEnnemi.rotate(orientationRobot);
			positionEnnemi.plus(positionRobot);
			
			if(positionEnnemi.isHorsTable())
				continue; // hors table
			
			ObstacleProximity o = gridspace.addObstacleAndRemoveNearbyObstacles(positionEnnemi);
			
			/**
			 * Mise à jour de l'état de la table
			 */
		    for(GameElementNames g: GameElementNames.values())
		        if(table.isDone(g) == Tribool.FALSE && g.obstacle.isProcheObstacle(o, o.radius))
		        	table.setDone(g, Tribool.MAYBE);

		}
		dstarlite.updateObstacles();
		chemin.checkColliding();
	}
	
}

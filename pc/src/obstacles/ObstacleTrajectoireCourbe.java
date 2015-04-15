package obstacles;

import astar.arc.PathfindingNodes;
import astar.arc.SegmentTrajectoireCourbe;
import robot.Speed;
import utils.ConfigInfo;
import vec2.ReadOnly;
import vec2.ReadWrite;
import vec2.Vec2;

/**
 * Obstacle formé par le robot lorsqu'il effectue une trajectoire courbe
 * @author pf
 *
 */

public class ObstacleTrajectoireCourbe extends ObstacleRectanglesCollection
{
	private SegmentTrajectoireCourbe segment;
	
	/**
	 * 
	 * @param objectifFinal
	 * @param intersection
	 * @param directionAvant de norme 1000
	 * @param vitesse
	 */
	public ObstacleTrajectoireCourbe(PathfindingNodes objectifFinal, PathfindingNodes intersection, Vec2<? extends ReadOnly> directionAvant, Speed vitesse)
	{
		// La position de cet obstacle est assez arbitraire...
		super(intersection.getCoordonnees().clone());
		
		Vec2<ReadWrite> directionApres = new Vec2<ReadWrite>(intersection.getOrientationFinale(objectifFinal));

		int rayonCourbure = vitesse.rayonCourbure();

		double angleDepart = directionAvant.getArgument();
		double angleRotation = (directionApres.getArgument() - angleDepart) % (2*Math.PI);

		if(angleRotation < -Math.PI)
			angleRotation += 2*Math.PI;
		else if(angleRotation > Math.PI)
			angleRotation -= 2*Math.PI;

		int distanceAnticipation = (int)(rayonCourbure * Math.tan(Math.abs(angleRotation/2)));
		
		Vec2<ReadWrite> pointDepart = position.minusNewVector(directionAvant.scalarNewVector(distanceAnticipation/1000.));
		Vec2<ReadWrite> orthogonalDirectionAvant = directionAvant.rotateNewVector(Math.PI/2);

		// Afin de placer le centre du cercle entre les deux directions
		if(orthogonalDirectionAvant.dot(directionApres) < 0)
			Vec2.scalar(orthogonalDirectionAvant, -1);

		Vec2<ReadWrite> centreCercle = pointDepart.plusNewVector(orthogonalDirectionAvant.scalarNewVector(rayonCourbure/1000.));

		int largeurRobot = config.getInt(ConfigInfo.LARGEUR_ROBOT);
		int longueurRobot = config.getInt(ConfigInfo.LONGUEUR_ROBOT);
//		double angleEntreOmbre = Math.atan2(longueurRobot/2, rayonCourbure+largeurRobot/2);
//		nb_rectangles = (int)(Math.abs(angleRotation/angleEntreOmbre))+1;
		nb_rectangles = 10;
		ombresRobot = new ObstacleRectangular[nb_rectangles];
		for(int i = 0; i < nb_rectangles-1; i++)
			ombresRobot[i] = new ObstacleRectangular(pointDepart.rotateNewVector(i*angleRotation/(nb_rectangles-1), centreCercle), longueurRobot, largeurRobot, angleDepart+i*angleRotation/(nb_rectangles-1));
		ombresRobot[nb_rectangles-1] = new ObstacleRectangular(pointDepart.rotateNewVector(angleRotation, centreCercle), longueurRobot, largeurRobot, angleDepart+angleRotation);

//		log.debug("Erreur! diff = "+(2*distanceAnticipation - rayonCourbure * Math.abs(angleRotation)), this);
		
		segment = new SegmentTrajectoireCourbe(objectifFinal, (int)(2*distanceAnticipation - rayonCourbure * Math.abs(angleRotation)), distanceAnticipation, pointDepart.getReadOnly(), directionAvant.getReadOnly(), vitesse);
	}

	/**
	 * Renvoie le segment associé
	 * @return
	 */
	public SegmentTrajectoireCourbe getSegment()
	{
		return segment;
	}

}

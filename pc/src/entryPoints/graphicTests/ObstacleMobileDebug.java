package entryPoints.graphicTests;

import java.util.Random;

import obstacles.OldCapteurs;
import obstacles.ObstaclesMemory;
import buffer.IncomingData;
import buffer.IncomingDataBuffer;
import permissions.ReadOnly;
import permissions.ReadWrite;
import tests.graphicLib.Fenetre;
import utils.Log;
import utils.Sleep;
import utils.Vec2;
import container.Container;
import container.ServiceNames;

/**
 * Test graphique d'obstacles
 * @author pf
 *
 */

public class ObstacleMobileDebug  {

	public static void main(String[] args)
	{
		try {
			Random rand = new Random();
			int bruit = 10;
			Container container = new Container();
			Log log = (Log) container.getService(ServiceNames.LOG);
			OldCapteurs capteurs = (OldCapteurs) container.getService(ServiceNames.CAPTEURS);
			ObstaclesMemory memory = (ObstaclesMemory) container.getService(ServiceNames.OBSTACLES_MEMORY);
			IncomingDataBuffer buffer = (IncomingDataBuffer) container.getService(ServiceNames.INCOMING_DATA_BUFFER);
			Fenetre.setInstance(container);
			Fenetre fenetre = Fenetre.getInstance();
			fenetre.setCapteurs(capteurs);
//			fenetre.setObstaclesMobiles(memory.getListObstaclesMobiles());
			fenetre.showOnFrame();
			int nbPoints = 1;
			@SuppressWarnings("unchecked")
			Vec2<ReadWrite>[] point = new Vec2[nbPoints];
			Vec2<ReadOnly> positionRobot = new Vec2<ReadOnly>(0,1000);
			int nbCapteurs = 8;
			int[] mesures = new int[nbCapteurs];
			mesures[0] = 247;
			mesures[1] = 365;
			mesures[2] = 3000;
			mesures[3] = 3000;
			mesures[4] = 3000;
			mesures[5] = 3000;
			mesures[6] = 3000;
			mesures[7] = 3000;
			
			buffer.add(new IncomingData(mesures));
			Sleep.sleep(100);
			fenetre.repaint();
//			if(true)
//				return;
								
			int dureeSleep = 150;
			for(int k = 0; k < nbPoints; k++)
			{
				point[k] = new Vec2<ReadWrite>(600, 2*Math.PI*k/nbPoints, true);
				Vec2.plus(point[k], positionRobot);
			}
			
// 			long dateDebut = System.currentTimeMillis();
			for(int i = 0; i < 1000; i++)
			{
//				point.y = i;
				
				for(int k = 0; k < nbPoints; k++)
				{
					point[k].y += (int)(rand.nextGaussian()*10);
					point[k].x += (int)(rand.nextGaussian()*10);
				}
//				fenetre.setPoint(point);

				for(int j = 0; j < nbCapteurs; j++)
				{
					mesures[j] = 3000;
					for(int k = 0; k < nbPoints; k++)
					{
						if(capteurs.canBeSeen(point[k].minusNewVector(positionRobot).getReadOnly(), j))
							mesures[j] = Math.min(mesures[j], Math.max((int)(rand.nextGaussian()*bruit) + (int)point[k].distance(positionRobot.plusNewVector(capteurs.positionsRelatives[j]))-200,0));
					}
					log.debug("Capteur "+j+": "+mesures[j]);
				}

				buffer.add(new IncomingData(mesures));
				fenetre.repaint();
				Sleep.sleep(dureeSleep);
			}
//			log.debug("Durée total: "+(System.currentTimeMillis()-dateDebut));
//			log.debug("Fréquence: "+(10000000./(System.currentTimeMillis()-dateDebut)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

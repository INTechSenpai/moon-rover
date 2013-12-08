package table;

import robot.Orientation;
import smartMath.Vec2;
import container.Service;
import utils.*;

public class Table implements Service {

	Fire arrayFire[] = new Fire[16];
	Tree arrayTree[] = new Tree[4];
	Fireplace arrayFireplace[]= new Fireplace[3];
	Torch arrayTorch[] = new Torch[10] ;
	Fruit_Tub arrayTub[] = new Fruit_Tub[2];
	Fresco fresco;
	
	private Log log;
	private Read_Ini config;
	
	public Table(Service log, Service config)
	{
		this.log = (Log) log;
		this.config = (Read_Ini) config;
		
		arrayFire[0] = new Fire(new Vec2(1485,1200), 0, 0, Orientation.XPLUS, Colour.YELLOW);
		arrayFire[1] = new Fire(new Vec2(1100,900), 1, 0, Orientation.YPLUS, Colour.YELLOW);
		arrayFire[2] = new Fire(new Vec2(600,1400), 2, 0, Orientation.XPLUS, Colour.YELLOW);
		arrayFire[3] = new Fire(new Vec2(600,900), 3, 1, Orientation.GROUND, Colour.YELLOW);
		arrayFire[4] = new Fire(new Vec2(600,900), 4, 2, Orientation.GROUND, Colour.RED);
		arrayFire[5] = new Fire(new Vec2(600,900), 5, 3, Orientation.GROUND, Colour.YELLOW);
		arrayFire[6] = new Fire(new Vec2(600,400), 6, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[7] = new Fire(new Vec2(200,15), 7, 0, Orientation.YPLUS, Colour.YELLOW);
		arrayFire[8] = new Fire(new Vec2(-200,15), 8, 0, Orientation.YPLUS, Colour.RED);
		arrayFire[9] = new Fire(new Vec2(-600,1400), 9, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[10] = new Fire(new Vec2(-600,900), 10, 1, Orientation.GROUND, Colour.RED);
		arrayFire[11] = new Fire(new Vec2(-600,900), 11, 2, Orientation.GROUND, Colour.YELLOW);
		arrayFire[12] = new Fire(new Vec2(-600,900), 12, 3, Orientation.GROUND, Colour.RED);
		arrayFire[13] = new Fire(new Vec2(-600,400), 13, 0, Orientation.XPLUS, Colour.RED);
		arrayFire[14] = new Fire(new Vec2(-1100,900), 14, 0, Orientation.YPLUS, Colour.RED);
		arrayFire[15] = new Fire(new Vec2(-1485,1200), 15, 0, Orientation.XPLUS, Colour.YELLOW);
		//on passe à l'initialisation des arbres
		arrayTree[0] = new Tree(new Vec2(1500,700), 0, new Vec2(1396,640),new Vec2(1500,580),new Vec2(1604,640),
								new Vec2(1604,760),new Vec2(1500,820),new Vec2(1396,760));
		arrayTree[1] = new Tree(new Vec2(800,0), 1, new Vec2(740,104),new Vec2(680,0),new Vec2(740,-104),
								new Vec2(860,-104),new Vec2(920,0),new Vec2(860,104));
		arrayTree[2] = new Tree(new Vec2(-800,0), 2, new Vec2(-860,104),new Vec2(-920,0),new Vec2(-860,-104),
								new Vec2(-740,-104),new Vec2(-680,0),new Vec2(-740,104));
		arrayTree[3] = new Tree(new Vec2(-1500,700), 3, new Vec2(-1396,760),new Vec2(-1500,820),new Vec2(-1604,760),
								new Vec2(1604,640),new Vec2(1380,0),new Vec2(1396,640));
		//initialisation des foyers
		arrayFireplace[0] = new Fireplace(new Vec2(1500,0), 250) ;
		arrayFireplace[1] = new Fireplace(new Vec2(0,950), 150) ;
		arrayFireplace[2] = new Fireplace(new Vec2(-1500,0), 250) ;
		//initialisation des torches
		arrayTorch[0] = new Torch(new Vec2(600,900), 0, true, 80) ; //0 et 1 sont les torches mobiles
		arrayTorch[1] = new Torch(new Vec2(-600,900), 1, true, 80) ;  //de 2 à 9, chaque torche est un des piliers des torches fixes
		arrayTorch[2] = new Torch(new Vec2(1489,1258), 2, false, 11) ;
		arrayTorch[3] = new Torch(new Vec2(1489,1142), 3, false, 11) ;
		arrayTorch[4] = new Torch(new Vec2(258,11), 4, false, 11) ;
		arrayTorch[5] = new Torch(new Vec2(142,11), 5, false, 11) ;
		arrayTorch[6] = new Torch(new Vec2(-142,11), 6, false, 11) ;
		arrayTorch[7] = new Torch(new Vec2(-258,11), 7, false, 11) ;
		arrayTorch[8] = new Torch(new Vec2(-1489,1258), 8, false, 11) ;
		arrayTorch[9] = new Torch(new Vec2(-1489,1142), 9, false, 11) ;
		//initialisation des bacs
		arrayTub[0] = new Fruit_Tub(new Vec2(400,1700)) ;
		arrayTub[1] = new Fruit_Tub(new Vec2(-1100,1700)) ;
		//initialisation de la fresque 
		this.fresco = new Fresco(new Vec2(-300, 2000)); //la position est elle du coin en bas
	}
	
}


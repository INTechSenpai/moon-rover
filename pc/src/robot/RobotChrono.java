package robot;

import java.util.ArrayList;

import hook.Hook;
import smartMath.Vec2;
import utils.Log;
import utils.Config;
import enums.ConfigInfo;
import enums.PathfindingNodes;
import enums.Speed;
import exceptions.FinMatchException;

/**
 * Robot particulier qui fait pas bouger le robot réel, mais détermine la durée des actions
 * @author pf et marsu !
 */

public class RobotChrono extends Robot
{
	protected Vec2 position = new Vec2();
	protected PathfindingNodes positionPathfinding;
	protected boolean isPositionPathfindingActive = false;
	protected double orientation;
	
	// Date en millisecondes depuis le début du match.
	protected long date;
	
	/** valeur approchée du temps (en milisecondes) nécéssaire pour qu'une information que l'on envois a la série soit aquité */
	private final static int approximateSerialLatency = 50;

	public RobotChrono(Config config, Log log)
	{
		super(config, log);
	}
	
	@Override
	public void setPosition(Vec2 position) {
		this.position = position.clone();
		isPositionPathfindingActive = false;
		this.date += approximateSerialLatency;
	}
	
	@Override
	public void setOrientation(double orientation) {
		this.orientation = orientation;
		this.date += approximateSerialLatency;
	}

	@Override
    public void avancer(int distance, ArrayList<Hook> hooks, boolean mur) throws FinMatchException
	{
		date += Math.abs(distance)*vitesse.invertedTranslationnalSpeed;
		Vec2 ecart;
        ecart = new Vec2((int)(distance*Math.cos(orientation)), (int)(distance*Math.sin(orientation)));

		checkHooks(position, position.plusNewVector(ecart), hooks);
		position.plus(ecart);
		isPositionPathfindingActive = false;
		this.date += approximateSerialLatency;
		this.date += Speed.translationStopDuration;
	}
	
	@Override
	public void set_vitesse(Speed vitesse)
	{
	    this.vitesse = vitesse;
		this.date += approximateSerialLatency;
	}
	
	@Override
	public long getTempsDepuisDebutMatch()
	{
		return date;
	}

	@Override
	public RobotChrono cloneIntoRobotChrono() throws FinMatchException
	{
		RobotChrono cloned_robotchrono = new RobotChrono(config, log);
		copy(cloned_robotchrono);
		return cloned_robotchrono;
	}

	@Override
    public void tourner(double angle)
    {
        tourner(angle, false);
    }
	
	/**
	 * Donne l'angle entre l'orientation actuelle et l'angle donné en argument
	 * @param angle
	 * @return
	 */
	public double calculateDelta(double angle)
	{
		double delta = orientation-angle;
		if(delta < 0)
			delta *= -1;
		while(delta > 2*Math.PI)
			delta -= 2*Math.PI;
		if(delta > Math.PI)
			delta = 2*(float)Math.PI - delta;
		return delta;
	}
	
	@Override
    public void tourner(double angle, boolean mur)
	{
		// TODO: avec les trajectoires courbes, les durées changent
		// et la marche arrière automatique?
		double delta = calculateDelta(angle);
		orientation = angle;
		date += delta*vitesse.invertedRotationnalSpeed;
		isPositionPathfindingActive = false;
		this.date += approximateSerialLatency;
		this.date += Speed.rotationStopDuration;
	}

	@Override
    public void suit_chemin(ArrayList<PathfindingNodes> chemin, ArrayList<Hook> hooks) throws FinMatchException
	{
		for(PathfindingNodes point: chemin)
			va_au_point(point.getCoordonnees(), hooks);
	}
	
	public void va_au_point(Vec2 point, ArrayList<Hook> hooks) throws FinMatchException
	{
		double orientation_finale = Math.atan2(point.y - position.y, point.x - position.x);
		tourner(orientation_finale);
		checkHooks(position, point, hooks);
		date += position.distance(point)*vitesse.invertedTranslationnalSpeed;
		position = point.clone();
		isPositionPathfindingActive = false;
		date += approximateSerialLatency;
		date += Speed.translationStopDuration;
	}

	public void va_au_point_pathfinding(PathfindingNodes n, ArrayList<Hook> hooks) throws FinMatchException
	{
		if(!isPositionPathfindingActive)
			va_au_point(n.getCoordonnees(), hooks);
		else
		{
			tourner(positionPathfinding.getOrientationFinale(n));
			checkHooks(position, n.getCoordonnees(), hooks);
			date += positionPathfinding.distanceTo(n)*vitesse.invertedTranslationnalSpeed;
			date += approximateSerialLatency;
			date += Speed.translationStopDuration;			
		}
		setPositionPathfinding(n);
	}

	/**
	 * Utilisé par les tests
	 * @param other
	 * @return
	 */
	// TODO à compléter au fur et à mesure
	public boolean equals(RobotChrono other)
	{
		return 	position.equals(other.position)
				&& orientation == other.orientation
				&& positionPathfinding == other.positionPathfinding
				&& isPositionPathfindingActive == other.isPositionPathfindingActive
				&& date == other.date;

	}

	@Override
	public void sleep(long duree, ArrayList<Hook> hooks) throws FinMatchException 
	{
		this.date += duree;
		checkHooks(position, position, hooks);
	}
	
	@Override
    public void stopper()
    {
		this.date += approximateSerialLatency;
		this.date += Speed.translationStopDuration;
    }

    @Override
    public Vec2 getPosition()
    {
        return position.clone();
    }

    @Override
    public double getOrientation()
    {
        return orientation;
    }
    
    @Override
    public void copy(RobotChrono rc) throws FinMatchException
    {
        super.copy(rc);
        position.copy(rc.position);
        rc.orientation = orientation;
    	rc.positionPathfinding = positionPathfinding;
    	rc.isPositionPathfindingActive = isPositionPathfindingActive;
    }

    public void desactiver_asservissement_rotation()
    {
		this.date += approximateSerialLatency;
    }

    public void activer_asservissement_rotation()
    {
    	this.date += approximateSerialLatency;
    }

	@Override
	public void setInsiste(boolean insiste) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * On déclenche tous les hooks entre le point A et le point B.
	 * @param pointA
	 * @param pointB
	 * @param hooks
	 * @throws FinMatchException 
	 */
	private void checkHooks(Vec2 pointA, Vec2 pointB, ArrayList<Hook> hooks) throws FinMatchException
	{
		if(hooks != null)
			for(Hook hook: hooks)
				if(hook.simulated_evaluate(pointA, pointB, date))
					hook.trigger();
	}

	public void setPositionPathfinding(PathfindingNodes n)
	{
		position = n.getCoordonnees().clone();
		positionPathfinding = n;
		isPositionPathfindingActive = true;
	}
	
	public PathfindingNodes getPositionPathfinding()
	{
		if(isPositionPathfindingActive)
			return positionPathfinding;
		return null;
	}

	/**
	 * Appelé par le pathfinding. Corrige le temps d'une trajectoire lorsqu'on la lisse.
	 * Au lieu de passer par "depart, n1, n2" on saute n1 et on fait le trajet "depart, n2".
	 * @param depart
	 * @param n1
	 * @param n2
	 */
	public void corrige_temps(Vec2 depart, Vec2 n1, Vec2 n2)
	{
		date -= depart.distance(n1)*vitesse.invertedTranslationnalSpeed;
		date -= n1.distance(n2)*vitesse.invertedTranslationnalSpeed;
		date += n1.distance(n2)*vitesse.invertedTranslationnalSpeed;
	}
	
	public void setFinalState()
	{
		super.setFinalState();
		date = Integer.parseInt(config.get(ConfigInfo.DUREE_MATCH_EN_S))*1000;
	}

	/**
	 * UTILISE UNIQUEMENT PAR LES TESTS
	 */
	public void reinitDate()
	{
		date = 0;
	}
	
}

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

package graphic;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import graphic.printable.Couleur;
import graphic.printable.Layer;
import graphic.printable.Printable;
import obstacles.types.ObstacleCircular;
import pathfinding.chemin.CheminPathfinding;
import pathfinding.chemin.IteratorCheminPathfinding;
import robot.Cinematique;
import robot.RobotReal;
import utils.Log;

/**
 * PrintBuffer prévu pour l'externalisation de l'affichage
 * @author pf
 *
 */

public class ExternalPrintBuffer implements PrintBufferInterface {
	private List<ArrayList<Serializable>> elementsAffichablesSupprimables = new ArrayList<ArrayList<Serializable>>();
	private List<ArrayList<Serializable>> elementsAffichables = new ArrayList<ArrayList<Serializable>>();
	private RobotReal robot = null;
	private IteratorCheminPathfinding iterChemin = null;
	
	protected Log log;
	
	public ExternalPrintBuffer(Log log)
	{
		this.log = log;
		for(int i = 0 ; i < Layer.values().length; i++)
		{
			elementsAffichablesSupprimables.add(new ArrayList<Serializable>());
			elementsAffichables.add(new ArrayList<Serializable>());
		}
	}

	/**
	 * Supprime tous les obstacles supprimables
	 * @param c
	 */
	@Override
	public synchronized void clearSupprimables()
	{
		for(int i = 0 ; i < Layer.values().length; i++)
			elementsAffichablesSupprimables.get(i).clear();
		notify();
	}

	/**
	 * Ajoute un obstacle dans la liste des supprimables
	 * @param o
	 */
	@Override
	public synchronized void addSupprimable(Printable o)
	{
		addSupprimable(o, o.getLayer());
	}

	/**
	 * Ajoute un obstacle dans la liste des supprimables
	 * @param o
	 */
	@Override
	public synchronized void addSupprimable(Printable o, Layer l)
	{
		add(o, l, elementsAffichablesSupprimables);
	}

	/**
	 * Ajoute un obstacle
	 * @param o
	 */
	@Override
	public synchronized void add(Printable o)
	{
		add(o, o.getLayer(), elementsAffichables);
	}
	
	private void add(Printable o, Layer l, List<ArrayList<Serializable>> list)
	{
		if(o instanceof RobotReal)
		{
			robot = ((RobotReal) o);
			notify();
		}
		else if(o instanceof CheminPathfinding)
		{
			iterChemin = new IteratorCheminPathfinding((CheminPathfinding) o);
		}
		else if(o instanceof Serializable)
		{
			list.get(l.ordinal()).add((Serializable)o);
			notify();
		}
	}

	/**
	 * Supprime un printable ajouté à la liste des supprimables
	 * Ce n'est pas grave s'il y a une double suppression
	 * @param o
	 */
	@Override
	public synchronized void removeSupprimable(Printable o)
	{
		if(elementsAffichablesSupprimables.get(o.getLayer().ordinal()).remove(o))
			notify();
	}

	/**
	 * Envoie en sérialisant les objets à afficher
	 * @param out
	 * @throws IOException 
	 */
	public synchronized void send(ObjectOutputStream out) throws IOException
	{
		ArrayList<Serializable> o = new ArrayList<Serializable>();
		
		if(robot != null)
		{
			o.add(robot.getCinematique());
//			log.debug(o.get(0));
		}
		
		for(int i = 0 ; i < Layer.values().length; i++)
		{
			for(Serializable p : elementsAffichablesSupprimables.get(i))
			{
				o.add(p);
				o.add(Layer.values()[i]);
			}

			for(Serializable p : elementsAffichables.get(i))
			{
				o.add(p);
				o.add(Layer.values()[i]);
			}
		}
		
		if(iterChemin != null)
		{
			iterChemin.reinit();
			while(iterChemin.hasNext())
			{
				Cinematique a = iterChemin.next();
				o.add(new ObstacleCircular(a.getPosition(), 8, Couleur.TRAJECTOIRE));
				o.add(Layer.MIDDLE);
			}
		}
		
		out.writeObject(o);
		out.flush(); // on force l'envoi !
	}


	
}

/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package pathfinding.dstarlite.gridspace;

import java.io.Serializable;

/**
 * Une structure utilisée par le GridSpace
 * 
 * @author pf
 *
 */

public class PointDirige implements Serializable
{
	private static final long serialVersionUID = 7904466980326128967L;
	public final PointGridSpace point;
	public final Direction dir;

	PointDirige(PointGridSpace point, Direction dir)
	{
		this.point = point;
		this.dir = dir;
	}

	@Override
	public int hashCode()
	{
		return (point.hashcode << 3) + dir.ordinal();
	}

	@Override
	public boolean equals(Object d)
	{
		return d instanceof PointDirige && hashCode() == ((PointDirige) d).hashCode();
	}

	@Override
	public String toString()
	{
		return point + " " + dir;
	}

}

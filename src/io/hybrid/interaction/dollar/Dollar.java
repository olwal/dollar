/* -------------------------------------------------------------------------
 *
 *	$1 Java
 *
 * 	This is a Java port of the $1 Gesture Recognizer by
 *	Jacob O. Wobbrock, Andrew D. Wilson, Yang Li.
 * 
 *	"The $1 Unistroke Recognizer is a 2-D single-stroke recognizer designed for 
 *	rapid prototyping of gesture-based user interfaces."
 *	 
 *	http://depts.washington.edu/aimgroup/proj/dollar/
 *
 *	Copyright (C) 2009, Alex Olwal, www.olwal.com
 *
 *	$1 Java free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	$1 Java is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with $1 Java.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  -------------------------------------------------------------------------
 */

package io.hybrid.interaction.dollar;

import io.hybrid.interaction.touch.TouchListener;

import java.util.Vector;

//import javax.microedition.lcdui.Graphics; //Java ME -> render
//import java.awt.Graphics; //Java SE -> render


public class Dollar implements TouchListener
{
	protected int x, y;
	protected int state;
		
	protected int _key = -1;
	
	protected boolean gesture = true; 
	protected Vector points = new Vector(1000);
	
	protected Recognizer recognizer;
	protected Result result = new Result("no gesture", 0, -1);
	
	protected boolean active = false;
	
	protected DollarListener listener = null;

	public static final int GESTURES_DEFAULT = 1;
	public static final int GESTURES_SIMPLE = 2;
	public static final int GESTURES_CIRCLES = 3;
	
	protected int gestureSet;
	
	public Dollar()
	{
		this(GESTURES_SIMPLE);
	}
	
	public Dollar(int gestureSet)
	{
		this.gestureSet = gestureSet;
		recognizer = new Recognizer(gestureSet);
	}
	
	public void setListener(DollarListener listener)
	{
		this.listener = listener;
	}

	/*
	public void render(Graphics g) 
	{
		if (!active)
			return;
		
		Point p1, p2;
				
//		g.setColor(0x999999);
		
		for (int i = 0; i < points.size()-1; i++)
		{
			p1 = (Point)points.elementAt(i);
			p2 = (Point)points.elementAt(i+1);
			g.drawLine((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y);
		}		
	}
	*/
	
	public Vector getPoints()
	{
		return points;
	}
	
	public void addPoint(int x, int y)
	{
		if (!active)
			return;
		
		points.addElement(new Point(x, y));
//		System.out.println(x + " " + y + " " + points.size());
	}	
	
	public void recognize()
	{
		if (!active)
			return;
		
		if (points.size() == 0) //the recognizer will crash if we try to process an empty set of points...
			return;
		
		result = recognizer.Recognize(points);		
//		points.removeAllElements();
		
		if (listener != null)
			listener.dollarDetected(this);
	}

	public Rectangle getBoundingBox()
	{
		return recognizer.boundingBox;
	}
	
	public int[] getBounds()
	{
		return recognizer.bounds;
	}
	
	public Point getPosition()
	{
		return recognizer.centroid;
	}
	
	public String getName()
	{		
		return result.Name;
	}
	
	public double getScore()
	{
		return result.Score;
	}

	public int getIndex()
	{
		return result.Index;
	}

	public void setActive(boolean state)
	{
		active = state;
	}
	
	public boolean getActive()
	{
		return active;
	}	
	
	public void pointerPressed(int x, int y)
	{
		clear();
	}
	
	public void pointerReleased(int x, int y)
	{
		recognize();
	}
	
	public void pointerDragged(int x, int y)
	{
		addPoint(x, y);
	}
	
	public void clear()
	{
		points.removeAllElements();
		result.Name = "";
		result.Score = 0;
		result.Index = -1;
	}
	
}



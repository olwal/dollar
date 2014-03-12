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

//TODO: Most of the code here could be significantly optimized. This was a quick port from the C# version of the library


import io.hybrid.util.Trigonometric;

import java.util.Vector;
import java.util.Enumeration;

public class Utils
{	 
	public static double lastTheta;
	
	public static Vector Resample(Vector points, int n)
	{		
		double I = PathLength(points) / (n 	- 1); // interval length
		double D = 0.0;
		
		Vector srcPts = new Vector(points.size());
		for (int i = 0; i < points.size(); i++)
			srcPts.addElement(points.elementAt(i));
		
		Vector dstPts = new Vector(n);
		dstPts.addElement(srcPts.elementAt(0));	//assumes that srcPts.size() > 0
		
		for (int i = 1; i < srcPts.size(); i++)
		{
			Point pt1 = (Point) srcPts.elementAt(i - 1);
			Point pt2 = (Point) srcPts.elementAt(i);

			double d = Distance(pt1, pt2);
			if ((D + d) >= I)
			{
				double qx = pt1.X + ((I - D) / d) * (pt2.X - pt1.X);
				double qy = pt1.Y + ((I - D) / d) * (pt2.Y - pt1.Y);
				Point q = new Point(qx, qy);
				dstPts.addElement(q); // append new point 'q'
				srcPts.insertElementAt(q, i); // insert 'q' at position i in points s.t. 'q' will be the next i
				D = 0.0;
			}
			else
			{
				D += d;
			}
		}
		// somtimes we fall a rounding-error short of adding the last point, so add it if so
		if (dstPts.size() == n - 1)
		{
			dstPts.addElement(srcPts.elementAt(srcPts.size() - 1));
		}

		return dstPts;
	}

	
	public static Vector RotateToZero(Vector points)
	{	return RotateToZero(points, null, null);	}

	
	public static Vector RotateToZero(Vector points, Point centroid, Rectangle boundingBox)
	{
		Point c = Centroid(points);
		Point first = (Point)points.elementAt(0);
		double theta = Trigonometric.atan2(c.Y - first.Y, c.X - first.X);
		
		if (centroid != null)
			centroid.copy(c);
		
		if (boundingBox != null)
			BoundingBox(points, boundingBox);
		
		lastTheta = theta;
		
		return RotateBy(points, -theta);
	}		
	
	public static Vector RotateBy(Vector points, double theta)
	{
		return RotateByRadians(points, theta);
	}
	
	// rotate the points by the given radians about their centroid
	public static Vector RotateByRadians(Vector points, double radians)
	{
		Vector newPoints = new Vector(points.size());
		Point c = Centroid(points);

		double _cos = Math.cos(radians);
		double _sin = Math.sin(radians);

		double cx = c.X;
		double cy = c.Y;

		for (int i = 0; i < points.size(); i++)
		{
			Point p = (Point) points.elementAt(i);

			double dx = p.X - cx;
			double dy = p.Y - cy;

			newPoints.addElement(
				new Point(	dx * _cos - dy * _sin + cx, 
							dx * _sin + dy * _cos + cy )
							);
		}
		return newPoints;
	}

	public static Vector ScaleToSquare(Vector points, double size)
	{
		return ScaleToSquare(points, size, null);
	}				

	public static Vector ScaleToSquare(Vector points, double size, Rectangle boundingBox)
	{
		Rectangle B = BoundingBox(points);
		Vector newpoints = new Vector(points.size());
		for (int i = 0; i < points.size(); i++)
		{
			Point p = (Point)points.elementAt(i);
			double qx = p.X * (size / B.Width);
			double qy = p.Y * (size / B.Height);
			newpoints.addElement(new Point(qx, qy));
		}
		
		if (boundingBox != null) //this will probably not be used as we are more interested in the pre-rotated bounding box -> see RotateToZero
			boundingBox.copy(B);
		
		return newpoints;
	}			
	
	public static Vector TranslateToOrigin(Vector points)
	{
		Point c = Centroid(points);
		Vector newpoints = new Vector(points.size());
		for (int i = 0; i < points.size(); i++)
		{
			Point p = (Point)points.elementAt(i);
			double qx = p.X - c.X;
			double qy = p.Y - c.Y;
			newpoints.addElement(new Point(qx, qy));
		}
		return newpoints;
	}			
	
	public static double DistanceAtBestAngle(Vector points, Template T, double a, double b, double threshold)
	{
		double Phi = Recognizer.Phi;
	
		double x1 = Phi * a + (1.0 - Phi) * b;
		double f1 = DistanceAtAngle(points, T, x1);
		double x2 = (1.0 - Phi) * a + Phi * b;
		double f2 = DistanceAtAngle(points, T, x2);
		
		while (Math.abs(b - a) > threshold)
		{
			if (f1 < f2)
			{
				b = x2;
				x2 = x1;
				f2 = f1;
				x1 = Phi * a + (1.0 - Phi) * b;
				f1 = DistanceAtAngle(points, T, x1);
			}
			else
			{
				a = x1;
				x1 = x2;
				f1 = f2;
				x2 = (1.0 - Phi) * a + Phi * b;
				f2 = DistanceAtAngle(points, T, x2);
			}
		}
		return Math.min(f1, f2);
	}			

	public static double DistanceAtAngle(Vector points, Template T, double theta)
	{
		Vector newpoints = RotateBy(points, theta);
		return PathDistance(newpoints, T.Points);
	}		

//	#region Lengths and Rects	
	
	public static Rectangle BoundingBox(Vector points)
	{
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
	
		Enumeration e = points.elements();
		
//		foreach (Point p in points)
		while (e.hasMoreElements())
		{
			Point p = (Point)e.nextElement();
		
			if (p.X < minX)
				minX = p.X;
			if (p.X > maxX)
				maxX = p.X;
		
			if (p.Y < minY)
				minY = p.Y;
			if (p.Y > maxY)
				maxY = p.Y;
		}
	
		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	public static void BoundingBox(Vector points, Rectangle dst)
	{
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
	
		Enumeration e = points.elements();
		
//		foreach (Point p in points)
		while (e.hasMoreElements())
		{
			Point p = (Point)e.nextElement();
		
			if (p.X < minX)
				minX = p.X;
			if (p.X > maxX)
				maxX = p.X;
		
			if (p.Y < minY)
				minY = p.Y;
			if (p.Y > maxY)
				maxY = p.Y;
		}
	
		dst.X = minX;
		dst.Y = minY;
		dst.Width = maxX - minX;
		dst.Height = maxY - minY;
	}	
	
	public static double Distance(Point p1, Point p2)
	{
		double dx = p2.X - p1.X;
		double dy = p2.Y - p1.Y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	// compute the centroid of the points given
	public static Point Centroid(Vector points)
	{
		double xsum = 0.0;
		double ysum = 0.0;
		
		Enumeration e = points.elements();
		
//		foreach (Point p in points)
		while (e.hasMoreElements())
		{
			Point p = (Point)e.nextElement();
			xsum += p.X;
			ysum += p.Y;
		}
		return new Point(xsum / points.size(), ysum / points.size());
	}

	public static double PathLength(Vector points)
	{
		double length = 0;
		for (int i = 1; i < points.size(); i++)
		{
			//length += Distance((Point) points[i - 1], (Point) points[i]);
			length += Distance((Point) points.elementAt(i - 1), (Point) points.elementAt(i));
		}
		return length;
	}

	// computes the 'distance' between two point paths by summing their corresponding point distances.
	// assumes that each path has been resampled to the same number of points at the same distance apart.
	public static double PathDistance(Vector path1, Vector path2)
	{            
		double distance = 0;
		for (int i = 0; i < path1.size(); i++)
		{
			distance += Distance((Point) path1.elementAt(i), (Point) path2.elementAt(i));
		}
		return distance / path1.size();
	}

}

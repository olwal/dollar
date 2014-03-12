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

import java.util.*;

public class Recognizer
{
	//
	// Recognizer class constants
	//
	int NumTemplates = 16;
	public static int NumPoints = 64;
	public static double SquareSize = 250.0;
	double HalfDiagonal = 0.5 * Math.sqrt(250.0 * 250.0 + 250.0 * 250.0);
	double AngleRange = 45.0;
	double AnglePrecision = 2.0;
	public static double Phi = 0.5 * (-1.0 + Math.sqrt(5.0)); // Golden Ratio
	
	public Point centroid = new Point(0, 0);
	public Rectangle boundingBox = new Rectangle(0, 0, 0, 0);
	int bounds[] = { 0, 0, 0, 0 };
	
	Vector Templates = new Vector(NumTemplates);

	public static final int GESTURES_DEFAULT = 1;
	public static final int GESTURES_SIMPLE = 2;
	public static final int GESTURES_CIRCLES = 3;	
	
	public Recognizer()
	{
		this(GESTURES_SIMPLE);
	}

	public Recognizer(int gestureSet)
	{
		switch(gestureSet)
		{
			case GESTURES_DEFAULT:
				loadTemplatesDefault(); break;

			case GESTURES_SIMPLE:
				loadTemplatesSimple();	break;

			case GESTURES_CIRCLES:
				loadTemplatesCircles();	break;
		}
	}
	
	void loadTemplatesDefault()
	{
		Templates.addElement(loadTemplate("triangle", TemplateData.trianglePoints));
		Templates.addElement(loadTemplate("x", TemplateData.xPoints));
		Templates.addElement(loadTemplate("rectangle CCW", TemplateData.rectanglePointsCCW));
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("check", TemplateData.checkPoints));
		Templates.addElement(loadTemplate("caret CW", TemplateData.caretPointsCW));
		Templates.addElement(loadTemplate("question", TemplateData.questionPoints));
		Templates.addElement(loadTemplate("arrow", TemplateData.arrowPoints));
		Templates.addElement(loadTemplate("leftSquareBracket", TemplateData.leftSquareBracketPoints));
		Templates.addElement(loadTemplate("rightSquareBracket", TemplateData.rightSquareBracketPoints));
		Templates.addElement(loadTemplate("v", TemplateData.vPoints));
		Templates.addElement(loadTemplate("delete", TemplateData.deletePoints));	
		Templates.addElement(loadTemplate("leftCurlyBrace", TemplateData.leftCurlyBracePoints));
		Templates.addElement(loadTemplate("rightCurlyBrace", TemplateData.rightCurlyBracePoints));
		Templates.addElement(loadTemplate("star", TemplateData.starPoints));
		Templates.addElement(loadTemplate("pigTail", TemplateData.pigTailPoints));
	}
	
	void loadTemplatesSimple()
	{
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("circle CW", TemplateData.circlePointsCW));
//		Templates.addElement(loadTemplate("rectangle CCW", TemplateData.rectanglePointsCCW));
//		Templates.addElement(loadTemplate("rectangle CW", TemplateData.rectanglePointsCW));
		Templates.addElement(loadTemplate("caret CCW", TemplateData.caretPointsCCW));
		Templates.addElement(loadTemplate("caret CW", TemplateData.caretPointsCW));
//		Templates.addElement(loadTemplate("line left", TemplateData.lineToLeftPoints));
//		Templates.addElement(loadTemplate("line right", TemplateData.lineToRightPoints));
		Templates.addElement(loadTemplate("question", TemplateData.questionPoints));
	}
	
	void loadTemplatesCircles()
	{
		Templates.addElement(loadTemplate("circle CCW", TemplateData.circlePointsCCW));
		Templates.addElement(loadTemplate("circle CW", TemplateData.circlePointsCW));
	}

	void loadTemplatesPhoto()
	{
	}
	
	
	Template loadTemplate(String name, int[] array)
	{
		return new Template(name, loadArray(array));
	}
	
	Vector loadArray(int[] array)
	{
		Vector v = new Vector(array.length/2);
		for (int i = 0; i < array.length; i+= 2)
		{
			Point p = new Point(array[i], array[i+1]);
			v.addElement(p);
		}
		
	//	System.out.println(v.size() + " " + array.length);
	
		return v;
	}
	
	public Result Recognize(Vector points)
	{
		points = Utils.Resample(points, NumPoints);		
		points = Utils.RotateToZero(points, centroid, boundingBox);
		points = Utils.ScaleToSquare(points, SquareSize);
		points = Utils.TranslateToOrigin(points);
	
		bounds[0] = (int)boundingBox.X;
		bounds[1] = (int)boundingBox.Y;
		bounds[2] = (int)boundingBox.X + (int)boundingBox.Width;
		bounds[3] = (int)boundingBox.Y + (int)boundingBox.Height;
		
		int t = 0;
		
		double b = Double.MAX_VALUE;
		for (int i = 0; i < Templates.size(); i++)
		{
			double d = Utils.DistanceAtBestAngle(points, (Template)Templates.elementAt(i), -AngleRange, AngleRange, AnglePrecision);
			if (d < b)
			{
				b = d;
				t = i;
			}
		}
		double score = 1.0 - (b / HalfDiagonal);
		return new Result(((Template)Templates.elementAt(t)).Name, score, t, Utils.lastTheta);
	};

	int AddTemplate(String name, Vector points)
	{
		Templates.addElement(new Template(name, points));
		return Templates.size();
	}
	
	int DeleteUserTemplates()
	{
		for (int i = Templates.size()-NumTemplates; i > 0; i--)
		{
			Templates.removeElementAt(Templates.size()-1);
		}
		
		return Templates.size();
	}

}

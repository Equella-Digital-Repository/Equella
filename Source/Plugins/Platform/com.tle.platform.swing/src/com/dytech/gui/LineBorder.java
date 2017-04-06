package com.dytech.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

/**
 * A class which implements a line border of arbitrary thickness and of a single
 * color.
 * <p>
 * <strong>Warning:</strong> Serialized objects of this class will not be
 * compatible with future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Swing. As of 1.4, support for long term storage of all
 * JavaBeans<sup><font size="-2">TM</font></sup> has been added to the
 * <code>java.beans</code> package. Please see {@link java.beans.XMLEncoder}.
 * 
 * @version 1.21 12/03/01
 * @author David Kloba modified by bmillar
 */
public class LineBorder extends AbstractBorder
{
	private Color lineColor;

	private int top = 1;
	private int left = 1;
	private int bottom = 1;
	private int right = 1;

	/**
	 * Creates a line border with the specified color, thickness, and corner
	 * shape.
	 * 
	 * @param color the color of the border
	 */
	public LineBorder(Color color, int top, int left, int bottom, int right)
	{
		lineColor = color;
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
		Color oldColor = g.getColor();
		int i;

		// / PENDING(klobad) How/should do we support Roundtangles?
		g.setColor(lineColor);
		for( i = 0; i < top; i++ )
		{
			g.drawLine(x + i, y + i, x + width - i - 1, y + i);
		}
		for( i = 0; i < left; i++ )
		{
			g.drawLine(x + i, y + i, x + i, y + height - i - 1);
		}
		for( i = 0; i < bottom; i++ )
		{
			g.drawLine(x + i, y + height - i - 1, x + width - i - 1, y + height - i - 1);
		}
		for( i = 0; i < right; i++ )
		{
			g.drawLine(x + width - i - 1, y + i, x + width - i - 1, y + height - i - 1);
		}
		g.setColor(oldColor);
	}

	@Override
	public Insets getBorderInsets(Component c)
	{
		return new Insets(top, left, bottom, right);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets)
	{
		insets.left = left;
		insets.top = top;
		insets.right = right;
		insets.bottom = bottom;
		return insets;
	}

	/**
	 * Returns the color of the border.
	 */
	public Color getLineColor()
	{
		return lineColor;
	}
}

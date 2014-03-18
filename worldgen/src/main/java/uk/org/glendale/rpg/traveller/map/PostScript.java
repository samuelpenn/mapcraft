/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.5 $
 * $Date: 2007/12/09 17:45:17 $
 */

package uk.org.glendale.rpg.traveller.map;

import java.io.*;

public class PostScript {
	FileOutputStream	file = null;
	PrintStream			out = null;

	static final double COS30 = 0.8660254038;  // sqrt(3)/2
	static final double COS60 = 0.5;
	static final double SIN60 = 0.8660254038;
	static final double SIN30 = 0.5;
	static final double ROOT_TWO = 1.414213562;

	int		leftMargin, topMargin;
	int		scale;


	public
	PostScript(File filename) {
		try {
			file = new FileOutputStream(filename);
			out = new PrintStream(file, true);
        } catch (Exception e) {
			System.out.println(e);
        }

		out.println("%!PS-Adobe-2.0");
		out.println("%%Creator: PostScript.java");
		out.println("%%Pages: 1");
		out.println("%%BoundingBox: 2 -2590 3210 829");
		out.println("%%BeginProlog");
		out.println("/mm {0.5 mul} def");
		out.println("0.1 setlinewidth");
   	}

	public void
	close() {
		try {
			out.println("showpage");
			file.close();
        } catch (Exception e) {
			System.out.println(e);
        }
	}

	public void	plotMove(double x, double y) {
		out.println(""+x+" mm "+y+" mm moveto");
    }

	public void	plotLine(double x, double y) {
		out.println(""+x+" mm "+y+" mm lineto");
    }

	public void	plotLine(double x0, double y0, double x1, double y1) {
		out.println("newpath");
		plotMove(x0, y0);
		plotLine(x1, y1);
		out.println("closepath");
		out.println("stroke");
    }

	/**
	 * Angles:
	 *  x = x * cos(a) - y * sin(a)
	 *  y = x * sin(a) + y * cos(a)
	 */
	public void	plotHexagon(double ox, double oy, double size) {
		double 		x = ox, y = oy;
		double 		px, py;
		double		topLeft_x, top_y, topRight_x, right_x, middle_y, bottom_y, left_x;

		topLeft_x = x;
		top_y = y;
		topRight_x = x + size;

		right_x = topRight_x + (size * COS60 - 0 * SIN60);
		middle_y = y - (size * SIN60 + 0 * COS60);

		bottom_y = y - 2 * (size * SIN60 + 0 * COS60);

		left_x = x - (size * COS60 - 0 * SIN60);

		out.println("newpath");
		plotMove(topLeft_x, top_y);
		plotLine(topRight_x, top_y);
		plotLine(right_x, middle_y);
		plotLine(topRight_x, bottom_y);
		plotLine(topLeft_x, bottom_y);
		plotLine(left_x, middle_y);
		out.println("closepath");
		out.println("stroke");
		//out.println(""+topLeft_x+" mm "+top_y+" mm moveto (blue) show");
	}

	public void	plotFont(String name, double size) {
		out.println("/"+name+" findfont "+size+" scalefont setfont");
	}

	public void	plotText(double x, double y, String text) {
		out.println(""+x+" mm "+y+" mm moveto ("+text+") show");
    }

	public void	plotText(double x, double y, String text, String rgb) {
		out.println(rgb+" setrgbcolor");
		out.println(""+x+" mm "+y+" mm moveto ("+text+") show");
		out.println("0 0 0 setrgbcolor");
    }

	public void	plotCircle(double x, double y, double size, String rgb) {
		out.println("newpath");
		out.println(rgb+" setrgbcolor");
		out.println(""+x+" mm "+y+" mm "+size+" mm 0 360 arc");
		out.println("closepath");
		out.println("fill");
		out.println("0 0 0 setrgbcolor");
    }

	public void	plotCircleOutline(double x, double y, double size, String rgb) {
		out.println("newpath");
		out.println(rgb+" setrgbcolor");
		out.println(""+x+" mm "+y+" mm "+size+" mm 0 360 arc");
		out.println("closepath");
		out.println("stroke");
		out.println("0 0 0 setrgbcolor");
    }
	
	public void plotRectangle(double x, double y, double width, double height, String rgb) {
		out.println("newpath");
		out.println(rgb+" setrgbcolor");
		plotMove(x, y);
		plotLine(x+width, y);
		plotLine(x+width, y-height);
		plotLine(x, y-height);
		plotLine(x, y);
		out.println("closepath");
		out.println("fill");
		out.println(rgb+" setrgbcolor");
	}

	public void	setLeftMargin(int margin) { this.leftMargin = margin; }

	public void	setTopMargin(int margin) { this.topMargin = margin; }

	public void	setScale(int scale) { this.scale = scale; }

	/**
	 * Get actual coordinate of the hexagon specified by x and y index.
	 * 
	 * @param x		X index of hexagon.
	 * @param y		Y index of hexagon.
	 * @return			X coordinate of top left of hexagon.
	 */
	public double getX(double x, double y) {
		return leftMargin + (x*(scale * 1.5));
	}

	/**
	 * Get actual coordinate of the hexagon specified by x and y index.
	 * 
	 * @param x		X index of hexagon.
	 * @param y		Y index of hexagon.
	 * @return			Y coordinate of top left of hexagon.
	 */
	public double getY(double x, double y) {
		return (topMargin - (x%2)*(scale*SIN60) - y*(SIN60*2*scale));
	}

	public int getScale() { return scale; }

	/**
	 * Plot a Traveller hex map of the given size, with coordinates printed for
	 * each hex. Assumes the top left starts from hex 0101.
	 * 
	 * @param width
	 * @param height
	 */
	public void plotHexMap(int width, int height) {
		plotHexMap(width, height, 0, 0);
	}

	/**
	 * Plot a Traveller hex map of the given size, with coordinates printed for
	 * each hex. The top left coordinate will be xoff+1, yoff+1 (set both to be
	 * zero for a start of 0101).
	 * 
	 * @param width
	 * @param height
	 * @param xoff
	 * @param yoff
	 */
	public void	plotHexMap(int width, int height, int xoff, int yoff) {
		int			x = 0;
		int			y = 0;
		String		coords = null;

		out.println("/Helvetica findfont "+(getScale()/6)+" scalefont setfont");
		for (x = 0; x < width; x++) {
			for (y=0; y < height; y++) {
				int hx = x+1+xoff;
				int hy = y+1+yoff;
				coords = ((hx<10)?"0"+hx:""+hx)+""+((hy<10)?"0"+hy:""+hy);
				plotHexagon(getX(x,y), getY(x,y), getScale());
				plotText(getX(x,y)+getScale()/5, getY(x,y)-getScale()/3, coords);
				//plotCircle(getX(x,y)+getScale()/2, (int)(getY(x,y)-getScale()/1.2), getScale()/4);
			}
		}
	}

	public static void
	main(String[] args) {
		PostScript	ps = new PostScript(new File("myps.ps"));

		ps.setTopMargin(1500);
		ps.setLeftMargin(100);
		ps.setScale(75);
		ps.plotHexMap(8, 10);
		ps.close();
    }
}

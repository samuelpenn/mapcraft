/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.test;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.builders.Tile;

/**
 * Test for mapping world surfaces as an icosohedron. This would split the
 * world into triangles (20 large triangles, each split into 16 smaller
 * triangles).
 * 
 * There are: 5 large triangles along top and bottom, and 10 on the equator.
 * Total small triangles: 320
 * 
 * @author Samuel Penn
 */
public class Icosohedron {
	/** Width of each row, in tiles. */
	private final int[] widths = { 5, 15, 25, 35, 40, 40, 40, 40, 35, 25, 15, 5 };
	private final static double  ROOT3 = Math.sqrt(3.0);
	private final static int     W = 20;
	
	/** X position of each triangle. */
	private final int[][] P = {
			{ 8, 16, 24, 32, 40 },
			{ 7, 8, 9,  15,16,17,  23,24,25, 31,32,33, 39,40,41 },
			{ 6,7,8,9,10, 14,15,16,17,18, 22,23,24,25,26, 30,31,32,33,34, 38,39,40,41,42 },
			{ 5,6,7,8,9,10,11,  13,14,15,16,17,18,19,  21,22,23,24,25,26,27,  29,30,31,32,33,34,35,  37,38,39,40,41,42,43 },
			{ 4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44 },
			{ 3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43 },
			{ 2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42 },
			{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41 },
			{ 1,2,3,4,5,6,7,  9,10,11,12,13,14,15, 17,18,19,20,21,22,23, 25,26,27,28,29,30,31, 33,34,35,36,37,38,39 },
			{ 2,3,4,5,6, 10,11,12,13,14, 18,19,20,21,22, 26,27,28,29,30, 34,35,36,37,38 },
			{ 3,4,5, 11,12,13, 19,20,21, 27,28,29, 35,36,37 },
			{ 4, 12, 20, 28, 36 }
			
	};

	/** Direction of each triangle, either up (-ve) or down (+ve). */
	private final int[][] D = {
			{ -1, -1, -1, -1, -1 },
			{ -1, 1, -1,  -1, 1, -1,  -1, 1, -1,  -1, 1, -1,  -1, 1, -1 },
			{ -1,1,-1,1,-1, -1,1,-1,1,-1, -1,1,-1,1,-1, -1,1,-1,1,-1, -1,1,-1,1,-1 },
			{ -1,1,-1,1,-1,1,-1, -1,1,-1,1,-1,1,-1, -1,1,-1,1,-1,1,-1, -1,1,-1,1,-1,1,-1, -1,1,-1,1,-1,1,-1 },
			{ -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1 },
			{ -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1 },
			{ -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1 },
			{ -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1,1,-1,1,-1,1,-1, 1, -1 },
			{ 1,-1,1,-1,1,-1,1, 1,-1,1,-1,1,-1,1, 1,-1,1,-1,1,-1,1, 1,-1,1,-1,1,-1,1, 1,-1,1,-1,1,-1,1 },
			{ 1,-1,1,-1,1, 1,-1,1,-1,1, 1,-1,1,-1,1, 1,-1,1,-1,1, 1,-1,1,-1,1, },
			{ 1, -1, 1,  1, -1, 1,  1, -1, 1,  1, -1, 1,  1, -1, 1 },
			{ 1, 1, 1, 1, 1 },
	};
	
	/**
	 * Gets the width of the world at the given y coordinate. 
	 * Y coordinate ranges from 0 (north pole) to 11 (south pole).
	 * 
	 * @param tileY		Y coordinate on map, from 0 to 11.
	 * @return
	 */
	private final int getWidthAtY(final int tileY) {
		if (tileY < 0 || tileY > 11) {
			throw new IllegalArgumentException("Y coordinate [" + tileY 
					+ "] is outside bounds 0 - 11");
		}
		return widths[tileY];
	}
	
	private final int getHeightAtX(final int tileX) {
		return 0;
	}
	
	private final Point getBase(final int tileX, final int tileY) {
		int		px = 0;
		int		py = 0;
				
		px = (P[tileY][tileX] -1) * W;		
		py = (int) (tileY * W * ROOT3);
		
		if (getDirection(tileX, tileY) > 0) {
			py -= (W * ROOT3);
		}
		if (tileY < 7) {
			//px += (W * (7 - tileY));
		} else if (tileY > 8) {
			//px += W * (tileY - 8);
		}
		
		return new Point(px, py);
	}
	
	private final int getDirection(final int tileX, final int tileY) {
		return D[tileY][tileX];
	}
	
	/**
	 * Gets the X coordinate of the tile to the West of the specified tile.
	 * This will be X-1, unless we need to wrap around the world, in which
	 * case it is the far East of the map.
	 * 
	 * @param tileX		X coordinate to check.
	 * @param tileY		Y coordinate to check.
	 * @return			X coordinate of new tile.
	 */
	private final int getWest(final int tileX, final int tileY) {
		int x = tileX - 1;
		if (x < 0) {
			x = getWidthAtY(tileY) - 1;
		}
		return x;
	}
	
	/**
	 * Gets the X coordinate of the tile to the East of the specified tile.
	 * This will be X+1, unless we need to wrap around the world, in which
	 * case it is the far West of the map.
	 * 
	 * @param tileX		X coordinate to check.
	 * @param tileY		Y coordinate to check.
	 * @return			X coordinate of new tile.
	 */
	private final int getEast(final int tileX, final int tileY) {
		int x= tileX + 1;
		if (x >= getWidthAtY(tileY)) {
			x = 0;
		}
		return x;
	}
	
	/**
	 * Gets the Y coordinate of the tile either directly to the north or
	 * directly to the south of the specified tile. Which way depends on
	 * the facing of the current tile. There is never a wrap, since tiles
	 * at the poles will always return a tile nearer the equator.
	 * 
	 * @param tileX		X coordinate to check.
	 * @param tileY		Y coordinate to check.
	 * @return			Y coordinate of new tile.
	 */
	private final Point getUpDown(final int tileX, final int tileY) {
		int d = D[tileY][tileX];
		int x = tileX;
		int y = tileY - d;
		
		if (tileY > 6) {
			// Bottom third.
			if (D[tileY][tileX] == -1) {
				int m = getWidthAtY(tileY) / 5;
				x -= (1 + (2 * (tileX / m)));
			} else if (tileY > 8) {
				int m = getWidthAtY(tileY) / 5;
				x += (1 + (2 * (tileX / m)));
			}
			System.out.println("B: "+tileY+","+y+": "+tileX+","+x);
		} else if (tileY > 3) {
			x -= D[tileY][tileX];
			System.out.println("M: "+tileY+","+y+": "+tileX+","+x);
		}
		
		return new Point(x, y);
	}
	
	private Tile[][] map = new Tile[12][];
	
	public void random() {
		Tile	light = new Tile ("Light", "#CCCCCC", false);
		Tile	dark = new Tile("Dark", "#BBBBBB", false);
		Tile	red = new Tile("Red", "#FF0000", false);
		Tile	blue = new Tile("Red", "#0000FF", false);
		Tile	green = new Tile("Red", "#00FF00", false);
		
		for (int tileY=0; tileY < 12; tileY++) {
			map[tileY] = new Tile[getWidthAtY(tileY)];
			for (int tileX = 0; tileX < getWidthAtY(tileY); tileX++) {
				if (Die.d20() == 1) {
					map[tileY][tileX] = dark;
				} else {
					map[tileY][tileX] = light;
				}
			}
		}
		map[5][0] = blue;
		map[6][19] = blue;
		map[7][2] = blue;
		map[4][14] = blue;

		for (int i=0; i < 1; i++) {
			Tile[][] tmp = new Tile[12][];
			
			for (int tileY=0; tileY < 12; tileY++) {
				tmp[tileY] = new Tile[getWidthAtY(tileY)];
				for (int tileX = 0; tileX < getWidthAtY(tileY); tileX++) {
					tmp[tileY][tileX] = map[tileY][tileX];
				}
			}

			for (int tileY=0; tileY < 12; tileY++) {
				for (int tileX = 0; tileX < getWidthAtY(tileY); tileX++) {
					if (tmp[tileY][tileX] == blue) {
						Point p = getUpDown(tileX, tileY);
						map[(int)p.getY()][(int)p.getX()] = green;						
					}
					if (blue == null) {
					if (tmp[tileY][tileX] == dark) {
						try {
							switch (Die.d3()) {
							case 1:
								// West.
								map[tileY][getWest(tileX, tileY)] = dark;
								break;
							case 2:
								// East.
								map[tileY][getEast(tileX, tileY)] = dark;
								break;
							case 3:
								// North/South.
								Point p = getUpDown(tileX, tileY);
								map[(int)p.getY()][(int)p.getX()] = dark;
								break;
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							map[tileY][tileX] = red;
							System.out.println(tileX +", "+ tileY);
						}
					}
					}
				}
			}
		}
	}
	
	
	public void draw() throws IOException {
		SimpleImage		img = new SimpleImage(881, 415, "#000000");
		
		int baseX = 0;
		int baseY = (int)(W * ROOT3);
		
		for (int tileY=0; tileY < 12; tileY++) {
			for (int tileX=0; tileX < getWidthAtY(tileY); tileX++) {
				Point	point = getBase(tileX, tileY);
				int		h = (int)(W * ROOT3 * getDirection(tileX, tileY));
				img.triangleFill(baseX + (int)point.getX(), baseY + (int)point.getY(), W, h, map[tileY][tileX].getRGB(Die.d10())); 
				img.triangle(baseX + (int)point.getX(), baseY + (int)point.getY(), W, h, map[tileY][tileX].getRGB(Die.d10())); 
			}
			if (tileY > 6) {
				//break;
			}
		}
		img.save(new File("/tmp/maps/test.jpg"));
		
		// Stretch
		BufferedImage b = img.getBufferedImage();
		System.out.println(b.getWidth() + " x " + b.getHeight());
		for (int y=0; y < b.getHeight(); y++) {
			int count = 0;
			List<Integer>  nonBlack = new ArrayList<Integer>();
			for (int x=0; x < b.getWidth(); x++) {
				if (b.getRGB(x, y) != -16777216) {
					nonBlack.add(b.getRGB(x, y));
					count++;
				}
			}
			
			int x = 0;
			double stretch = 1.0 * b.getWidth() / count;
			double total = 0;
			int	lastRgb = 0;
			for (int rgb : nonBlack) {
				lastRgb = rgb;
				total += stretch;
				int i = (int)(total);
				total -= i;
				while (i-- > 0) {
					b.setRGB(x++, y, rgb);
				}
			}
			while (x < b.getWidth()) {
				b.setRGB(x++, y, lastRgb);
			}
		}
		OutputStream out = new BufferedOutputStream(new FileOutputStream("/tmp/maps/stretch.jpg"));
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		encoder.encode(b);

	}
	
	public static void main(String[] args) throws Exception {
		Icosohedron ico = new Icosohedron();
		
		ico.random();
		ico.draw();
	}
}

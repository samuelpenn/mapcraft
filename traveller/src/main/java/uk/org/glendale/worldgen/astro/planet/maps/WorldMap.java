/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.maps;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import uk.org.glendale.graphics.SimpleImage;
import uk.org.glendale.rpg.utils.Die;

/**
 * Manages mapping of worlds.
 */
public class WorldMap {
	private Icosohedron		model = new Icosohedron();
	private Tile[][]		map;
	
	private static final Tile	OOB = new Tile("OOB", "#FFFFFF", false);
	private static final Tile	BLANK = new Tile("BLANK", "#000000", false);
	
	private Tile	light = new Tile ("Light", "#CCCCCC", false);
	private Tile	dark = new Tile("Dark", "#BBBBBB", false);
	private Tile	red = new Tile("Red", "#FF0000", false);

	public WorldMap() {
		map = new Tile[model.getTotalHeight()][];
		for (int y=0; y < model.getTotalHeight(); y++) {
			map[y] = new Tile[model.getWidthAtY(y)];
			for (int x=0; x < model.getWidthAtY(y); x++) {
				map[y][x] = BLANK;
			}
		}
	}
	
	public void generateMap() {	
		for (int tileY=0; tileY < 12; tileY++) {
			map[tileY] = new Tile[model.getWidthAtY(tileY)];
			for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
				if (Die.d20() == 1) {
					map[tileY][tileX] = dark;
				} else {
					map[tileY][tileX] = light;
				}
			}
		}

		for (int i=0; i < 4; i++) {
			Tile[][] tmp = new Tile[12][];
			
			for (int tileY=0; tileY < 12; tileY++) {
				tmp[tileY] = new Tile[model.getWidthAtY(tileY)];
				for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
					tmp[tileY][tileX] = map[tileY][tileX];
				}
			}

			for (int tileY=0; tileY < 12; tileY++) {
				for (int tileX = 0; tileX < model.getWidthAtY(tileY); tileX++) {
					if (tmp[tileY][tileX] == dark) {
						try {
							switch (Die.d3()) {
							case 1:
								// West.
								map[tileY][model.getWest(tileX, tileY)] = dark;
								break;
							case 2:
								// East.
								map[tileY][model.getEast(tileX, tileY)] = dark;
								break;
							case 3:
								// North/South.
								Point p = model.getUpDown(tileX, tileY);
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
	
	/**
	 * Used to test the neighbour logic. Given a tile, colours it blue and
	 * then colours its vertical neighbour red. Allows an easy manual check
	 * to ensure that the vertical neighbour match is working.
	 * 
	 * @param x		X coordinate of tile to look for up/down neighbour.
	 * @param y		Y coordinate of tile to look for up/down neighbour.
	 */
	@SuppressWarnings("unused")
	private void testUpDown(int x, int y) {
		Tile blue = new Tile("Blue", "#0000FF", false);
		Tile red = new Tile("Red", "#FF0000", false);
		
		map[y][x] = blue;
		map[model.getUpDown(x, y).y][model.getUpDown(x, y).x] = red;
	}
	
	private int getVariedColour(int c, int var) {
		c += Die.die(var) - Die.die(var);
		if (c < 1) {
			c = 1;
		} else if (c > 254) {
			c = 254;
		}
		return c;
	}
	
	private void blur(BufferedImage image) {
		for (int y=0; y < image.getHeight(); y++) {
			for (int x=0; x < image.getWidth(); x++) {
				int c = image.getRGB(x, y) & 0xFFFFFF;
				if (c == 0xFFFFFF) {
					continue;
				}
				int r = (c >> 16) & 0xFF;
				int g = (c >> 8) & 0xFF;
				int b = c & 0xFF;
				switch (Die.d8()) {
				case 1:
					if (y > 0) {
						c = image.getRGB(x, y-1);
					}
					break;
				case 2:
					if (y < image.getHeight() - 1) {
						c = image.getRGB(x, y+1);
					}
					break;
				case 3:
					if (x > 0) {
						c = image.getRGB(x-1, y);
					}
					break;
				case 4:
					if (x < image.getWidth() - 1) {
						c = image.getRGB(x+1, y);
					}
					break;
				default:
					break;
				}
				if ((c & 0xFFFFFF) != 0xFFFFFF) {
					if ((c & 0xFFFFFF) == 0xBBBBBB) {
						image.setRGB(x, y, c);
					}
				}
			}
		}
	}
	
	public void generate() throws IOException {
		generateMap();
		SimpleImage image = model.draw(map);
		
		BufferedImage	bi = image.getBufferedImage();
		
		for (int y=0; y < bi.getHeight(); y++) {
			for (int x=0; x < bi.getWidth(); x++) {
				int c = bi.getRGB(x, y) % 0xFFFFFF;
				if (c == -1) {
					bi.setRGB(x, y, 0xFFFFFF);
				} else {
					int r = (c >> 16) & 0xFF;
					int g = (c >> 8) & 0xFF;
					int b = c & 0xFF;
					
					/*
					r = getVariedColour(r, 8);
					g = getVariedColour(g, 8);
					b = getVariedColour(b, 8);
					//System.out.println(r);
					
					c = r * 65536 + g * 256 + b;
					
					bi.setRGB(x, y, c);
					*/
				}
			}
		}
		
		for (int i=0; i < 8; i++) {
			blur(bi);
		}
		
		image.save(new File("/tmp/maps/test.jpg"), bi);
	}
	
	public static void main(String[] args) throws Exception {
		WorldMap	map = new WorldMap();

		for (int y=0; y < map.model.getTotalHeight(); y++) {
			for (int x=0; x < map.model.getTotalWidth(); x++) {
				if (map.model.isValid(x, y)) {
					System.out.print("X");
				} else {
					System.out.print(" ");
				}
			}
			System.out.println("");
		}
		
		map.generate();
	}
}

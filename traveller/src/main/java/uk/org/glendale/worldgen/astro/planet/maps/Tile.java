/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet.maps;

/**
 * A tile represents a square section of world map. When a world is first
 * mapped, it is done so at a low resolution. Eventually each tile will be
 * scaled up using a fractal algorithm to give a more graphically appealing map,
 * but until then the low resolution tiles are used to store terrain and
 * ecological information about the world's surface.
 * 
 * @author Samuel Penn
 */
public class Tile {
	private String name;
	private String rgb;
	private boolean isWater;

	public Tile(final String name, final String rgb, final boolean isWater) {
		this.name = name;
		this.rgb = rgb;
		this.isWater = isWater;
	}

	public final String getName() {
		return name;
	}

	public final String getRGB() {
		return rgb;
	}
	
	public final void setRGB(final String rgb) {
		this.rgb = rgb;
	}

	private final String getHex(int v) {
		if (v > 255)
			v = 255;
		if (v < 0)
			v = 0;

		return ((v < 16) ? "0" : "") + Integer.toHexString(v);
	}

	public final String getRGB(final int modifier) {
		int r = Integer.parseInt(rgb.substring(1, 3), 16) + modifier;
		int g = Integer.parseInt(rgb.substring(3, 5), 16) + modifier;
		int b = Integer.parseInt(rgb.substring(5, 7), 16) + modifier;

		return "#" + getHex(r) + getHex(g) + getHex(b);
	}

	public final boolean isWater() {
		return isWater;
	}
	
	public final String toString() {
		return rgb;
	}
}

/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.sector;

/**
 * Enumerates the possible sub sectors. Each sector is divided into sixteen sub
 * sectors, named A - P. A sector is 32 x 40 parsecs in size, and each sub
 * sector is therefore 8 x 10 parsecs in size. They are named A - D along the
 * 'top' row, E - H on the 2nd row etc.
 * 
 * @author Samuel Penn
 */
public enum SubSector {
	A(0, 0), B(1, 0), C(2, 0), D(3, 0), E(0, 1), F(1, 1), G(2, 1), H(3, 1), 
	I(0, 2), J(1, 2), K(2, 2), L(3, 2), M(0, 3), N(1, 3), O(2, 3), P(3, 3);

	/** X position of sub-sector, 0-3. */
	private int x = 0;
	/** Y position of sub-sector, 0-3. */
	private int y = 0;

	private SubSector(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Gets the X position of this sub sector in the 4x4 grid.
	 * 0,0 is top left of the sector, 3,3 is bottom right.
	 * 
	 * @return The X position, 0 - 3.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Gets the Y position of this sub sector in the 4x4 grid.
	 * 0,0 is top left of the sector, 3,3 is bottom right.
	 * 
	 * @return The Y position, 0 - 3.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Given a star's coordinates in a sector, gets the sub-sector to which the
	 * star belongs. Star coordinates are 1-32 for X, 1-40 for Y. If the star is
	 * outside the boundaries of the sector, then null is returned.
	 * 
	 * @param x
	 *            X coordinate of the star, 1-32.
	 * @param y
	 *            Y coordinate of the star, 1-40.
	 * @return Sub sector in which the star resides.
	 */
	public static SubSector getSubSector(int x, int y) {
		x = (x - 1) / 8;
		y = (y - 1) / 10;

		for (SubSector ss : values()) {
			if (ss.x == x && ss.y == y) {
				return ss;
			}
		}
		return null;
	}
}

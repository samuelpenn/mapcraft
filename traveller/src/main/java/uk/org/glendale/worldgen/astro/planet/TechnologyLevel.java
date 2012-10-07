/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

/**
 * Rough guide to the level of technology desired. Breaks the levels down
 * into groups of three.
 * 
 * @author Samuel Penn
 */
public enum TechnologyLevel {
	// TL 0 - 2
	Primitive,
	// TL 3 - 5
	LowTech,
	// TL 6 - 8
	HighTech,
	// TL 9 - 11
	Interplanetary,
	// TL 12+
	Interstellar;
}

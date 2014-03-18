/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

/**
 * Defines a population size class, to two orders of magnitude. Used when
 * creating worlds to roughly define how populated the world is.
 *  
 * @author Samuel Penn
 */
public enum PopulationSize {
	/** No population at all. */
	None,
	/** Up to 100 people. */
	Tiny,
	/** Up to 10,000 people. */
	Small,
	/** Up to 1 million people. */
	Medium,
	/** Up to 100 million people. */
	Large,
	/** Up to 10 billion people. A full civilisation. */
	Huge,
	/** More than 10 billion people. City or Hive worlds. */
	Gigantic
}

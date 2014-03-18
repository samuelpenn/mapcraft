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
 * Codes given to each sector. These may be used when generating star systems in
 * the worlds, or at other times as meta data.
 * 
 * @author Samuel Penn
 */
public enum SectorCode {
	/** Core sector, based on official data. */
	Co,
	/** Barren sector, very few life bearing worlds. */
	Ba,
	/** Fertile sector, lots of Earth like worlds. */
	Fe,
	/** Low level of colonisation. */
	Lo,
	/** High level of colonisation. */
	Hi,
	/** Sparse sectors with few planets. */
	Sp;
}

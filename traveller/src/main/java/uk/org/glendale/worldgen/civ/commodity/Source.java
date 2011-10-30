/*
 * Copyright (C) 2008 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

/**
 * Defines the different sources for commodities.
 * Mi: Minerals, obtained raw from the environment.
 * Ag: Agricultural produce, grown and farmed.
 * In: Industrial goods, made in factories.
 * Kn: Knowledge goods, information and entertainment.
 * 
 * @author Samuel Penn
 */
public enum Source {
	/** Agricultural produce, grown and farmed. */
	Ag,
	/** Industrial goods, made in factories. */
	In,
	/** Minerals, obtained raw from the environment. */
	Mi,
	/** Knowledge goods, information and entertainment. */
	Kn,
	/** Residential goods, produced in the home or by simple labour. */
	Re
}

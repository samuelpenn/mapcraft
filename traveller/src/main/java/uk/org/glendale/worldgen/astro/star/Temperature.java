/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.4 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.worldgen.astro.star;

/**
 * Describes the surface temperature of the planet. Standard temperature is a
 * typical Earth-like world.
 * 
 * ExtremelyCold: Temperature rarely above 100K
 * VeryCold: Too cold for humans. Temperature rarely above 200K
 * Cold: Winter world, similar to Arctic conditions all over. Special life support
 *       is needed for a colony to survive. Rarely above 250K
 * Cool: Uncomfortable, long winters and short summers. Humans can survive
 *       without special life support.
 * Standard: Earth
 * Warm: Tropical climate, hot all over. Really nice to uncomfortable.
 * Hot: Too hot, difficult to survive. Rarely below 320K.
 * VeryHot: Rarely below 400K. Water boils. Humans cannot live.
 * ExtremelyHot: Rock melts. 
 *
 */
public enum Temperature {
	UltraCold(0.0, 50, 20),
	ExtremelyCold(0.0, 100, 15), 
	VeryCold(0.0, 200, 12), 
	Cold(0.1, 250, 5), 
	Cool(0.5, 273, 2), 
	Standard(1.0, 293, 0), 
	Warm(0.75, 310, 2), 
	Hot(0.1, 330, 10), 
	VeryHot(0.0, 360, 50), 
	ExtremelyHot(0.0, 400, 100),
	UltraHot(0.0, 600, 150);
	
	double	suitability = 1.0;
	int		kelvin = 300;
	int		badness = 0;
	
	Temperature(double suitability, int kelvin, int badness) {
		this.suitability = suitability;
		this.kelvin = kelvin;
		this.badness = badness;
	}
	
	public int getBadness() {
		return badness;
	}
	
	public double getSuitability() {
		return suitability;
	}
	
	public boolean isColderThan(Temperature otherTemperature) {
		return ordinal() < otherTemperature.ordinal();
	}

	public boolean isHotterThan(Temperature otherTemperature) {
		return ordinal() > otherTemperature.ordinal();
	}
	
	/**
	 * Get a temperature that is one level hotter than the current one.
	 * If the temperature is already ExtremelyHot, then ExtremelyHot is
	 * returned.
	 */
	public Temperature getHotter() {
		if (this == UltraHot) return UltraHot;
		
		return Temperature.values()[ordinal()+1];
	}

	/**
	 * Get a temperature that is one level colder than the current one.
	 * If the temperature is already ExtremelyCold, then ExtremelyCold
	 * is returned.
	 */
	public Temperature getColder() {
		if (this == UltraCold) return UltraCold;
		
		return Temperature.values()[ordinal()-1];
	}
}
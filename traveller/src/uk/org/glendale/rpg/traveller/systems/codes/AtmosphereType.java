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
package uk.org.glendale.rpg.traveller.systems.codes;

public enum AtmosphereType {
	Vacuum(0.0, 1.0), 
	Standard(1.0, 1.0), 
	Chlorine(0.0, 1.0), 
	Flourine(0.0, 1.0), 
	SulphurCompounds(0.0, 0.9), 
	NitrogenCompounds(0.0, 0.8), 
	OrganicToxins(0.5, 1.0),
	LowOxygen(0.75, 1.0), 
	Pollutants(0.5, 1.0), 
	HighCarbonDioxide(0.25, 0.75), 
	HighOxygen(1.0, 1.0), 
	InertGases(0.0, 1.0), 
	Hydrogen(0.0, 1.0), 
	Primordial(0.0, 0.9),
	WaterVapour(0.0, 0.8), 
	CarbonDioxide(0.0, 0.6), 
	Tainted(0.75, 1.0),
	Exotic(0.0, 1.0);
	
	double 	suitability = 0.0;
	double  greenhouse = 1.0;
	
	AtmosphereType(double suitability, double greenhouse) {
		this.suitability = suitability;
		this.greenhouse = greenhouse;
	}
	
	/**
	 * Get the suitability of this atmosphere for humans. This is on a scale
	 * of 0 to 1 or more, with 1.0 being Earth normal.
	 * 
	 * @return		Suitability of the world for human life.
	 */
	public double getSuitability() {
		return suitability;
	}
	
	public boolean isGaian() {
		return (suitability > 0.1);
	}
	
	/**
	 * Get the greenhouse factor for this atmosphere. This modifies the
	 * effective distance of the planet from the star, so values less
	 * than 1.0 make the planet warmer.
	 * 
	 * @return		Greenhouse modifier.
	 */
	public double getGreenhouse() {
		return greenhouse;
	}
}
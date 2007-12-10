/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems.codes;

public enum AtmospherePressure {
	None(1.0, 0.0), 
	Trace(1.0, 0.0), 
	VeryThin(0.9, 0.01), 
	Thin(0.8, 0.5), 
	Standard(0.7, 1.0), 
	Dense(0.6, 0.9), 
	VeryDense(0.5, 0.75), 
	SuperDense(0.4, 0.5);
	
	private double	distance = 1.0;
	private double  suitability = 1.0;
	
	AtmospherePressure(double distance, double suitability) {
		this.distance = distance;
		this.suitability = suitability;
	}
	
	/**
	 * Get the effective stellar distance modifier. A thick atmosphere
	 * retains heat, meaning the world is warmer as if it were closer to
	 * the star.
	 */
	public double getEffectiveDistance() {
		return distance;
	}
	
	public double getSuitability() {
		return suitability;
	}
	
	/**
	 * Get an atmosphere one level denser than this one. If already
	 * at SuperDense, simply returns SuperDense.
	 */
	public AtmospherePressure getDenser() {
		if (this == SuperDense) {
			// Cannot get any denser.
			return SuperDense;
		} else {
			return values()[ordinal()+1];
		}
	}

	/**
	 * Get an atmosphere one level thinner than this one. If
	 * pressure is already None, then returns None.
	 */
	public AtmospherePressure getThinner() {
		if (this == None) {
			return None;
		} else {
			return values()[ordinal()-1];
		}
	}
	
	public boolean isThinnerThan(AtmospherePressure pressure) {
		if (pressure != null && (ordinal() < pressure.ordinal())) {
			return true;
		}
		
		return false;
	}
	
	public boolean isDenserThan(AtmospherePressure pressure) {
		if (pressure != null && (ordinal() > pressure.ordinal())) {
			return true;
		}
		
		return false;
	}
}
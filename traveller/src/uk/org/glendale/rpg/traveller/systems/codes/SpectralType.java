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

public enum SpectralType  {
	M9, M8, M7, M6, M5, M4, M3, M2, M1, M0,
	K9, K8, K7, K6, K5, K4, K3, K2, K1, K0,
	G9, G8, G7, G6, G5, G4, G3, G2, G1, G0,
	F9, F8, F7, F6, F5, F4, F3, F2, F1, F0,
	A9, A8, A7, A6, A5, A4, A3, A2, A1, A0,
	B9, B8, B7, B6, B5, B4, B3, B2, B1, B0,
	O9, O8, O7, O6, O5, O4, O3, O2, O1, O0;
	
	public String getRGBColour() {
		String		rgb = "0 0 0";
		
		if (compareTo(A0) > 0) {
			// Blue.
			rgb = "0 1 1";
		} else if (compareTo(A0) > 0) {
			// Green-Blue.
			rgb = "0 1 0.5";
		} else if (compareTo(G0) > 0) {
			// Yellow.
			rgb = "0.8 1 0";
		} else if (compareTo(K0) > 0) {
			// Orange
			rgb = "1 1 0";
		} else if (compareTo(M0) > 0) {
			// Red-Orange.
			rgb = "1 0.7 0";
		} else {
			// Red.
			rgb = "1 0 0";
		}

		return rgb;
	}

	/**
	 * Get the surface temperature of the star.
	 */
	public int getSurfaceTemperature() {
		return 1500 + ordinal() * 150;
	}
	
	/** 
	 * Get the mass of the star, relative to Sol.
	 */
	public double getMass() {
		double		mass = 1.0;
		
		if (toString().startsWith("M")) {
			mass = 0.25;
		} else if (toString().startsWith("K")) {
			mass = 0.7;
		} else if (toString().startsWith("G")) {
			mass = 1.0;
		} else if (toString().startsWith("F")) {
			mass = 1.4;
		} else if (toString().startsWith("A")) {
			mass = 2.0;
		} else if (toString().startsWith("B")) {
			mass = 10.0;
		} else if (toString().startsWith("O")) {
			mass = 50.0;
		}
		
		return mass;
	}
	
	/**
	 * Get the life time of the star, in billions of years.
	 */
	public double getLifeTime() {
		double		billions = 0.0;
		
		if (toString().startsWith("M")) {
			billions = 100;
		} else if (toString().startsWith("K")) {
			billions = 21;
		} else if (toString().startsWith("G")) {
			billions = 12;
		} else if (toString().startsWith("F")) {
			billions = 3.0;
		} else if (toString().startsWith("A")) {
			billions = 1.0;
		} else if (toString().startsWith("B")) {
			billions = 0.2;
		} else if (toString().startsWith("O")) {
			billions = 0.005;
		}

		return billions;
	}
	
}
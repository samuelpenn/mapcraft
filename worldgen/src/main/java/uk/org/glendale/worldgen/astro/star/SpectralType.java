/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

/**
 * Defines the spectral type of the star, using the Hertzsprung-Russell diagram.
 * This is a measure of the surface temperature (and hence, colour) of the star,
 * with 'Y' stars being coolest and 'O' stars the hottest. The digit provides
 * further grading, with '0' being hottest and '9' being coolest.
 * 
 * Spectral types L, T and Y are reserved for brown dwarfs (Y are theoretical).
 * 'D' stars are White Dwarfs, and use a somewhat different categorisation. Type
 * 'X' stars are X-ray sources, such as neutron stars and black holes. This
 * latter type are completely unofficial, since I can't find the right way to
 * classify these star types.
 * 
 * @author Samuel Penn
 */
public enum SpectralType {
	// < 700K (infrared), sub-brown dwarfs
	Y9, Y8, Y7, Y6, Y5, Y4, Y3, Y2, Y1, Y0,
	// 700 - 1,300 K (infrared), methane dwarfs
	T9, T8, T7, T6, T5, T4, T3, T2, T1, T0,
	// 1,300 - 2,000 K (dark red), dwarfs
	L9, L8, L7, L6, L5, L4, L3, L2, L1, L0,
	// 2,000 - 3,700 K (red)
	M9, M8, M7, M6, M5, M4, M3, M2, M1, M0,
	// 3,700 - 5,200 K (orange)
	K9, K8, K7, K6, K5, K4, K3, K2, K1, K0,
	// 5,200 - 6000 K (yellow)
	G9, G8, G7, G6, G5, G4, G3, G2, G1, G0,
	// 6,000 - 7,500 K (yellowish white)
	F9, F8, F7, F6, F5, F4, F3, F2, F1, F0,
	// 7,500 - 10,000 K (white)
	A9, A8, A7, A6, A5, A4, A3, A2, A1, A0,
	// 10,000 - 30,000 K (blue to blue white)
	B9, B8, B7, B6, B5, B4, B3, B2, B1, B0,
	// > 33,000 K (white)
	O9, O8, O7, O6, O5, O4, O3, O2, O1, O0,
	// White Dwarfs
	D9, D8, D7, D6, D5, D4, D3, D2, D1, D0,
	// Neutron Stars and Black Holes
	X9, X8, X7, X6, X5, X4, X3, X2, X1, X0;

	private char getFirst() {
		return toString().charAt(0);
	}

	private int getDigit() {
		return Integer.parseInt("" + toString().charAt(1));
	}

	private int getInverseDigit() {
		return 9 - Integer.parseInt("" + toString().charAt(1));
	}

	public String getRGBColour() {
		String rgb = "0 0 0";

		switch (getFirst()) {
		case 'D':
			rgb = "1.0 1.0 1.0";
			break;
		case 'X':
			rgb = "1.0 1.0 1.0";
			break;
		case 'Y':
			rgb = "0.3 0.0 0.0";
			break;
		case 'T':
			rgb = "0.5 0.0 0.0";
			break;
		case 'L':
			rgb = "0.7 0.0 0.0";
			break;
		case 'M':
			rgb = "0.9 0.0 0.0";
			break;
		case 'K':
			rgb = "1.0 0.5 0.0";
			break;
		case 'G':
			rgb = "0.8 1.0 0.0";
			break;
		case 'F':
			rgb = "0.5 1.0 0.2";
			break;
		case 'A':
			rgb = "0.0 1.0 0.5";
			break;
		case 'B':
			rgb = "0.0 1.0 0.7";
			break;
		case 'O':
			rgb = "0.5 1.0 1.0";
			break;
		}

		return rgb;
	}

	/**
	 * Get the surface temperature of the star.
	 */
	public int getSurfaceTemperature() {
		int k = 0;
		switch (getFirst()) {
		case 'D':
			// White Dwarfs.
			k = 1000 + getInverseDigit() * getInverseDigit() * 1500;
			break;
		case 'X':
			// X-Ray remnants, Neutron Stars and Black Holes.
			k = 1000 + getInverseDigit() * getInverseDigit()
					* getInverseDigit() * 2000;
			break;
		case 'Y':
			// Ultra-cool brown dwarfs.
			k = 300 + getInverseDigit() * 60;
			break;
		case 'T':
			// Cool brown dwarfs (Methan Dwarfs)
			k = 600 + getInverseDigit() * 70;
			break;
		case 'L':
			// Cool dwarfs
			k = 1300 + getInverseDigit() * 70;
			break;
		case 'M':
			k = 2000 + getInverseDigit() * 170;
			break;
		case 'K':
			k = 3700 + getInverseDigit() * 150;
			break;
		case 'G':
			k = 5200 + getInverseDigit() * 80;
			break;
		case 'F':
			k = 6000 + getInverseDigit() * 150;
			break;
		case 'A':
			k = 7500 + getInverseDigit() * 250;
			break;
		case 'B':
			k = 10000 + getInverseDigit() * 2000;
			break;
		case 'O':
			k = 30000 + getInverseDigit() * 5000;
			break;
		}

		return k;
	}

	/**
	 * Get the mass of the star, relative to Sol.
	 */
	public double getMass() {
		double mass = 1.0;

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
		double billions = 0.0;

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
		} else if (toString().startsWith("Y")) {
			billions = 800;
		} else if (toString().startsWith("T")) {
			billions = 400;
		} else if (toString().startsWith("L")) {
			billions = 200;
		} else if (toString().startsWith("D")) {
			billions = 100;
		} else if (toString().startsWith("X")) {
			billions = 1000;
		}

		return billions;
	}

}
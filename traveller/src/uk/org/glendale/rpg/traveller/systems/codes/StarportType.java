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

/**
 * Starport classification for a world. This uses the standard Traveller
 * classificatins, from A (best) to E (worst) and X as no starport at all.
 * 
 * The type of starport may modify the World Trade Number, and the types
 * define how this modification happens. Modifiers are stored in an array,
 * with the modifier for an initial WTN of 0 to 7. These modifiers are
 * from GURPS Free Trader.
 * 
 * Starports also have a minimum Tech Level, which is required to support
 * a starport of the given type.
 * 
 * @author Samuel Penn
 */
public enum StarportType {
	A(10, new double[] { 1.5, 1, 1, 0.5, 0.5, 0, 0, 0 }),
	B(9, new double[] { 1, 1, 0.5, 0.5, 0, 0, -0.5, -1}),
	C(8, new double[] { 1, 0.5, 0.5, 0, 0, -0.5, -1, -1.5}), 
	D(7, new double[] { 0.5, 0.5, 0, 0, -0.5, -1, -1.5, -2}),
	E(5, new double[] { 0.5, 0, 0, -0.5, -1, -1.5, -2, -2.5}),
	X(0, new double[] { 0, 0, -2.5, -3, -3.5, -4, -4.5, -5});
	
	int			minTechLevel = 0;
	double[]	wtnModifier = null;
	
	StarportType(int minTechLevel, double[] wtn) {
		this.minTechLevel = minTechLevel;
		this.wtnModifier = wtn;
	}
	
	public int getMinimumTechLevel() {
		return minTechLevel;
	}
	
	/**
	 * Given a World Trade Number, returns  the modified WTN depending
	 * on this type of starport. Small starports tend to heavily modify
	 * large WTNs downwards, but have a smaller (or even beneficial)
	 * effect on small WTNs.
	 */
	public double getModifiedWTN(double wtn) {
		int		i = (int)wtn;
		if (i >= wtnModifier.length) {
			i = wtnModifier.length - 1;
		}
		if (i < 0) {
			i = 0;
		}
		return wtn + wtnModifier[i];
	}
	
	public StarportType getBetter() {
		switch (this) {
		case A: return A;
		case B: return A;
		case C: return B;
		case D: return C;
		case E: return D;
		case X: return E;
		}
		
		return E;
	}
	
	public StarportType getWorse() {
		switch (this) {
		case A: return B;
		case B: return C;
		case C: return D;
		case D: return E;
		case E: return X;
		}
		
		return X;
	}
}

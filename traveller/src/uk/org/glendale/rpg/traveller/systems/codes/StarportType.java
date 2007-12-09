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

public enum StarportType {
	A(10),
	B(9), 
	C(8), 
	D(7),
	E(5),
	X(0);
	
	int		minTechLevel = 0;
	
	StarportType(int minTechLevel) {
		this.minTechLevel = minTechLevel;
	}
	
	public int getMinimumTechLevel() {
		return minTechLevel;
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

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

import uk.org.glendale.rpg.utils.Die;

/**
 * The star class, which is the size of the star. This provides a finer
 * grained indication to the type of star, and can be used to determine
 * how big to draw it on the star map.
 */
public enum StarClass { 
	BH ("Black Hole", 0.2, 0.0000001), 
	N ("Neutron Star", 0.3, 0.000001), 
	D ("White Dwarf", 0.5, 0.001),
	VI ("Sub Dwarf", 0.7, 0.5), 
	V ("Main Sequence", 1.0, 1.0), 
	IV ("Sub Giant", 1.1, 2.0), 
	III ("Giant", 1.2, 10), 
	II ("Large Giant", 1.3, 25), 
	Ib ("Super Giant", 1.4, 50),
	Ia ("Super Giant", 1.5, 100);
	
	private String		name = null;
	private double		size = 0.0;
	private double		radius = 0;
	
	StarClass(String name, double size, double radius) {
		this.name = name;
		this.size = size;
		this.radius = radius;
	}
	
	public double getSize() {
		return size;
	}
	
	/**
	 * Get radius of star, in multiple's of Sol.
	 */
	public double getRadius() {
		return radius;
	}
	
	public String getDescription() {
		return name;
	}
	
	public boolean isBiggerThan(StarClass compareTo) {
		if (ordinal() > compareTo.ordinal()) {
			return true;
		}
		
		return false;
	}

	public boolean isSmallerThan(StarClass compareTo) {
		if (ordinal() < compareTo.ordinal()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Get a star class which is smaller than this one. Will never return
	 * something smaller than a White Dwarf. This is primarily used to
	 * determine a companion of this star.
	 * 
	 * @return		A smaller type of star.
	 */
	public StarClass getCompanionStar() {
		StarClass		sc = null;
		
		switch (this) {
		case BH: case N: case D: case VI:
			sc = D;
			break;
		case V:
			sc = VI;
			break;
		case IV: case III:
			sc = V;
			break;
		case II: case Ib: case Ia:
			sc = III;
			break;
		}
		
		return sc;
	}
	
	/**
	 * Get a suitable spectral type for a star of this class. The spectral
	 * type returned is random, and is weighted according to the star class.
	 * 
	 * @return		Random spectral type.
	 */
	public SpectralType getSpectralType() {
		SpectralType		type = SpectralType.M0;
		SpectralType[]      values = SpectralType.values();
		
		switch (this) {
		case BH:
			// Radiate in X-ray frequencies.
			type = SpectralType.valueOf("X"+Die.d6()+3);
			break;
		case N:
			// Radiate in X-ray frequencies.
			type = SpectralType.valueOf("X"+Die.d4()+5);
			break;
		case D:
			// Surprisingly hot, actually. These are white dwarfs.
			type = SpectralType.valueOf("D"+Die.d8()+1);
			break;
		case VI:
			// Sub-dwarf stars, most will be cool, of type M or L.
			switch (Die.d6()) {
			case 1: case 2: case 3: case 4:
				type = SpectralType.valueOf("M"+(Die.d10()-1));
				break;
			case 5: case 6:
				type = SpectralType.valueOf("L"+(Die.d10()-1));
				break;
			}
			break;
		case V:
			// Wide range, average around G.
			switch (Die.d6()) {
			case 1: case 2:
				type = SpectralType.valueOf("F"+(Die.d10()-1));
				break;
			case 3: case 4:
				type = SpectralType.valueOf("G"+(Die.d10()-1));
				break;
			case 5: case 6:
				type = SpectralType.valueOf("K"+(Die.d10()-1));
				break;
			}
			break;
		case IV:
			// Sub giant stars.
			switch (Die.d6(2)) {
			case 2: case 3: case 4:
				type = SpectralType.valueOf("K"+(Die.d6()+3));
				break;
			case 5: case 6: case 7: case 8:
				type = SpectralType.valueOf("M"+(Die.d10()-1));
				break;
			case 9: case 10: case 11:
				type = SpectralType.valueOf("F"+(Die.d10()-1));
				break;
			case 12:
				type = SpectralType.valueOf("A"+(Die.d6()+3));
				break;
			}
			break;
		case III:
			// Giant stars. May be red-giants, or hot giants.
			switch (Die.d6(2)) {
			case 2: case 3: case 4: case 5:
				type = SpectralType.valueOf("M"+(Die.d10()-1));
				break;
			case 6: case 7: case 8: case 9:
				type = SpectralType.valueOf("F"+(Die.d6()-1));
				break;
			case 10: case 11:
				type = SpectralType.valueOf("A"+(Die.d10()-1));
				break;
			case 12:
				type = SpectralType.valueOf("B"+(Die.d6()+3));
				break;
			}
			break;
		case II:
			// Large (bright) giant stars
			switch (Die.d6(2)) {
			case 2: case 3: case 4: case 5:
			case 6: case 7: case 8: case 9:
				type = SpectralType.valueOf("M"+(Die.d10()-1));
				break;
			case 10: case 11:
				type = SpectralType.valueOf("A"+(Die.d4()-1));
				break;
			case 12:
				type = SpectralType.valueOf("B"+(Die.d10()-1));
				break;
			}
			break;
		case Ib:
			type = SpectralType.valueOf("B"+(Die.d10()-1));
			break;
		case Ia:
			type = SpectralType.valueOf("O"+(Die.d10()-1));
			type = values[Die.d10(2) + 50];
			break;
		}
		
		return type;
	}
}
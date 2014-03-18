/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems.codes;

/**
 * Defines the evolutionary stage of life on a planet.
 * 
 * Proteins: Simple replicators.
 * Protozoa: Single celled organisms
 * Metazoa: Multi-celled organisms.
 * SimpleOcean: Animal and plant life
 * ComplexOcean: Fish
 * SimpleLand: Insects, moss, simple plants.
 * ComplexLand: Early tetrapods and forests.
 * Extensive: Earth-like.
 * 
 * @see http://mapcraft.glendale.org.uk/worldgen/planets/lifelevel
 * 
 * @author Samuel Penn
 */
public enum LifeType {
	None(5), 
	Organic(5), 
	Archaean(5), 
	Aerobic(5), 
	ComplexOcean(3), 
	SimpleLand(2), 
	ComplexLand(1), 
	Extensive(0);
	
	private int badness = 0;
	LifeType(int badness) {
		this.badness = badness;
	}
	
	public int getBadness() {
		return badness;
	}
	
	/**
	 * True iff this type of life is simpler than the one passed.
	 */
	public boolean isSimplerThan(LifeType type) {
		return type.ordinal() > ordinal();
	}

	/**
	 * True iff this type of life is more complex than the one passed.
	 */
	public boolean isMoreComplexThan(LifeType type) {
		return type.ordinal() < ordinal();
	}
}
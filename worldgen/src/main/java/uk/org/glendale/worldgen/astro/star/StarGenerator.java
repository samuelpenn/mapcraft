/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;

/**
 * Generates stars for a star system. Created stars are not persisted, this is
 * left to the StarSystemGenerator to handle as part of system creation.
 * 
 * @author Samuel Penn
 */
public class StarGenerator {
	private StarSystem system;
	private boolean multipleStars;
	private Star primary, secondary, tertiary;

	public StarGenerator(StarSystem system, boolean multipleStars) {
		this.system = system;
		this.multipleStars = multipleStars;
	}

	/**
	 * Generate a specific type of star as the primary.
	 * 
	 * @param form
	 *            Form of the star, normally 'Star'
	 * @param classification
	 *            The general size of the star.
	 * @param type
	 *            Spectral type.
	 * @return
	 */
	public Star generatePrimary(StarForm form, StarClass classification,
			SpectralType type) {
		primary = new Star();
		primary.setSystem(system);
		primary.setName(system.getName() + ((multipleStars) ? " Alpha" : ""));
		primary.setForm(form);
		primary.setClassification(classification);
		primary.setSpectralType(type);

		return primary;
	}
	
	/**
	 * Generate a primary star for a simple star system. A simple system
	 * is very boring, so always has a single Class V star.
	 * 
	 * @return	A suitable, random class V star.
	 */
	public Star generateSimplePrimary() {
		primary = new Star();
		primary.setSystem(system);
		primary.setName(system.getName() + ((multipleStars) ? " Alpha" : ""));

		primary.setForm(StarForm.Star);
		primary.setClassification(StarClass.V);
		primary.setSpectralType(StarClass.V.getSpectralType());

		return primary;
	}

	public Star generatePrimary() {
		primary = new Star();
		primary.setSystem(system);
		primary.setName(system.getName() + ((multipleStars) ? " Alpha" : ""));

		StarClass starClass = null;

		primary.setForm(StarForm.Star);
		// Select the general class of the star. Smaller numbers
		// are larger stars.
		switch (Die.d6(3)) {
		case 3:
			starClass = StarClass.II;
			break;
		case 4:
		case 5:
			starClass = StarClass.III;
			break;
		case 6:
		case 7:
			starClass = StarClass.IV;
			break;
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
		case 13:
			starClass = StarClass.V;
			break;
		case 14:
		case 15:
		case 16:
		case 17:
		case 18:
			starClass = StarClass.VI;
			break;
		}
		primary.setClassification(starClass);
		primary.setSpectralType(starClass.getSpectralType());

		return primary;
	}

	public Star generateSecondary() {
		if (!multipleStars) {
			throw new IllegalStateException("This system has only one star");
		}
		if (primary == null || primary.getId() == 0) {
			throw new IllegalStateException("Primary star has not been defined");
		}
		secondary = new Star();
		secondary.setSystem(system);
		secondary.setName(system.getName() + " Beta");

		secondary.setForm(StarForm.Star);
		secondary.setClassification(StarClass.VI);
		secondary.setSpectralType(SpectralType.valueOf("M" + (Die.d10() - 1)));

		// This is a place holder value.
		secondary.setParentId(primary.getId());
		secondary.setDistance(Die.d10(10) * 10000);

		return secondary;
	}

	public Star generateTertiary() {
		if (!multipleStars) {
			throw new IllegalStateException("This system has only one star");
		}
		if (secondary == null || secondary.getId() == 0) {
			throw new IllegalStateException(
					"Secondary star has not been defined");
		}
		tertiary = new Star();
		tertiary.setSystem(system);
		tertiary.setName(system.getName() + " Gamma");

		tertiary.setForm(StarForm.WhiteDwarf);
		tertiary.setClassification(StarClass.D);
		tertiary.setSpectralType(SpectralType.D7);

		tertiary.setParentId(secondary.getId());
		tertiary.setDistance(Die.d10(5) * 1000);

		return tertiary;
	}
}

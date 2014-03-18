/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;


public class StarAPI {
	/**
	 * Get the heat output of this star relative to Sol. This is based on the
	 * surface temperate and surface area of the star. Used to calculate the
	 * habitable zones of the system.
	 * 
	 * @return Relative heat output of star.
	 */
	private static double getSolarConstant(Star star) {
		double constant = 1.0;

		constant = (1.0 * star.getSpectralType().getSurfaceTemperature() / SpectralType.G2
				.getSurfaceTemperature());
		constant *= Math.pow(star.getClassification().getRadius(), 2.0);

		return constant;
	}

	/**
	 * Get the innermost distance at which planets are likely to be found. Any
	 * orbits closer than this are likely to be too hot to allow planetary
	 * formation.
	 * 
	 * @return Distance in millions of km.
	 */
	public static int getInnerLimit(Star star) {
		return (int) (30 * getSolarConstant(star));
	}

	/**
	 * Get the optimal distance for an Earth-like world. Much closer than this,
	 * and the temperature is too warm, much further out and the temperature
	 * will be too cold. Note that the world's atmosphere will also affect the
	 * temperature of the world.
	 * 
	 * @return Distance in millions of km.
	 */
	public static int getEarthDistance(Star star) {
		return (int) (150 * getSolarConstant(star));
	}

	/**
	 * Worlds beyond this distance are likely to be cold ice worlds.
	 * 
	 * @return Distance in millions of km.
	 */
	public static int getColdPoint(Star star) {
		return (int) (1000 * getSolarConstant(star));
	}

	/**
	 * Get the typical temperature for a planet at the given orbital distance.
	 * The temperature is returned as relative to Earth-standard. This assumes a
	 * planet without an atmosphere. Planets with atmospheres will be somewhat
	 * warmer.
	 * 
	 * @param distance
	 *            Distance from the star, in Mkm.
	 * @return Temperature.
	 */
	public static Temperature getOrbitTemperature(Star star, int distance) {
		Temperature temperature = Temperature.Standard;
		double constant = getSolarConstant(star);

		if (distance < 15 * constant) {
			temperature = Temperature.UltraHot;
		} else if (distance < 25 * constant) {
			temperature = Temperature.ExtremelyHot;
		} else if (distance < 50 * constant) {
			temperature = Temperature.VeryHot;
		} else if (distance < 75 * constant) {
			temperature = Temperature.Hot;
		} else if (distance < 100 * constant) {
			temperature = Temperature.Warm;
		} else if (distance < 120 * constant) {
			temperature = Temperature.Standard;
		} else if (distance < 200 * constant) {
			temperature = Temperature.Cool;
		} else if (distance < 500 * constant) {
			temperature = Temperature.Cold;
		} else if (distance < 2000 * constant) {
			temperature = Temperature.VeryCold;
		} else if (distance < 8000 * constant) {
			temperature = Temperature.ExtremelyCold;
		} else {
			temperature = Temperature.UltraCold;
		}

		return temperature;
	}

	/**
	 * Get the orbital period of a given orbit.
	 * 
	 * @param distance
	 *            Distance in millions of km.
	 * @return Period, in seconds.
	 */
	public static long getOrbitPeriod(Star star, int distance) {
		long seconds = 0;
		double mass = star.getSpectralType().getMass(); // Relative to the mass
														// of our sun.
		double a = Math.pow(distance / 150, 3);
		double years = 0;

		years = Math.sqrt(a / mass);
		seconds = (long) (years * 365.25 * 86400);

		return seconds;
	}

}

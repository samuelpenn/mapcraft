/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import junit.framework.Assert;

import org.junit.Test;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.PlanetFeature;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.worldgen.astro.star.Temperature;

/**
 * Tests the planet entity.
 * 
 * @author Samuel Penn
 */
public class PlanetTest {
	@Test
	public void testPlanet() {
		Planet planet = new Planet();

		Assert.assertEquals(0, planet.getId());
		Assert.assertFalse(planet.isMoon());

		planet.setDayLength(1);
		Assert.assertEquals(1, planet.getDayLength());

		planet.setDistance(1);
		Assert.assertEquals(1, planet.getDistance());

		planet.setAxialTilt(1);
		Assert.assertEquals(1, planet.getAxialTilt());

		planet.setDescription("Test");
		Assert.assertEquals("Test", planet.getDescription());

		planet.setTechLevel(1);
		Assert.assertEquals(1, planet.getTechLevel());

		planet.setType(PlanetType.Gaian);
		Assert.assertEquals(PlanetType.Gaian, planet.getType());

		Assert.assertNull(planet.getSystem());
		Assert.assertEquals(0, planet.getParentId());

		planet.setHydrographics(1);
		Assert.assertEquals(1, planet.getHydrographics());

		planet.setLawLevel(1);
		Assert.assertEquals(1, planet.getLawLevel());

		planet.setRadius(1);
		Assert.assertEquals(1, planet.getRadius());

		planet.setPopulation(1);
		Assert.assertEquals(1, planet.getPopulation());

		planet.setName("Test");
		Assert.assertEquals("Test", planet.getName());

		Assert.assertEquals(0, planet.getNextEvent());
	}

	@Test
	public void testDayLength() {
		Planet planet = new Planet();

		planet.setDayLength(1);
		Assert.assertEquals("1s", planet.getDayLengthAsString());

		planet.setDayLength(60);
		Assert.assertEquals("1m", planet.getDayLengthAsString());

		planet.setDayLength(3602);
		Assert.assertEquals("1h 2s", planet.getDayLengthAsString());

		planet.setDayLength(86400);
		Assert.assertEquals("1d", planet.getDayLengthAsString());
	}

	@Test
	public void testAtmosphereTemperature() {
		Planet planet = new Planet();

		for (AtmosphereType type : AtmosphereType.values()) {
			planet.setAtmosphere(type);
			Assert.assertEquals(type, planet.getAtmosphere());
		}

		for (AtmospherePressure p : AtmospherePressure.values()) {
			planet.setPressure(p);
			Assert.assertEquals(p, planet.getPressure());
		}

		for (Temperature t : Temperature.values()) {
			planet.setTemperature(t);
			Assert.assertEquals(t, planet.getTemperature());
		}

		// Setting a null value should set a vacuum.
		planet.setAtmosphere(null);
		Assert.assertEquals(AtmosphereType.Vacuum, planet.getAtmosphere());
		Assert.assertEquals(AtmospherePressure.None, planet.getPressure());
	}

	@Test
	public void testCivilisation() {
		Planet planet = new Planet();

		for (GovernmentType g : GovernmentType.values()) {
			planet.setGovernment(g);
			Assert.assertEquals(g, planet.getGovernment());
		}

		for (LifeType l : LifeType.values()) {
			planet.setLifeType(l);
			Assert.assertEquals(l, planet.getLifeType());
		}

		for (StarportType s : StarportType.values()) {
			planet.setStarport(s);
			Assert.assertEquals(s, planet.getStarport());
		}
	}

	@Test
	public void testTrade() {
		Planet planet = new Planet();

		Assert.assertNotNull(planet.getTradeCodes());
		Assert.assertNotNull(planet.getTradeCodeList());

		planet.addTradeCode(TradeCode.Ag);
		Assert.assertTrue(planet.getTradeCodes().contains(TradeCode.Ag));
		Assert.assertEquals(1, planet.getTradeCodeList().size());

		planet.addTradeCode(TradeCode.In);
		Assert.assertTrue(planet.getTradeCodes().contains(TradeCode.Ag));
		Assert.assertTrue(planet.getTradeCodes().contains(TradeCode.In));
		Assert.assertEquals(2, planet.getTradeCodeList().size());

		planet.removeTradeCode(TradeCode.Ag);
		Assert.assertFalse(planet.getTradeCodes().contains(TradeCode.Ag));
		Assert.assertTrue(planet.getTradeCodes().contains(TradeCode.In));
		Assert.assertEquals(1, planet.getTradeCodeList().size());
	}

	@Test
	public void testFeatures() {
		Planet planet = new Planet();

		Assert.assertNotNull(planet.getFeatureCodes());

		planet.addFeature(PlanetFeature.AA);
		Assert.assertTrue(planet.getFeatureCodes().contains(PlanetFeature.AA));
		planet.addFeature(PlanetFeature.Algae);
		Assert.assertTrue(planet.getFeatureCodes()
				.contains(PlanetFeature.Algae));
		planet.removeFeature(PlanetFeature.AA);
		Assert.assertFalse(planet.getFeatureCodes().contains(PlanetFeature.AA));
		Assert.assertTrue(planet.getFeatureCodes()
				.contains(PlanetFeature.Algae));
	}

	@Test
	public void testResources() {
		Planet planet = new Planet();

		Assert.assertNotNull(planet.getResources());
		Assert.assertEquals(0, planet.getResources().size());
	}
}

/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import java.util.ArrayList;
import java.util.List;

import uk.org.glendale.rpg.traveller.systems.codes.AtmospherePressure;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.GovernmentType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.worldgen.astro.star.Temperature;
import uk.org.glendale.worldgen.civ.facility.FacilityTO;

/**
 * Simplest representation of a Planet for use by REST interfaces.
 */
public class PlanetTO {
	public final int id;
	public final String name;
	public final int distance;
	public final boolean isMoon;
	public final int parentId;
	public final int radius;
	public final AtmosphereType atmosphere;
	public final AtmospherePressure pressure;
	public final Temperature temperature;
	public final int axialTilt;
	public final int hydrographics;
	public final int dayLength;
	public final String dayLengthText;
	public final long population;
	public final int lawLevel;
	public final int techLevel;
	public final LifeType lifeLevel;
	public final GovernmentType government;
	public final String governmentShort;
	public final StarportType starport;
	public final PlanetType type;
	public final String description;
	public final String tradeCodes;
	public final List<FacilityTO> facilities = new ArrayList<FacilityTO>();
	
	public PlanetTO(Planet planet) {
		this.id = planet.getId();
		this.name = planet.getName();
		this.distance = planet.getDistance();
		this.isMoon = planet.isMoon();
		this.parentId = planet.getParentId();
		this.radius = planet.getRadius();
		this.atmosphere = planet.getAtmosphere();
		this.pressure = planet.getPressure();
		this.temperature = planet.getTemperature();
		this.axialTilt = planet.getAxialTilt();
		this.hydrographics = planet.getHydrographics();
		this.dayLength = planet.getDayLength();
		this.dayLengthText = planet.getDayLengthAsString();
		this.government = planet.getGovernment();
		if (this.government != null) {
			this.governmentShort = government.getAbbreviation();
		} else {
			this.governmentShort = "";
		}
		this.lawLevel = planet.getLawLevel();
		this.techLevel = planet.getTechLevel();
		this.lifeLevel = planet.getLifeType();
		this.population = planet.getPopulation();
		this.starport = planet.getStarport();
		this.type = planet.getType();
		this.description = (planet.getDescription() == null)?null:planet.getDescription();
		
		String tradeCodes = "";
		for (TradeCode c : planet.getTradeCodeList()) {
			tradeCodes += c.name() + " ";
		}
		this.tradeCodes = tradeCodes.trim();
		
		for (Installation in : planet.getFacilities()) {
			facilities.add(new FacilityTO(in.getFacility(), in.getSize()));
		}
	}
	
}

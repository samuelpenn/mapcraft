/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.8 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.codes.*;
import uk.org.glendale.rpg.utils.Die;

/**
 * A factory class for creating planets.
 * 
 * @author Samuel Penn.
 */
public class PlanetFactory {
	private ObjectFactory	factory = null;
	private Star			star = null;
	private int				earthFudge = 2;
	
	// Constant definitions for resource types.
	// Not that this isn't as stupid as it looks, since the refactor option
	// in Eclipse can rename a variable easily if the definition changes, and
	// is more reliable than using a standard search and replace.
	private static final String VEGETABLES = "Vegetables";
	private static final String MEAT = "Meat";
	private static final String SEAFOOD = "Seafood";
	
	private static final String FERRIC = "Ferric ore";
	private static final String CARBONIC = "Carbonic ore";
	private static final String SILICATE = "Silicate ore";
	private static final String AQUAM = "Aquean ore";
	
	private static final String KRYSITE = "Krysite ore";
	private static final String MAGNESITE = "Magnesite ore";
	private static final String ERICATE = "Ericate ore";
	
	private static final String HELIACATE = "Heliacate ore";
	private static final String ACENITE = "Acenite ore";
	private static final String PARDENIC = "Pardenic ore";
	
	private static final String VARDONNEK = "Vardonnek ore";
	private static final String LARATHIC = "Larathic ore";
	private static final String XITHANTITE = "Xithantite ore";
	
	private static final String DORIC = "Doric ore";
	private static final String ISKINE = "Iskine ore";
	private static final String OORCINE = "Oorcine ore"; 
	
	public PlanetFactory(ObjectFactory factory, Star star) {
		this.factory = factory;
		this.star = star;
	}
	
	private void setDayLength(Planet planet, double factor) {
		int		radius = (int)(factor * Math.sqrt(planet.getRadius()));
		
		planet.setDay(Die.d10(3) * radius * 60);
	}
	
	/**
	 * Set a fudge factor to increase the chance of life. Only applies in a
	 * few cases, but slightly increases the likelyhood of Gaian worlds.
	 * 
	 * @param fudge		 0 = normal, 1 = more likely, 2 = very likely.
	 */
	public void setFudgeFactor(int fudge) {
		if (fudge >= 0) {
			earthFudge = fudge;
		}
	}
	
	/**
	 * Get a world of the specific type. By default, it will generate an airless world
	 * of the specified type, according to the rules defined in the enum. If a method
	 * exists called define<PlanetType>, then this is also called to define further
	 * details about the world.
	 */
	private Planet getWorld(String name, int distance, PlanetType type) {
		Planet			planet = new Planet(factory, name, star, distance, type);
		planet.setRadius(type.getRadius()/2 + Die.die(type.getRadius()));
		planet.setTemperature(star.getOrbitTemperature(distance));
		setDayLength(planet, 1.0);
		
		return defineWorld(planet);
	}
	
	Planet defineWorld(Planet planet) {
		String		methodName = "define"+planet.getType();
		Class		c = this.getClass();
		try {
			Method	m = c.getDeclaredMethod(methodName, planet.getClass());
			m.setAccessible(true);
			m.invoke(this, planet);
		} catch (Exception e) {
			// It is valid for there to be no method for this type of planet.
		}
		
		if (planet.hasFeature(PlanetFeature.FastRotation)) {
			planet.setDay(planet.getDay()/5);
		}
		
		// Only one of the following codes should be applied.
		if (!planet.getType().isJovian()) {
			if (planet.getAtmospherePressure() == AtmospherePressure.None) {
				planet.addTradeCode(TradeCode.Va);
			} else if (planet.getLifeLevel() == LifeType.None && planet.getHydrographics() == 0) {
				planet.addTradeCode(TradeCode.Ba);
			} else if (planet.getHydrographics() == 0) {
				planet.addTradeCode(TradeCode.De);
			}
		}
		if (planet.getHydrographics() > 95) {
			planet.addTradeCode(TradeCode.Wa);
		}
		if (planet.getType().isBelt()) {
			planet.addTradeCode(TradeCode.As);
		}
		
		return planet;
	}
	
	/**
	 * Get a moon of the specific type. Similar to creating a planet, but requires
	 * a primary planet to be in orbit around, and calculates distance from star
	 * from this.
	 */
	private Planet getWorld(String name, int distance, PlanetType type, Planet primary) {
		Planet			planet = new Planet(factory, name, star, distance, type);
		planet.setRadius(type.getRadius()/2 + Die.die(type.getRadius()));
		
		// Distance to the star. Large jovians will tend to warm their moons
		// with tidal forces and radiant energy, making them warmer.
		int		effectiveDistance = primary.getDistance();
		if (primary.getType().isJovian()) {
			switch (primary.getType()) {
			case CryoJovian:
				// Unlikely to warm its moons.
				break;
			case SubJovian:
				effectiveDistance *= 0.9;
				break;
			case EuJovian:
				effectiveDistance *= 0.8;
				break;
			case SuperJovian:
				effectiveDistance *= 0.7;
				break;
			case MacroJovian:
				effectiveDistance *= 0.5;
				break;
			case EpiStellarJovian:
				effectiveDistance *= 0.5;
				break;
			}
		}
		
		planet.setTemperature(star.getOrbitTemperature(effectiveDistance));
		setDayLength(planet, 1.0);
		planet.setMoon(true);
		planet.setParentId(primary.getId());
		
		return defineWorld(planet);
		
	}
	
	private void selectRingFeature(Planet planet, boolean minorOnly) {
		switch (Die.d6(3) + (minorOnly?5:0)) {
		case 3: case 4:
			planet.addFeature(PlanetFeature.ExtensiveRings);
			break;
		case 5: case 6:
			planet.addFeature(PlanetFeature.BrightRings);
			break;
		case 7: case 8:
			planet.addFeature(PlanetFeature.Rings);
			break;
		case 9: case 10:
			planet.addFeature(PlanetFeature.FaintRings);
			break;
		case 11: case 12: case 13:
			planet.addFeature(PlanetFeature.PartialRings);
			break;
		default:
			// No rings.
		}
	}

	/**
	 * Found in the outer reaches of the solar system, they tend to be small and cold.
	 * Examples are Neptune and Uranus.
	 */
	void defineCryoJovian(Planet planet) {
		planet.setAtmospherePressure(AtmospherePressure.VeryDense);
		planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
		planet.setMoonCount(Die.d4(1)+1);
		setDayLength(planet, 0.25);
		selectRingFeature(planet, false);
	}
	
	/**
	 * Small gas giants such as Saturn.
	 */
	void defineSubJovian(Planet planet) {
		planet.setAtmospherePressure(AtmospherePressure.VeryDense);
		planet.setAtmosphereType(AtmosphereType.Hydrogen);
		planet.setMoonCount(Die.d4(1)+1);
		setDayLength(planet, 0.25);
		selectRingFeature(planet, false);
	}

	/**
	 * Medium gas giants such as Jupiter.
	 */
	void defineEuJovian(Planet planet) {
		planet.setAtmospherePressure(AtmospherePressure.VeryDense);
		planet.setAtmosphereType(AtmosphereType.Hydrogen);
		planet.setTemperature(planet.getTemperature().getHotter());
		planet.setMoonCount(Die.d4(1)+2);
		setDayLength(planet, 0.25);
		selectRingFeature(planet, false);
	}
	
	/**
	 * Large gas giants.
	 */
	void defineSuperJovian(Planet planet) {
		planet.setAtmospherePressure(AtmospherePressure.VeryDense);
		planet.setAtmosphereType(AtmosphereType.Hydrogen);
		planet.setTemperature(planet.getTemperature().getHotter().getHotter());
		planet.setMoonCount(Die.d4(1)+3);
		setDayLength(planet, 0.25);
		selectRingFeature(planet, false);
	}

	/**
	 * Massive gas giants.
	 */
	void defineMacroJovian(Planet planet) {
		planet.setAtmospherePressure(AtmospherePressure.VeryDense);
		planet.setAtmosphereType(AtmosphereType.Hydrogen);
		planet.setTemperature(planet.getTemperature().getHotter().getHotter().getHotter());
		planet.setMoonCount(Die.d4(1)+4);
		setDayLength(planet, 0.25);
		selectRingFeature(planet, false);
	}

	/**
	 * Jovian planets very close to their primary star, generally 5AU or closer.
	 */
	void defineEpiStellarJovian(Planet planet) {
		planet.setAtmospherePressure(AtmospherePressure.VeryDense);
		planet.setAtmosphereType(AtmosphereType.Hydrogen);
		planet.setMoonCount(Die.d4(1));
		setDayLength(planet, 1);
		selectRingFeature(planet, true);
	}
	
	



	/**
	 * Incredibly geologically active worlds such as Io. Unstable, and much of it may
	 * be molten. May have an atmosphere, though will consist mostly of sulphur.
	 */
	void defineHephaestian(Planet planet) {
		// Atmosphere
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmosphereType(AtmosphereType.SulphurCompounds);
			planet.setAtmospherePressure(AtmospherePressure.Trace);
			break;
		case 4: case 5:
			planet.setAtmosphereType(AtmosphereType.SulphurCompounds);
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.SulphurCompounds);
			if (planet.getRadius() > 3000) {
				planet.setAtmospherePressure(AtmospherePressure.Standard);
				if (Die.d4()==1) planet.addResource("Pentric ore", 1);
			} else {
				planet.setAtmospherePressure(AtmospherePressure.Thin);
			}
			break;
		}
		planet.setTemperature(planet.getTemperature().getHotter());
		
		// Mineral resources.
		planet.addResource(SILICATE, 50+Die.d20());
		planet.addResource(CARBONIC, 40+Die.d20());
		planet.addResource(MAGNESITE, 25+Die.d10());
		planet.addResource(ACENITE, 5+Die.d10());
	}

	/**
	 * World close to a star which has been scoured by hot solar winds. Tend
	 * to be almost entirely metal content, with little rock. Unlikely to
	 * have any atmosphere except trace amounts of Hydrogen/Helium.
	 */
	void defineFerrinian(Planet planet) {
		if (planet.getRadius() > 2000) {
			planet.setAtmosphereType(AtmosphereType.Hydrogen);
			planet.setAtmospherePressure(AtmospherePressure.Trace);			
		}
		
		// Mineral resources.
		planet.addResource(SILICATE, 30+Die.d20());
		planet.addResource(FERRIC, 50+Die.d20());
		planet.addResource(VARDONNEK, 25+Die.d10());
		planet.addResource(LARATHIC, 5+Die.d10());
		
		if (star.getStarForm() == StarForm.WhiteDwarf) {
			planet.addResource(XITHANTITE, Die.d20());
		}
	}
	
	/**
	 * Such worlds have lost their outer mantle, leaving just a dense core.
	 * Mostly iron rich, they are battered and broken worlds, generally
	 * found close to a star.
	 */
	void defineHadean(Planet planet) {
		if (planet.getRadius() > 2000) {
			planet.setAtmosphereType(AtmosphereType.Hydrogen);
			planet.setAtmospherePressure(AtmospherePressure.Trace);			
		}
		
		// Mineral resources.
		planet.addResource(SILICATE, 10+Die.d20(2));
		planet.addResource(FERRIC, 50+Die.d20(2));
		planet.addResource(VARDONNEK, 20+Die.d20());
		planet.addResource(LARATHIC, 5+Die.d20(3));

		if (star.getStarForm() == StarForm.WhiteDwarf) {
			planet.addResource(XITHANTITE, Die.d8());
		}
	}
	
	/**
	 * A small world similar to our moon. Not particularly dense, no
	 * atmosphere and very dry. Tends to be heavily cratered, with
	 * old lava beds. No recent geological activity.
	 */
	void defineSelenian(Planet planet) {
		// Surface features.
		switch (Die.d6(2)) {
		case 2:
			// Probably caused by near-collision.
			planet.addFeature(PlanetFeature.TidalStressMarks);
			break;
		case 3:
			planet.addFeature(PlanetFeature.Smooth);
			break;
		case 4:
			planet.addFeature(PlanetFeature.FastRotation);
			break;
		case 5: case 6:
			planet.addFeature(PlanetFeature.HeavilyCratered);
			break;
		case 12:
			planet.addFeature(PlanetFeature.GiantCrater);
			break;
		default:
			// Nothing special.
		}
		
		// Mineral resources.
		planet.addResource(SILICATE, 20+Die.d20());
		if (planet.getTemperature().isColderThan(Temperature.Standard)) {
			planet.addResource(AQUAM, Die.d6());
		}
	}
	
	/**
	 * A chlorine world, less than a billion years old. It is in the early
	 * stages of formation, and has little or no life.
	 */
	void defineEoChloritic(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmosphereType(AtmosphereType.Primordial);
			break;
		case 4: case 5:
			planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.OrganicToxins);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2: case 3: case 4:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 5:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			break;
		case 6:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
		
		planet.setHydrographics(Die.d20() * 2);
		switch (Die.d8()) {
		case 1: case 2: case 3: case 4:
			planet.setLifeLevel(LifeType.Proteins);
			break;
		case 5: case 6:
			planet.setLifeLevel(LifeType.Protozoa);
			break;
		case 7: case 8:
			planet.setLifeLevel(LifeType.Metazoa);
			break;
		}
		setDayLength(planet, 1.0);
	}

	/**
	 * Primal Earth-like worlds with thick atmospheres and early oceans. There may
	 * be life, though it will be very primitive. Geologically active.
	 */
	void defineEoGaian(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmosphereType(AtmosphereType.Primordial);
			break;
		case 4: case 5:
			planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.OrganicToxins);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 4: case 5:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			break;
		case 6:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
		
		planet.setHydrographics(Die.d20() * 2);
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setLifeLevel(LifeType.Proteins);
			break;
		case 4: case 5:
			planet.setLifeLevel(LifeType.Protozoa);
			break;
		case 6:
			planet.setLifeLevel(LifeType.Metazoa);
			break;
		}
		setDayLength(planet, 1.0);
		
		// Mineral resources.
		planet.addResource(SILICATE, 6);
		planet.addResource(CARBONIC, 5);
		planet.addResource(FERRIC, 4);
		if (planet.getHydrographics() >= 10) {
			planet.addResource(AQUAM, planet.getHydrographics()/10);
		}
	}

	/**
	 * Primal Earth-like world where life is beginning to take hold. Age is
	 * a billion to a few billion years old.
	 */
	void defineMesoGaian(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2: case 3: case 4:
			planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
			break;
		case 5: case 6:
			planet.setAtmosphereType(AtmosphereType.LowOxygen);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 4: case 5:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			break;
		case 6:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));

		planet.setHydrographics(Die.d20() * 3);
		switch (Die.d6()) {
		case 1:
			planet.setLifeLevel(LifeType.Protozoa);
			break;
		case 2: case 3: case 4: case 5:
			planet.setLifeLevel(LifeType.Metazoa);
			break;
		case 6:
			planet.setLifeLevel(LifeType.ComplexOcean);
			break;
		}
		setDayLength(planet, 1.0);

		// Mineral resources.
		planet.addResource(SILICATE, 6);
		planet.addResource(CARBONIC, 5);
		planet.addResource(FERRIC, 4);
		if (planet.getHydrographics() >= 10) {
			planet.addResource(AQUAM, planet.getHydrographics()/10);
		}
		if (planet.getLifeLevel() == LifeType.ComplexOcean) {
			planet.addResource(SEAFOOD, Die.d2());
		}
	}

	/**
	 * Primal Earth-like world where life has taken over the oceans, but there
	 * is no land life. They are the most common of the non-terraformed Gaian
	 * worlds, which suggests that the jump from oceanic to land is a difficult
	 * one for life.
	 */
	void defineArchaeoGaian(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmosphereType(AtmosphereType.LowOxygen);
			break;
		case 3:
			planet.setAtmosphereType(AtmosphereType.Pollutants);
			break;
		case 4:
			planet.setAtmosphereType(AtmosphereType.OrganicToxins);
			break;
		case 5: 
			planet.setAtmosphereType(AtmosphereType.HighCarbonDioxide);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.Standard);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2: case 3: case 4:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 5:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			break;
		case 6:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));

		planet.setHydrographics(Die.d20() * 4);
		planet.setLifeLevel(LifeType.ComplexOcean);
		setDayLength(planet, 1.0);

		// Mineral resources.
		planet.addResource(SILICATE, 6);
		planet.addResource(CARBONIC, 5);
		planet.addResource(FERRIC, 4);
		if (planet.getHydrographics() >= 10) {
			planet.addResource(AQUAM, planet.getHydrographics()/10);
		}
		planet.addResource(SEAFOOD, 3+Die.d4());
	}

	/**
	 * Primal Earth-like world where life is just beginning to take hold on
	 * land. However, the world is too small to hold onto its atmosphere
	 * forever, and eventually it will turn into a Mars like airless desert
	 * within a billion years.
	 */
	void defineEoArean(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmosphereType(AtmosphereType.LowOxygen);
			break;
		case 3:
			planet.setAtmosphereType(AtmosphereType.Pollutants);
			break;
		case 4:
			planet.setAtmosphereType(AtmosphereType.OrganicToxins);
			break;
		case 5: 
			planet.setAtmosphereType(AtmosphereType.HighCarbonDioxide);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.Standard);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 4: case 5:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		case 6:
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));

		planet.setHydrographics(Die.d20() * 2);
		
		switch (Die.d6(2)) {
		case 2: case 3: case 4:
			planet.setLifeLevel(LifeType.None);
			break;
		case 5:
			planet.setLifeLevel(LifeType.Proteins);
			break;
		case 6:
			planet.setLifeLevel(LifeType.Protozoa);
			break;
		case 7:
			planet.setLifeLevel(LifeType.Metazoa);
			planet.addResource(SEAFOOD, 5+Die.d10());
			break;
		case 8: case 9: case 10:
			planet.setLifeLevel(LifeType.ComplexOcean);
			planet.addResource(SEAFOOD, 10+Die.d20(2));
			break;
		case 11: case 12:
			planet.setLifeLevel(LifeType.SimpleLand);
			planet.addResource(SEAFOOD, 20+Die.d20(2));
			planet.addResource(VEGETABLES, 10+Die.d20(2));
			planet.addResource(MEAT, 5+Die.d10());
			break;
		}
		setDayLength(planet, 1.0);

		// Mineral resources.
		planet.addResource(SILICATE, 40 + Die.d20(2));
		planet.addResource(CARBONIC, Die.d20());
		planet.addResource(FERRIC, 10 + Die.d20());
		if (planet.getHydrographics() > 0) {
			planet.addResource(AQUAM, planet.getHydrographics());
		}
	}

	/**
	 * Primal Mars-like world where life is just beginning to take hold on
	 * land. However, the world has become too cold, and is beginning to
	 * freeze. Life is unlikely to evolve into anything more complex.
	 */
	void defineAreanLacustric(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmosphereType(AtmosphereType.LowOxygen);
			break;
		case 4: case 5:
			planet.setAtmosphereType(AtmosphereType.Pollutants);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.Standard);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 4: case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));

		planet.setHydrographics(Die.d20() * 3);
		switch (Die.d6()) {
		case 1:
			planet.setLifeLevel(LifeType.Protozoa);
			break;
		case 2: case 3:
			planet.setLifeLevel(LifeType.Metazoa);
			break;
		case 4: case 5:
			planet.setLifeLevel(LifeType.ComplexOcean);
			planet.addResource(SEAFOOD, 1+Die.d3());
			break;
		case 6:
			planet.setLifeLevel(LifeType.SimpleLand);
			planet.addResource(SEAFOOD, 2+Die.d3());
			planet.addResource(VEGETABLES, 1+Die.d4());
			planet.addResource(MEAT, Die.d2());
			break;
		}
		setDayLength(planet, 1.0);

		// Mineral resources.
		planet.addResource(SILICATE, 6);
		planet.addResource(CARBONIC, 4);
		planet.addResource(FERRIC, 3);
		if (planet.getHydrographics() >= 10) {
			planet.addResource(AQUAM, planet.getHydrographics()/10);
		}
	}

	/**
	 * A world which once could have harboured life, but is now dead, like Mars.
	 */
	void defineArean(Planet planet) {
	
		// Atmosphere type
		switch (Die.d6()) {
		case 1:
			planet.setAtmosphereType(AtmosphereType.LowOxygen);
			break;
		case 2: case 3: case 4:
			planet.setAtmosphereType(AtmosphereType.HighCarbonDioxide);
			break;
		case 5: case 6:
			planet.setAtmosphereType(AtmosphereType.InertGases);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6() + earthFudge) {
		case 1: case 2: case 3: case 4:
			planet.setAtmospherePressure(AtmospherePressure.Trace);
			break;
		case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
			break;
		default:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));

		if (Die.d6() <= earthFudge + 1) {
			planet.setHydrographics(Die.d4());
		}
		setDayLength(planet, 1.0);

		// Mineral resources.
		planet.addResource(SILICATE, 6);
		planet.addResource(CARBONIC, 4);
		planet.addResource(FERRIC, 3);
	}
	
	/**
	 * Venus like, acidic hot house world with a thick atmosphere and
	 * opaque cloud layer.
	 */
	void defineCytherean(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2: case 3: case 4:
			planet.setAtmosphereType(AtmosphereType.CarbonDioxide);
			break;
		case 5: case 6:
			planet.setAtmosphereType(AtmosphereType.SulphurCompounds);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			break;
		case 3: case 4: case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.VeryDense);
			break;
		}
		// Permanent opaque cloud cover.
		planet.addFeature(PlanetFeature.DenseClouds);
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));

		// Mineral resources.
		planet.addResource(SILICATE, 6);
		planet.addResource(FERRIC, 3);
	}
	
	/**
	 * Hot terrestrial world, with a moderate carbon atmosphere. Similar
	 * to Venus, though the clouds are less opaque, and the greenhouse
	 * effect is somewhat less. However, the world is still dry and barren.
	 */
	void definePhosphorian(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1:
			planet.setAtmosphereType(AtmosphereType.InertGases);
			break;
		case 2: case 3: case 4:
			planet.setAtmosphereType(AtmosphereType.CarbonDioxide);
			break;
		case 5: case 6:
			planet.setAtmosphereType(AtmosphereType.SulphurCompounds);
			break;
		}

		// Atmosphere pressure
		switch (Die.d8()) {
		case 1:
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
			break;
		case 2: case 3: case 4:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 7:
			planet.addFeature(PlanetFeature.ThickClouds);
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 8:
			planet.addFeature(PlanetFeature.ThickClouds);
			if (planet.getAtmosphereType() != AtmosphereType.CarbonDioxide) {
				planet.setAtmospherePressure(AtmospherePressure.Dense);
			} else {
				planet.setAtmospherePressure(AtmospherePressure.Standard);
			}
			break;
		}

		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));		

		// Mineral resources.
		planet.addResource(SILICATE, 6);
		planet.addResource(CARBONIC, 2);
		planet.addResource(FERRIC, 3);
	}
	
	/**
	 * A large terrestrial world close to its star. Generally dead worlds, they
	 * manage to cling to an atmosphere without going through the massive greenhouse
	 * process of Cytherian worlds.
	 */
	void defineJaniLithic(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
			break;
		case 3: case 4: case 5:
			planet.setAtmosphereType(AtmosphereType.CarbonDioxide);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.InertGases);
			break;
		}

		// Atmosphere pressure
		switch (Die.d8()) {
		case 1: case 2: case 3:
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
			break;
		case 4: case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		case 7:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			if (planet.getAtmosphereType() == AtmosphereType.NitrogenCompounds) {
				planet.setLifeLevel(LifeType.Protozoa);				
			}
			break;
		case 8:
			planet.addFeature(PlanetFeature.ThickClouds);
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		}

		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
		if (planet.getDistance() < 50) {
			// Set the world to be tidelocked.
			int		period = (int)star.getOrbitPeriod(planet.getDistance());
			switch (Die.d6()) {
			case 1: case 2:
				planet.setDay(period);
				break;
			case 3: case 4:
				planet.setDay(period/2);
				break;
			case 5: case 6:
				planet.setDay((int)(period * 1.5));
				break;
			}
		}

		// Mineral resources.
		planet.addResource(SILICATE, 5);
		planet.addResource(CARBONIC, 1);
		planet.addResource(FERRIC, 3);
	}
	
	/**
	 * Moist greenhouse worlds. Very dense and hot atmosphere, with complete, or
	 * almost complete, water cover. Without the dense atmosphere, the water
	 * would boil.
	 */
	void definePelagic(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1:
			planet.setAtmosphereType(AtmosphereType.HighOxygen);
			break;
		case 2: case 3: case 4:
			planet.setAtmosphereType(AtmosphereType.WaterVapour);
			break;
		case 5: case 6:
			planet.setAtmosphereType(AtmosphereType.CarbonDioxide);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			break;
		case 2: case 3: case 4: case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.VeryDense);
			break;
		}
		
		switch (Die.d6(3)) {
		case 3:
			planet.setLifeLevel(LifeType.Metazoa);
			break;
		case 4:
			planet.setLifeLevel(LifeType.Protozoa);
			break;
		case 5:
			planet.setLifeLevel(LifeType.Proteins);
			break;
		default:
			planet.setLifeLevel(LifeType.None);
		}
		
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));		
		planet.setHydrographics(100);
		setDayLength(planet, 2.0);

		// Mineral resources.
		planet.addResource(AQUAM, 5);
	}

	/**
	 * Earth like worlds, with extensive life and a good atmosphere.
	 */
	void defineGaian(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1:
			planet.setAtmosphereType(AtmosphereType.HighOxygen);
			break;
		case 2: case 3: case 4: case 5: 
			planet.setAtmosphereType(AtmosphereType.Standard);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.HighCarbonDioxide);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		case 2: case 3: case 4: case 5:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 6:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			break;
		}

		if (Die.d3() <= earthFudge) {
			// Force ideal conditions if we're fudging things for more life.
			planet.setAtmosphereType(AtmosphereType.Standard);
			planet.setAtmospherePressure(AtmospherePressure.Standard);
		}
		
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
		
		planet.setHydrographics(40 + Die.d20(3));
		
		switch (Die.d6() + earthFudge) {
		case 1:
			planet.setLifeLevel(LifeType.SimpleLand);
			break;
		case 2: case 3: case 4:
			planet.setLifeLevel(LifeType.ComplexLand);
			break;
		default:
			planet.setLifeLevel(LifeType.Extensive);
		}
		planet.setDay(71000 + Die.die(15000, 2));
	}

	/**
	 * Cold Earth like worlds. They had extensive life once, but are in a snowball
	 * phase.
	 */
	void defineGaianTundral(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1:
			planet.setAtmosphereType(AtmosphereType.Pollutants);
			break;
		case 2: case 3: case 4: 
			planet.setAtmosphereType(AtmosphereType.LowOxygen);
			break;
		case 5: case 6:
			planet.setAtmosphereType(AtmosphereType.Standard);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		case 3: case 4: case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		}
		if (Die.d4() <= earthFudge) {
			// Force ideal conditions if we're fudging things for more life.
			planet.setAtmosphereType(AtmosphereType.Standard);
			planet.setAtmospherePressure(AtmospherePressure.Standard);
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
		planet.setHydrographics(10 + Die.d20(1));
		switch (Die.d6() + earthFudge) {
		case 1: case 2: case 3:
			planet.setLifeLevel(LifeType.ComplexOcean);
			break;
		case 4: case 5:
			planet.setLifeLevel(LifeType.SimpleLand);
			break;
		default:
			planet.setLifeLevel(LifeType.ComplexLand);
		}
		planet.setDay(71000 + Die.die(15000, 2));
	}

	/**
	 * Massive terrestrial worlds with huge oceans.
	 */
	void definePanthalassic(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmosphereType(AtmosphereType.HighOxygen);
			break;
		case 3: case 4: 
			planet.setAtmosphereType(AtmosphereType.HighCarbonDioxide);
			break;
		case 5: case 6:
			planet.setAtmosphereType(AtmosphereType.Pollutants);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			break;
		case 3: case 4: case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
		planet.setHydrographics(100);
		
		switch (Die.d6() + earthFudge) {
		case 1: case 2:
			planet.setLifeLevel(LifeType.None);
			break;
		case 3: case 4:
			planet.setLifeLevel(LifeType.Proteins);
			break;
		case 5:
			planet.setLifeLevel(LifeType.Protozoa);
			break;
		case 6:
			planet.setLifeLevel(LifeType.Metazoa);
			break;
		default:
			planet.setLifeLevel(LifeType.ComplexOcean);
		}
		planet.setDay(75000 + Die.die(50000, 3));
	}

	/**
	 * Old, dying Earth like worlds.
	 */
	void definePostGaian(Planet planet) {
		// Atmosphere type
		switch (Die.d6()) {
		case 1:
			planet.setAtmosphereType(AtmosphereType.Standard);
			break;
		case 2: case 3:
			planet.setAtmosphereType(AtmosphereType.LowOxygen);
			break;
		case 4: case 5: 
			planet.setAtmosphereType(AtmosphereType.Pollutants);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.HighCarbonDioxide);
			break;
		}

		// Atmosphere pressure
		switch (Die.d6() + earthFudge) {
		case 1:
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
			planet.setLifeLevel(LifeType.SimpleLand);
			planet.addResource(VEGETABLES, 4);
			planet.addResource(SEAFOOD, 3);
			planet.addResource(MEAT, 1);
			break;
		case 2: case 3: case 4:
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
			planet.setLifeLevel(LifeType.SimpleLand);
			planet.addResource(VEGETABLES, 5);
			planet.addResource(SEAFOOD, 3);
			planet.addResource(MEAT, 1);
			break;
		default:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			planet.setLifeLevel(LifeType.ComplexLand);
			planet.addResource(VEGETABLES, 7);
			planet.addResource(SEAFOOD, 5);
			planet.addResource(MEAT, 3);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
		
		planet.setHydrographics(Die.d10(3));
		planet.setDay(71000 + Die.die(20000, 2));
	}

	/**
	 * Cold worlds in the outer solar system, made of rock and ice, similar to
	 * Callisto.
	 */
	void defineLithicGelidian(Planet planet) {
		// Atmosphere
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmosphereType(AtmosphereType.Vacuum);
			planet.setAtmospherePressure(AtmospherePressure.None);
			break;
		case 4: case 5:
			planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
			planet.setAtmospherePressure(AtmospherePressure.Trace);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
	}

	/**
	 * Cold worlds with a frozen surface ocean with liquid water beneath.
	 */
	void defineEuropan(Planet planet) {
		if (planet.getType() != PlanetType.Europan) throw new IllegalArgumentException("This is not a Europan world");
		
		// Atmosphere
		switch (Die.d6()) {
		case 1: case 2: case 3:
			planet.setAtmosphereType(AtmosphereType.Vacuum);
			planet.setAtmospherePressure(AtmospherePressure.None);
			break;
		case 4: case 5:
			planet.setAtmosphereType(AtmosphereType.WaterVapour);
			planet.setAtmospherePressure(AtmospherePressure.Trace);
			break;
		case 6:
			planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			break;
		}
	}
	
	/**
	 * Cold dark worlds of ice and little rock.
	 */
	void defineStygian(Planet planet) {
		// Atmosphere
		switch (Die.d6()) {
		case 1: case 2: case 3: case 4:
			planet.setAtmosphereType(AtmosphereType.Vacuum);
			planet.setAtmospherePressure(AtmospherePressure.None);
			break;
		case 5: case 6:
			planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
			planet.setAtmospherePressure(AtmospherePressure.Trace);
			break;
		}
		planet.setTemperature(star.getOrbitTemperature(planet.getEffectiveDistance()));
	}
	
	/**
	 * An asteroid belt. Defines many small rocky planetoids rather than a single object.
	 * Most objects will be sub-100km in radius. Individual large objects will be listed
	 * separately if greater than 1000km in radius.
	 */
	void defineAsteroidBelt(Planet planet) {
		setDayLength(planet, 1.0);
		planet.addTradeCode(TradeCode.As);
	}

	/**
	 * An oort cloud. Defines many small icy planetoids found in the outer reaches of the
	 * solar system. As for asteroid belts, individual large objects will be listed
	 * separately.
	 */
	private Planet getOortCloud(String name, int distance) {
		Planet			planet = new Planet(factory, name, star, distance, PlanetType.OortCloud);
		planet.setRadius(0);
		planet.setTemperature(star.getOrbitTemperature(distance));
		setDayLength(planet, 1.0);
		
		return planet;
	}
	
	/**
	 * A Titan-like world with a thin atmosphere and little in the way of surface liquids.
	 * It's crust is made of hard ice, with a thin methane atmosphere. What standing liquid
	 * there is, is mostly temporary.
	 */
	void defineMesoTitanian(Planet planet) {
		planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmospherePressure(AtmospherePressure.Trace);
			planet.setHydrographics(Die.d6()-5);
			break;
		case 3: case 4:
			planet.setAtmospherePressure(AtmospherePressure.VeryThin);
			planet.setHydrographics(Die.d8()-5);
			break;
		case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			planet.setHydrographics(Die.d10()-5);
			break;
		}
		
		if (planet.getHydrographics() > 0) {
			planet.addTradeCode(TradeCode.Fl); // Non-water oceans.
		}
	}
	
	void defineEuTitanian(Planet planet) {
		planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
		switch (Die.d8()) {
		case 1:
			planet.setAtmospherePressure(AtmospherePressure.Thin);
			planet.setHydrographics(Die.d6());
			break;
		case 2: case 3:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			planet.setHydrographics(Die.d8(2));
			break;
		case 4: case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			planet.setHydrographics(Die.d10(3));
			break;
		case 7: case 8:
			planet.setAtmospherePressure(AtmospherePressure.VeryDense);
			planet.setHydrographics(20 + Die.d10(3));
			break;
		}
		
		if (planet.getHydrographics() > 0) {
			planet.addTradeCode(TradeCode.Fl); // Non-water oceans.
		}
	}
	
	void defineTitanLacustric(Planet planet) {
		planet.setAtmosphereType(AtmosphereType.NitrogenCompounds);
		switch (Die.d6()) {
		case 1: case 2:
			planet.setAtmospherePressure(AtmospherePressure.Standard);
			planet.setHydrographics(Die.d8(3));
			break;
		case 3: case 4:
			planet.setAtmospherePressure(AtmospherePressure.Dense);
			planet.setHydrographics(Die.d10(5));
			break;
		case 5: case 6:
			planet.setAtmospherePressure(AtmospherePressure.VeryDense);
			planet.setHydrographics(30 + Die.d10(5));
			break;
		}
		
		if (planet.getHydrographics() > 0) {
			planet.addTradeCode(TradeCode.Fl); // Non-water oceans.
			switch (Die.d10()) {
			case 1:
				planet.setLifeLevel(LifeType.Proteins);
				break;
			case 2: case 3:
				planet.setLifeLevel(LifeType.Protozoa);
				break;
			case 4: case 5: case 6: case 7:
				planet.setLifeLevel(LifeType.Metazoa);
				break;
			default:
				planet.setLifeLevel(LifeType.SimpleLand);
				break;
			}
		}
	}

	/**
	 * Get a world which has a high chance of being suitable for life.
	 * 
	 * @param name			Name of the world to create.
	 * @param distance		The distance of the world from the primary star, in MKm.
	 * @return				The created world.
	 */
	public Planet getWarmGaian(String name, int distance) {
		Planet		planet = null;
		
		switch (earthFudge) {
		case 2:
			// Very large chance of Earth-like world.
			switch (Die.d10()) {
			case 1:
				planet = getWorld(name, distance, PlanetType.ArchaeoGaian);
				break;
			case 2:
				planet = getWorld(name, distance, PlanetType.MesoGaian);
				break;
			case 3:
				planet = getWorld(name, distance, PlanetType.EoArean);
				break;
			case 4:
				planet = getWorld(name, distance, PlanetType.Panthalassic);
				break;
			case 5: case 6: case 7: case 8:
				planet = getWorld(name, distance, PlanetType.Gaian);
				break;
			case 9:
				planet = getWorld(name, distance, PlanetType.PostGaian);
				break;
			case 10:
				planet = getWorld(name, distance, PlanetType.EoGaian);
				break;
			}
			break;
		case 1:
			// Large chance of Earth-like world.
			switch (Die.d20()) {
			case 1: case 2: case 3: case 4:
				planet = getWorld(name, distance, PlanetType.ArchaeoGaian);
				break;
			case 5: case 6: case 7:
				planet = getWorld(name, distance, PlanetType.MesoGaian);
				break;
			case 8: case 9: case 10:
				planet = getWorld(name, distance, PlanetType.EoArean);
				break;
			case 11:
				planet = getWorld(name, distance, PlanetType.Panthalassic);
				break;
			case 12: case 13: case 14: case 15: case 16:
				planet = getWorld(name, distance, PlanetType.Gaian);
				break;
			case 17: case 18:
				planet = getWorld(name, distance, PlanetType.PostGaian);
				break;
			case 19: case 20:
				planet = getWorld(name, distance, PlanetType.EoGaian);
				break;
			}
			break;
		default:
			// Standard chance of different world types.
			switch (Die.d20()) {
			case 1: case 2: case 3: case 4: case 5: case 6: case 7:
				planet = getWorld(name, distance, PlanetType.ArchaeoGaian);
				break;
			case 8: case 9: case 10:
				planet = getWorld(name, distance, PlanetType.MesoGaian);
				break;
			case 11: case 12:
				planet = getWorld(name, distance, PlanetType.EoArean);
				break;
			case 13:
				planet = getWorld(name, distance, PlanetType.Panthalassic);
				break;
			case 14:
				planet = getWorld(name, distance, PlanetType.Gaian);
				break;
			case 15:
				planet = getWorld(name, distance, PlanetType.PostGaian);
				break;
			case 16: case 17: case 18: case 19: case 20:
				planet = getWorld(name, distance, PlanetType.EoGaian);
				break;
			}
		}
		setDayLength(planet, 1.0);
		
		return planet;
	}

	public Planet getHotAtmosphere(String name, int distance) {
		Planet		planet = null;
		
		switch (Die.d10()) {
		case 1: case 2:
			planet = getWorld(name, distance, PlanetType.Cytherean);
			break;
		case 3: case 4:
			planet = getWorld(name, distance, PlanetType.Phosphorian);
			break;
		case 5: case 6:
			planet = getWorld(name, distance, PlanetType.Pelagic);
			break;
		case 7: case 8:
			// Mercury
			planet = getWorld(name, distance, PlanetType.MesoGaian);
			break;
		case 9: case 10:
			planet = getWorld(name, distance, PlanetType.Panthalassic);
			break;
		}
		setDayLength(planet, 1.0);
		
		return planet;
	}
	
	/**
	 * Generate a world that is no further from the star than the 'optimum'
	 * distance. Worlds closest to the optimum distance will have the greatest
	 * chance of being Earth-like.
	 * 
	 * @param name			Name of the world.
	 * @param distance		Distance from the star in millions of km.
	 * @return				A new world.
	 */
	public Planet getHotWorld(String name, int distance) {
		Planet					planet = null;
		
		if (distance < star.getEarthDistance()/3) {
			switch (Die.d10()) {
			case 1: case 2:
				planet = getWorld(name, distance, PlanetType.AsteroidBelt);
				break;
			case 3: case 4:
				planet = getWorld(name, distance, PlanetType.Vulcanian);
				break;
			case 5: case 6: case 7:
				planet = getWorld(name, distance, PlanetType.Hadean);
				break;
			case 8: case 9:
				planet = getWorld(name, distance, PlanetType.Ferrinian);
				break;
			case 10:
				planet = getWorld(name, distance, PlanetType.Hermian);
				break;
			}
		} else if (distance < star.getEarthDistance()/2) {
			// Very hot worlds, close to the star.
			switch (Die.d6(3)) {
			case 3: case 4: case 5: case 6:
				planet = getWorld(name, distance, PlanetType.AsteroidBelt);
				break;
			case 7:
				planet = getWorld(name, distance, PlanetType.Ferrinian);
				break;
			case 8:
				planet = getWorld(name, distance, PlanetType.Vulcanian);
				break;
			case 9:
				planet = getWorld(name, distance, PlanetType.Basaltic);
				break;
			case 10: case 11:
				// Iron rich dwarf planet
				planet = getWorld(name, distance, PlanetType.Hadean);
				break;
			case 12: case 13: case 14:
				// Mercury
				planet = getWorld(name, distance, PlanetType.Hermian);
				break;
			case 15:
				planet = getWorld(name, distance, PlanetType.JaniLithic);
				break;
			case 16:
				planet = getWorld(name, distance, PlanetType.Phosphorian);
				break;
			case 17:
				planet = getWorld(name, distance, PlanetType.Cytherean);
				break;
			case 18:
				planet = getWorld(name, distance, PlanetType.Pelagic);
				break;
			}
		} else {
			// Warm worlds, possibility of atmosphere, even life.
			switch (Die.d6() + earthFudge) {
			case 1: case 2: case 3:
				planet = getWorld(name, distance, PlanetType.Hermian);
				break;
			case 4: case 5:
				planet = getHotAtmosphere(name, distance);
				break;
			default:
				// Earth-like worlds.
				planet = getWarmGaian(name, distance);
				break;
			}
		}
		setDayLength(planet, 1.0);
		
		return planet;
	}
	
	public Planet getCoolWorld(String name, int distance) {
		Planet					planet = null;
		
		if (distance < star.getEarthDistance()*1.5) {
			// Warm worlds, possibility of atmosphere, even life.
			switch (Die.d6() + earthFudge) {
			case 1:
				planet = getBelt(name, distance);
				break;
			case 2:
				planet = getWorld(name, distance, PlanetType.EoArean);
				break;
			case 3: case 4:
				planet = getWorld(name, distance, PlanetType.AreanLacustric);
				break;
			default:
				planet = getWarmGaian(name, distance);
				break;
			}		
		} else if (distance < star.getEarthDistance()*2) {
			// Cool worlds, just beyond Earth distance.
			switch (Die.d10()) {
			case 1: case 2:
				planet = getWorld(name, distance, PlanetType.GaianTundral);
				break;
			case 3:
				planet = getWorld(name, distance, PlanetType.EoArean);
				break;
			case 4: case 5: case 6:
				planet = getWorld(name, distance, PlanetType.Arean);
				break;
			case 7: case 8:
				planet = getWorld(name, distance, PlanetType.Cerean);
				break;
			case 9:
				planet = getWorld(name, distance, PlanetType.Vestian);
				break;
			case 10:
				planet = getWorld(name, distance, PlanetType.AsteroidBelt);
				break;
			}
		} else {
			// Further out worlds.
			switch (Die.d10()) {
			case 1:
				planet = getWorld(name, distance, PlanetType.Arean);
				break;
			case 2: case 3:
				planet = getWorld(name, distance, PlanetType.Vestian);
				break;
			case 4: case 5: 
				planet = getWorld(name, distance, PlanetType.AreanLacustric);
				break;
			case 6: case 7:
				planet = getWorld(name, distance, PlanetType.Stygian);
				break;
			case 8: case 9:
				planet = getWorld(name, distance, PlanetType.LithicGelidian);
				break;
			case 10:
				planet = getWorld(name, distance, PlanetType.AsteroidBelt);
				break;
			}
		}
		setDayLength(planet, 1.0);
		
		return planet;
	}
	
	/**
	 * Get one of the standard Jovian worlds (anything other than an EpiStellarJovian).
	 * The actual type will depend on how close the planet is to be to its parent star.
	 * 
	 * @param name
	 * @param distance
	 * @return
	 */
	public Planet getColdJovian(String name, int distance) {
		Planet					planet = null;
		
		if (distance < star.getEarthDistance()*10) {
			switch (Die.d6()) {
			case 1: case 2: case 3:
				planet = getWorld(name, distance, PlanetType.SubJovian);
				break;
			case 4: case 5:
				planet = getWorld(name, distance, PlanetType.EuJovian);
				break;
			case 6:
				planet = getWorld(name, distance, PlanetType.SuperJovian);
				break;
			}
		} else if (distance < star.getEarthDistance()*10) {
			switch (Die.d6()) {
			case 1: case 2: case 3:
				planet = getWorld(name, distance, PlanetType.CryoJovian);
				break;
			case 4: case 5:
				planet = getWorld(name, distance, PlanetType.EuJovian);
				break;
			case 6:
				planet = getWorld(name, distance, PlanetType.SuperJovian);
				break;
			}
		} else {
			planet = getWorld(name, distance, PlanetType.CryoJovian);			
		}
		setDayLength(planet, 1.0);
		
		return planet;		
	}

	public Planet getSmallJovian(String name, int distance) {
		Planet					planet = null;
		
		if (distance < star.getEarthDistance()*10) {
			switch (Die.d6()) {
			case 1: case 2: case 3: case 4: case 5:
				planet = getWorld(name, distance, PlanetType.SubJovian);
				break;
			case 6:
				planet = getWorld(name, distance, PlanetType.EuJovian);
				break;
			}
		} else if (distance < star.getEarthDistance()*10) {
			switch (Die.d6()) {
			case 1: case 2: case 3: case 4:
				planet = getWorld(name, distance, PlanetType.CryoJovian);
				break;
			case 5: case 6:
				planet = getWorld(name, distance, PlanetType.SubJovian);
				break;
			}
		} else {
			planet = getWorld(name, distance, PlanetType.CryoJovian);			
		}
		setDayLength(planet, 1.0);
		
		return planet;		
	}
	
	/**
	 * Get a large Jovian, warm enough to support a nice moon.
	 */
	public Planet getLargeJovian(String name, int distance) {
		Planet					planet = null;
		
		if (distance < star.getEarthDistance()*10) {
			switch (Die.d6()) {
			case 1:
				planet = getWorld(name, distance, PlanetType.EuJovian);
				break;
			case 2: case 3: case 4:
				planet = getWorld(name, distance, PlanetType.SuperJovian);
				break;
			case 5: case 6:
				planet = getWorld(name, distance, PlanetType.MacroJovian);
				break;
			}
		} else if (distance < star.getEarthDistance()*10) {
			switch (Die.d6()) {
			case 1: case 2: case 3:
				planet = getWorld(name, distance, PlanetType.SuperJovian);
				break;
			case 4: case 5: case 6:
				planet = getWorld(name, distance, PlanetType.MacroJovian);
				break;
			}
		} else {
			planet = getWorld(name, distance, PlanetType.MacroJovian);			
		}
		setDayLength(planet, 1.0);
		
		return planet;		
	}
	
	/**
	 * Get a hot jovian world, close to its parent star. This will always be an
	 * EpiStellarJovian world.
	 */
	public Planet getHotJovian(String name, int distance) {
		Planet		planet = getWorld(name, distance, PlanetType.EpiStellarJovian);
		setDayLength(planet, 0.5);
		
		return planet;
	}
	
	public Planet getBelt(String name, int distance) {
		Planet					planet = null;

		if (distance < star.getEarthDistance()*30) {
			planet = getWorld(name, distance, PlanetType.AsteroidBelt);
		} else {
			planet = getOortCloud(name, distance);
		}
		planet.setDay(0);
		
		return planet;
	}
	
	public Planet getIceWorld(String name, int distance) {
		Planet					planet = null;
		
		switch (Die.d6()) {
		case 1:
			planet = getWorld(name, distance, PlanetType.IceBelt);
			break;
		case 2:
			planet = getWorld(name, distance, PlanetType.Kuiperian);
			break;
		case 3:
			planet = getWorld(name, distance, PlanetType.LithicGelidian);
			break;
		case 4:
			planet = getWorld(name, distance, PlanetType.Oortean);
			break;
		case 5:
			planet = getWorld(name, distance, PlanetType.Iapetean);
			break;
		case 6:
			planet = getWorld(name, distance, PlanetType.Mimean);
			break;
		}
		setDayLength(planet, 2.0);
		
		return planet;
	}
	
	/**
	 * Get the name for a moon, based on the planet's name and orbit number.
	 * The orbit number should be 1+ (0 is the planet).
	 * 
	 * @param parent		Parent world.
	 * @param orbit			Orbit number of the moon, 1 based index.
	 * @return				Name to use for this moon.
	 */
	private String getMoonName(Planet parent, int orbit) {
		return parent.getName()+"/"+"abcdefghijklmnopqrstuvwxyz".substring(orbit-1, orbit);
	}
	
	/**
	 * Create a number of moons for this planet. The types of moons created
	 * depends on the type of the parent world, how warm it is, and the
	 * closeness of the moon to the parent.
	 * 
	 * Note that moons cannot themselves have moons. We also assume that only
	 * major moons are listed. The dozens of moonlets around jovian worlds
	 * are mostly ignored, for simplicity.
	 * 
	 * @param planet		Parent world to create moons for.
	 * @param number		Number of moons to create.
	 * 
	 * @return				List of all the moons for this planet.
	 */
	public Planet[] getMoons(Planet planet, int number) {
		Planet[]		moons = new Planet[number];
		int				distance = planet.getRadius() * (Die.d4()+1);
		int				increment = distance / 2;

		for (int i = 0; i < number; i++) {
			Planet		moon = null;
			String		name = getMoonName(planet, i+1);
			System.out.println("Creating moon ["+name+"] for ["+planet.getType()+"]");

			switch (planet.getType()) {
			case EpiStellarJovian:
				moon = getWorld(name, distance, PlanetType.Hephaestian, planet);
				break;
			case CryoJovian:
				switch (Die.d6()+i) {
				case 1: case 2:
					moon = getWorld(name, distance, PlanetType.LithicGelidian, planet);
					break;
				case 3: case 4:
					moon = getWorld(name, distance, PlanetType.Stygian, planet);
					break;
				default:
					moon = getWorld(name, distance, PlanetType.Kuiperian, planet);
					break;
				}
			default:
				if (planet.getType().isJovian()) {
					// Moons for Jovian worlds.
					switch (Die.d8()+i) {
					case 1: // Large Asteroid.
						switch (Die.d6()) {
						case 1: case 2:
							moon = getWorld(name, distance, PlanetType.Vulcanian, planet);
							break;
						case 3:
							moon = getWorld(name, distance, PlanetType.Basaltic, planet);
							break;
						case 4: 
							moon = getWorld(name, distance, PlanetType.Sideritic, planet);
							break;
						case 5:
							moon = getWorld(name, distance, PlanetType.Silicaceous, planet);
							break;
						case 6:
							moon = getWorld(name, distance, PlanetType.Enceladean, planet);
							break;
						}
						break;
					case 2: // Io
						moon = getWorld(name, distance, PlanetType.Hephaestian, planet);
						break;
					case 3: case 4: // Europan moon, with sub-surface oceans.
						moon = getWorld(name, distance, PlanetType.Europan, planet);
						break;
					case 5: // Titan-like moon.
						switch (Die.d6()) {
						case 1: case 2: case 3:
							moon = getWorld(name, distance, PlanetType.MesoTitanian, planet);
							break;
						case 4: case 5:
							moon = getWorld(name, distance, PlanetType.EuTitanian, planet);
							break;
						case 6:
							moon = getWorld(name, distance, PlanetType.TitaniLacustric, planet);
							break;
						}
						break;
					case 6: case 7: case 8:
						moon = getWorld(name, distance, PlanetType.LithicGelidian, planet);
						break;
					default:
						// Random moon/asteroid type.
						switch (Die.d6()) {
						case 1:
							moon = getWorld(name, distance, PlanetType.Enceladean, planet);
							break;
						case 2: case 3:
							moon = getWorld(name, distance, PlanetType.Mimean, planet);
							break;
						case 4: case 5:
							moon = getWorld(name, distance, PlanetType.Iapetean, planet);
							break;
						case 6:
							moon = getWorld(name, distance, PlanetType.Silicaceous, planet);
							break;
						}
					}
					break;
				} else {
					moon = getWorld(name, distance, PlanetType.Selenian, planet);
				}
			}
			moons[i] = moon;
			
			distance += Die.die(increment, 2);
			increment *= 1.3;
		}
		return moons;
	}
	
	public static void main(String[] args) throws Exception {
		String				methodName = "defineEuropan";
		Class				c = PlanetFactory.class;
		
		Method[] methods = c.getDeclaredMethods();
		for (Method m : methods) {
			System.out.println(m.toString());
		}

		Method	m = c.getDeclaredMethod(methodName, Planet.class);
		System.out.println("Calling method ["+methodName+"]");

		
	}
}

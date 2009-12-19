package uk.org.glendale.rpg.traveller.systems.codes;

import uk.org.glendale.rpg.traveller.systems.Planet;

/**
 * Defines how habitable a world is. World habitability is based on the
 * following constraints.
 * 
 * LifeLevel: Must be Extensive to be Ideal.
 * Atmosphere: Must be Standard
 * Pressure: Must be Standard
 * Temperature: Must be standard
 * Hydrographics: 60%-80%
 * 
 * All the above have a 'badness rating'. These are added together to
 * work out how bad the world is. Generally:
 * 0 = An ideal world.
 * 1-3 = Unpleasant, but not that big a deal.
 * 5 = Something nasty, requires some protection.
 * 10 = Something unlivable with (vacuum, non-breathable atmosphere)
 * 50 = Something that needs to be actively protected against (acid, extreme heat or pressure)
 * 100 = As per 50, but much worse.
 * 
 * @author Samuel Penn
 */
public enum Habitability {
	VeryHostile(1000, 0.0001),
	Hostile(100, 0.001),
	Inhospitable(25, 0.01),
	Difficult(10, 0.1),
	/**
	 * Atmosphere will be Standard or some variation on that.
	 */
	Unpleasant(6, 0.4),
	/**
	 * Fully habitable without protection, but not entirely ideal.
	 * Atmosphere will be Standard, or some variation on that.
	 * Temperature within Cool -> Warm
	 * Pressure within Thin -> Dense
	 * Life level of at least ComplexLand.
	 */
	Habitable(3, 0.7),
	/**
	 * Earth-like world, very pleasant to live on for the most part.
	 * Life level will be Extensive, atmosphere and temperature Standard.
	 */
	Garden(1, 1.0);
	
	private int		badnessLimit = 0;
	private double 	modifier = 0.0;
	
	Habitability(int badnessLimit, double modifier) {
		this.badnessLimit = badnessLimit;
		this.modifier = modifier;
	}
	
	public double getModifier() {
		return modifier;
	}
	
	public Habitability getWorse() {
		if (ordinal() == 0) return this;
		return values()[ordinal()-1];
	}
	
	public Habitability getBetter() {
		if (ordinal() == values().length-1) return this;
		return values()[ordinal() + 1];
	}
	
	public static Habitability getHabitability(Planet planet) {
		Habitability		habitability = Habitability.Garden;
		int					badness = 0;
		
		badness += planet.getAtmosphereType().getBadness();
		badness += planet.getAtmospherePressure().getBadness();
		badness += planet.getTemperature().getBadness();
		badness += planet.getLifeLevel().getBadness();
		
		if (planet.getType().isJovian()) {
			badness += 100;
		}
		
		if (planet.getHydrographics() < 1) {
			badness += 7;
		} else if (planet.getHydrographics() < 5) {
			badness += 5;
		} else if (planet.getHydrographics() < 15) {
			badness += 3;
		} else if (planet.getHydrographics() < 30) {
			badness += 2;
		} else if (planet.getHydrographics() < 55) {
			badness += 1;
		} else if (planet.getHydrographics() < 85) {
			// No effect.
		} else if (planet.getHydrographics() < 95) {
			badness += 1;
		} else if (planet.getHydrographics() < 98) {
			badness += 3;
		} else {
			badness += 6;
		}
		
		for (Habitability h : Habitability.values()) {
			if (badness <= h.badnessLimit) {
				habitability = h;
			}
		}
		
		
		return habitability;
	}
	
	public boolean isWorseThan(Habitability o) {
		if (o == null) return false;
		return o.badnessLimit < badnessLimit;
	}

	public boolean isBetterThan(Habitability o) {
		if (o == null) return false;
		return o.badnessLimit > badnessLimit;
	}
}

package uk.org.glendale.rpg.traveller.systems.codes;

/**
 * Defines how habitable a world is.
 * 
 * @author Samuel Penn
 */
public enum Habitability {
	VeryHostile(0.000001),   // Venus
	Hostile(0.0001),       // Mercury/Europa
	Inhospitable(0.01),  // Moon/Mars
	Difficult(0.1),     // Thin atmosphere, radiation etc
	Unpleasant(0.5),    // Poisoned Earth
	Hospitable(1.0),    // Earth
	Ideal(1.5);          // Really nice
	
	private double modifier = 0.0;
	
	Habitability(double modifier) {
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
}

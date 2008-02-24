/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.5 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems.codes;

public enum PlanetType {
	Undefined(Category.Belt, 0.0, 0), 
	AsteroidBelt(Category.Belt, 0.0, 0),
	IceBelt(Category.Belt, 0.0, 0),
	OortCloud(Category.Belt, 0.0, 0),
	
	Vulcanian(Category.Asteroid, 7.5, 80),		// Asteroid close to parent sun, heavy metals.
	Silicaceous(Category.Asteroid, 4.0, 120),	// Asteroid with nickle-iron core.
	Sideritic(Category.Asteroid, 6.0, 100),		// Pure nickel-iron, very dense.
	Basaltic(Category.Asteroid, 4.0, 50),		// Cooled larva on surface, smooth. Rare.
	Carbonaceous(Category.Asteroid, 3.0, 150),  // Very dark, rich in carbon. Outer middle solar systems. (C-type)
	Enceladean(Category.Asteroid, 1.6, 250),    // Enceladus (ice, active)
	Mimean(Category.Asteroid, 1.1, 200),        // Mimas (ice, inactive)
	Oortean(Category.Asteroid, 1.5, 100),		// World out in the Oort cloud.
	
	Hadean(Category.Dwarf, 7.0, 500),           // Planetoid very iron rich, just a core.
	Cerean(Category.Dwarf, 2.0, 500), 			// Ceres (rocky core, ice layer, dusty crust)
	Vesperian(Category.Dwarf, 3.4, 300),		// Vespa (iron-nickel core, rocky mantle and crust)
	Vestian(Category.Dwarf, 2.0, 2500),         // Silicate rich moons.
	Kuiperian(Category.Dwarf, 2.0, 1100),       // Pluto
	Hephaestian(Category.Dwarf, 3.0, 1800), 	// Io
	Iapetean(Category.Dwarf, 1.5, 1500),		// Iapetus, stretched and cracked ice world.
	Tritonic(Category.Dwarf, 2.0, 1000),		// Triton, icy volcanism.
	
	MesoTitanian(Category.Dwarf, 3.0, 2300),	// Dead Titan
	EuTitanian(Category.Dwarf, 3.0, 2500),		// Titan (methane, with solid water ice)
	TitaniLacustric(Category.Dwarf, 3.0, 2700),	// Warm Titan, seas.
	
	MesoUtgardian(Category.Dwarf, 2.0, 1200, Habitability.Hostile),	// Ammonia
	EuUtgardian(Category.Dwarf, 2.0, 1500, Habitability.Hostile),		// Ammonia
	UtgardiLacustric(Category.Dwarf, 2.0, 1800, Habitability.Hostile),// Ammonia
	
	Ferrinian(Category.Dwarf, 6.0, 1800), 		// Iron rich
	Selenian(Category.Dwarf, 7.0, 1700), 		// Moon
	Europan(Category.Dwarf, 2.5, 1500), 		// Europa
	Stygian(Category.Dwarf, 2.5, 2000), 		// Now frozen after death of star.
	LithicGelidian(Category.Dwarf, 2.0, 2000),  // Rock/ice worlds, often moons. Ganymede/Callisto
	
	Hermian(Category.Terrestrial, 5.0, 2500), 		// Mercury
	EoGaian(Category.Terrestrial, 5.5, 6500, Habitability.Difficult),
	MesoGaian(Category.Terrestrial, 5.5, 6500, Habitability.Difficult), 
	ArchaeoGaian(Category.Terrestrial, 5.5, 6500, Habitability.Difficult), 
	EoArean(Category.Terrestrial, 5.0, 5500, Habitability.Difficult), 
	AreanLacustric(Category.Terrestrial, 4.5, 3500), 
	Arean(Category.Terrestrial, 4.5, 3500),
	Cytherean(Category.Terrestrial, 5.5, 6200, Habitability.Hostile),    // Venus
	Phosphorian(Category.Terrestrial, 5.5, 6200, Habitability.Hostile),  // Cloudless Venus
	JaniLithic(Category.Terrestrial, 5.5, 5500, Habitability.Hostile),   // Dry, hot, atmosphere.
	Pelagic(Category.Terrestrial, 6.0, 7500, Habitability.Hostile), 
	Gaian(Category.Terrestrial, 5.5, 6500, Habitability.Hospitable), 
	GaianTundral(Category.Terrestrial, 5.5, 6200, Habitability.Unpleasant), 
	Panthalassic(Category.Terrestrial, 5.5, 6000), 
	PostGaian(Category.Terrestrial, 5.5, 6500, Habitability.Unpleasant), 
	
	CryoJovian(Category.Jovian, 1.1, 50000, Habitability.VeryHostile),
	SubJovian(Category.Jovian, 0.8, 70000, Habitability.VeryHostile), 
	EuJovian(Category.Jovian, 1.0, 90000, Habitability.VeryHostile), 
	SuperJovian(Category.Jovian, 1.5, 120000, Habitability.VeryHostile),
	MacroJovian(Category.Jovian, 2.0, 160000, Habitability.VeryHostile), 
	EpiStellarJovian(Category.Jovian, 1.2, 100000, Habitability.VeryHostile);
	
	private enum Category {
		Belt, Asteroid, Terrestrial, Dwarf, Jovian;
	}
	
	private Category		category = Category.Terrestrial;
	private double			density = 5.5;
	private int				radius = 6400;
	private Habitability	habitable = null;

	PlanetType() {
	}
	
	PlanetType(Category category, double density, int radius) {
		this.category = category;
		this.density = density;
		this.radius = radius;
		this.habitable = Habitability.Inhospitable;
	}

	PlanetType(Category category, double density, int radius, Habitability habitable) {
		this.category = category;
		this.density = density;
		this.radius = radius;
		this.habitable = habitable;
	}
	
	public String getPlanetClass() {
		switch (category) {
		case Belt:
			return "Belt";
		case Asteroid:
			return "Asteroid";
		case Dwarf:
			return "Dwarf Planet";
		case Terrestrial:
			return "Terrestrial";
		case Jovian:
			return "Jovian";
		}
		
		return "Belt";
	}
	
	public int getRadius() {
		return radius;
	}
	
	public double getDensity() {
		return density;
	}
	
	public Habitability getHabitability() {
		return habitable;
	}
	
	public boolean isBelt() {
		return category == Category.Belt;
	}
	
	public boolean isAsteroid() {
		return category == Category.Asteroid;
	}
	
	public boolean isDwarfPlanet() {
		return category == Category.Dwarf;
	}
	
	public boolean isTerrestrial() {
		return category == Category.Terrestrial;
	}
	
	public boolean isJovian() {
		return category == Category.Jovian;
	}
	

}

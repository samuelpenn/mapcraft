/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
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

/**
 * Defines the available planetary types.
 * 
 * @author Samuel Penn
 * @formatter:off
 */
public enum PlanetType {
	Undefined(Category.Belt, 0.0, 0), 
	AsteroidBelt(Category.Belt, 0.0, 0),
	VulcanianBelt(Category.Belt, 0.0, 0),
	MetallicBelt(Category.Belt, 0.0, 0),
	IceBelt(Category.Belt, 0.0, 0),
	OortCloud(Category.Belt, 0.0, 0),
	
	MatrioshkaBrain(Category.Construct, 0.0, 0),
	RingWorld(Category.Construct, 0.0, 0),
	Orbital(Category.Construct, 0.0, 0),
	DysonSphere(Category.Construct, 0.0, 0),
	GlobusCassus(Category.Construct, 0.0, 0),
	OrbitalRing(Category.Construct, 0.0, 0),
	
	Vulcanian(Category.Asteroid, 7.5, 100, "hotrock"),		// Asteroid close to parent sun, heavy metals.
	Silicaceous(Category.Asteroid, 4.0, 150),				// Asteroid with nickle-iron core.
	Sideritic(Category.Asteroid, 6.0, 120),					// Pure nickel-iron, very dense.
	Basaltic(Category.Asteroid, 4.0, 80),					// Cooled larva on surface, smooth. Rare.
	Carbonaceous(Category.Asteroid, 3.0, 150),  			// Very dark, rich in carbon. Outer middle solar systems. (C-type)
	Enceladean(Category.Asteroid, 1.6, 220, "ice"),    		// Enceladus (ice, active)
	Mimean(Category.Asteroid, 1.1, 180, "ice"),        		// Mimas (ice, inactive)
	Oortean(Category.Asteroid, 1.5, 100, "ice"),			// World out in the Oort cloud.
	
	Hadean(Category.Dwarf, 7.0, 500, "hotrock"),           	// Planetoid very iron rich, just a core.
	Cerean(Category.Dwarf, 2.0, 500, "ice"), 				// Ceres (rocky core, ice layer, dusty crust)
	Vesperian(Category.Dwarf, 3.4, 300),					// Vespa (iron-nickel core, rocky mantle and crust)
	Vestian(Category.Dwarf, 2.0, 2500),         			// Silicate rich moons.
	Kuiperian(Category.Dwarf, 2.0, 1100, "ice"),       		// Pluto
	Hephaestian(Category.Dwarf, 3.0, 1800), 				// Io
	Iapetean(Category.Dwarf, 1.5, 1500, "ice"),				// Iapetus, stretched and cracked ice world.
	Tritonic(Category.Dwarf, 2.0, 1000, "ice"),				// Triton, icy volcanism.
	
	MesoTitanian(Category.Dwarf, 3.0, 2300),	// Dead Titan
	EuTitanian(Category.Dwarf, 3.0, 2500),		// Titan (methane, with solid water ice)
	TitaniLacustric(Category.Dwarf, 3.0, 2700),	// Warm Titan, seas.
	
	MesoUtgardian(Category.Dwarf, 2.0, 1200),	// Ammonia
	EuUtgardian(Category.Dwarf, 2.0, 1500),		// Ammonia
	UtgardiLacustric(Category.Dwarf, 2.0, 1800),// Ammonia
	
	Ferrinian(Category.Dwarf, 6.0, 1800), 		// Iron rich
	Selenian(Category.Dwarf, 3.3, 1700), 		// Moon
	Europan(Category.Dwarf, 2.5, 1500), 		// Europa
	Stygian(Category.Dwarf, 2.5, 2000), 		// Now frozen after death of star.
	LithicGelidian(Category.Dwarf, 2.0, 2000),  // Rock/ice worlds, often moons. Ganymede/Callisto

	// Gaian type worlds
	EoGaian(Category.Terrestrial, 5.5, 6500, "gaian"),
	MesoGaian(Category.Terrestrial, 5.5, 6500, "gaian"), 
	ArchaeoGaian(Category.Terrestrial, 5.5, 6500, "gaian"),
	Gaian(Category.Terrestrial, 5.5, 6500, "gaian"),
	GaianTundral(Category.Terrestrial, 5.5, 6200, "gaian"), 
	GaianXenic(Category.Terrestrial, 5.5, 6200, "gaian"),
	PostGaian(Category.Terrestrial, 5.5, 6500, "gaian"),
	
	// Chlorine worlds
	EoChloritic(Category.Terrestrial, 5.5, 6500),
	MesoChloritic(Category.Terrestrial, 5.5, 6500),
	ArchaeoChloritic(Category.Terrestrial, 5.5, 6500),
	Chloritic(Category.Terrestrial, 5.5, 6500),
	ChloriticTundral(Category.Terrestrial, 5.5, 6500),
	PostChloritic(Category.Terrestrial, 5.5, 6500),
	
	// Sulphur worlds
	EoThio(Category.Terrestrial, 5.5, 6500),
	MesoThio(Category.Terrestrial, 5.5, 6500),
	ArchaeoThio(Category.Terrestrial, 5.5, 6500),
	Thio(Category.Terrestrial, 5.5, 6500),
	ThioTundral(Category.Terrestrial, 5.5, 6500),
	PostThio(Category.Terrestrial, 5.5, 6500),

	Hermian(Category.Terrestrial, 5.0, 2500, "hotrock"), 		// Mercury
	EoArean(Category.Terrestrial, 4.5, 3500), 
	MesoArean(Category.Terrestrial, 4.5, 3500),
	AreanLacustric(Category.Terrestrial, 4.5, 3500), // Watery Arean 
	Arean(Category.Terrestrial, 4.5, 3500),
	AreanXenic(Category.Terrestrial, 4.5, 3500),   // Hot Arean
	Cytherean(Category.Terrestrial, 5.5, 6200),    // Venus
	PelaCytherean(Category.Terrestrial, 5.5, 6200), // Venus with ocean
	Phosphorian(Category.Terrestrial, 5.5, 6200),  // Cloudless Venus
	JaniLithic(Category.Terrestrial, 5.5, 5500),   // Dry, hot, atmosphere.
	Pelagic(Category.Terrestrial, 6.0, 7000),
	Panthalassic(Category.Terrestrial, 5.5, 10000), // Huge world ocean
	
	CryoJovian(Category.Jovian, 1.1, 50000, "cryojovian"),
	SubJovian(Category.Jovian, 0.8, 70000, "jovian"), 
	EuJovian(Category.Jovian, 1.0, 90000, "jovian"), 
	SuperJovian(Category.Jovian, 1.5, 120000, "jovian"),
	MacroJovian(Category.Jovian, 2.0, 160000, "jovian"), 
	EpiStellarJovian(Category.Jovian, 1.2, 100000, "jovian");
	
	private enum Category {
		Belt, Asteroid, Terrestrial, Dwarf, Jovian, Construct;
	}
	
	private Category		category = Category.Terrestrial;
	private double			density = 5.5;
	private int				radius = 6400;
	private	String			image = "planet";

	PlanetType() {
	}
	
	PlanetType(Category category, double density, int radius) {
		this.category = category;
		this.density = density;
		this.radius = radius;
	}

	PlanetType(Category category, double density, int radius, String image) {
		this.category = category;
		this.density = density;
		this.radius = radius;
		this.image = image;
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
	
	public String getImage() {
		return image;
	}
}

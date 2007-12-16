/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.1 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.worlds;

import java.util.Vector;


/**
 * Defines terrain on a world map. This used to be an enum, but we really
 * need to be able to create dynamic and local instances of it, so we've
 * had to re-create it as a class, with some enum-like functionality.
 * 
 * Note that from now on, we only ever use colours to define a terrain,
 * never an image.
 * 
 * @author Samuel Penn.
 */
public class Terrain {
	/*
	Blank("blank", 100),
	// Stars
	O(170, 170, 220, 1.5, 1.5, 1.5, false),
	B(160, 180, 250, 1.5, 1.5, 1, false),
	A(220, 220, 220, 1, 1, 1, false),
	F(210, 210, 170, 1.6, 1.6, 1.2, false),
	G(200, 200, 160, 1.4, 1.4, 1, false),
	K(200, 160, 150, 1.5, 1.5, 1.3, false),
	M(200, 120, 120, 1.5, 1.5, 1.5, false),
	Ospot(17, 17, 22, 1.5, 1.5, 2, false),
	Bspot(16, 18, 25, 1.5, 1.5, 2, false),
	Aspot(22, 22, 22, 2, 2, 2, false),
	Fspot(21, 21, 17, 2, 2, 1.5, false),
	Gspot(20, 20, 16, 2, 2, 1.5, false),
	Kspot(20, 16, 15, 2, 1.5, 1.3, false),
	Mspot(20, 12, 12, 2, 1.5, 1.5, false),
	//Standard
	Rock("rock", 15),
	Water("water", 100, true),
	WaterLight("waterlight", 100, true),
	WaterDark("waterdark", 100, true),
	WaterRed("waterred", 100, true),
	WaterGreen("watergreen", 100, true),
	WaterPurple("waterpurple", 100, true),
	WaterBlack("waterblack", 100, true),
	Algae("algae", 30, true),
	Grass("grass", 15),
	Gaian("gaian/gaian", 10),
	Dirt("dirt", 15),
	Forest("wood", 30),
	Desert("desert/desert", 30),
	Snow("snow", 30),
	Scrub("scrub", 30),
	Rust("metallic/rust", 15),
	Dark("metallic/dark", 15),
	Sulphur("metallic/sulphur", 15),
	Larva("metallic/larva", 20),
	Ice("ice/ice", 15, true),
	Methane("ice/methane", 15),
	DirtyMethane("ice/dirtymethane", 15),
	// Jovian.
	RedGas("gas/red", 15),
	GreenGas("gas/green", 15),
	BlueGas("gas/blue", 15),
	OrangeGas("gas/orange", 15),
	CreamGas("gas/cream", 15),
	YellowGas("gas/yellow", 15),
	WhiteGas("gas/white", 15),
	// Asteroid types.
	Basaltic("asteroids/basaltic", 15),
	Carbon("asteroids/carbon", 15),
	Vulcanian("asteroids/vulcanian", 15),
	Silicaceous("asteroids/silicaceous", 15),
	// Planetoids
	// Rocky worlds
	Hermian(230, 180, 115, -1, -1, -1, false),
	HermianFlats(210, 190, 135, -0.7, -1, -1, false),
	Ferrinian(120, 120, 50, 3, 2.0, 2.0, false),
	FerrinianEjecta(100, 100, 50, 1.5, 1.0, 0.5, false),
	Hadean(60, 30, 30, 0.8, 0.4, 0.3, false),
	HadeanImpact(40, 20, 20, 0.4, 0.2, 0.1, false),
	HadeanEjecta(90, 60, 60, 1, 0.8, 0.8, false),
	Cerean(50, 50, 50, 2, 2, 1.5, false),
	Vestian(90, 90, 90, 2, 2, 1.5, false),
	Selenian(100, 100, 100, 1.5, 1.5, 1.5, false),
	SelenianFlats(140, 140, 140, -0.25, -0.25, -0.25, false),
	SelenianEjecta(130, 130, 130, 2, 2, 2, false);
	*/
	
	private static Vector<Terrain>  values = new Vector<Terrain>();
	
	private String	name = null;
	private boolean	isWater = false;
	private int		id = 0;
	
	private int		minRed = 0;
	private int		minGreen = 0;
	private int		minBlue = 0;
	private double	varRed = 0.0;
	private double  varGreen = 0.0;
	private double	varBlue = 0.0;
	
	
	/**
	 * Dynamically generate the images.
	 */
	private Terrain(String name, int red, int green, int blue, double r, double g, double b, boolean isWater) {
		this.name = name;
		this.isWater = isWater;
		
		this.minRed = red;
		this.minGreen = green;
		this.minBlue = blue;
		this.varRed = r;
		this.varGreen = g;
		this.varBlue = b;
	}
	
	public static Terrain create(String name, int red, int green, int blue, double r, double g, double b, boolean isWater) {
		Terrain		t = new Terrain(name, red, green, blue, r, g, b, isWater);
		t.id = values.size();
		values.add(t);
		
		return t;
	}

	public static Terrain create(String name, int red, int green, int blue, boolean isWater) {
		Terrain		t = new Terrain(name, red, green, blue, 0, 0, 0, isWater);
		t.id = values.size();
		values.add(t);
		
		return t;
	}
	
	public String toString() {
		return name;
	}
	
	public boolean isWater() {
		return isWater;
	}
	
	private String concatHex(String string, int value) {
		if (value < 16) string += "0";
		if (value > 255) value = 255;
		return string + Integer.toHexString(value);
	}
	
	public String getImage(int height) {
		String		colour = "#";
		int			red = minRed + (int)(height * varRed);
		int			green = minGreen + (int)(height * varGreen);
		int			blue = minBlue + (int)(height * varBlue);
		
		if (red < 0) red = 0;
		if (red > 255) red = 255;
		
		if (green < 0) green = 0;
		if (green > 255) green = 255;
		
		if (blue < 0) blue = 0;
		if (blue > 255) blue = 255;
		
		colour = concatHex(colour, red);
		colour = concatHex(colour, green);
		colour = concatHex(colour, blue);
			
		return colour;
	}
	
	public int getIndex() {
		return id;
	}
	
	public static Terrain getTerrain(int index) {
		return values.elementAt(index);
	}
	
}
/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.civilisation.trade;

/**
 * These codes describe types of goods which are shipped between worlds.
 * They present everything from how hard/easy a good is to produce, to the
 * level of demand for it, who wants it and how easy it is to transport.
 */
public enum CommodityCode {
	// Production
	Tl("TL dependant"),
	TL("Very TL dependant"),
	// Legal
	Il("Illegal"),
	// Used by tech levels
	Tp("Primitive (TL0-1)"),
	Tm("Medieval (TL2-4)"),
	Ti("Industrial (TL5-6)"),
	Tt("Technological (TL7-8)"),
	Ta("Advanced (TL9-10)"),
	Tu("Ultratech (TL11+)"),
	// Used by world types
	In("Industrial"),
	Ag("Agricultural"),
	Re("Residential"),
	Mn("Mining"),
	Sp("Space ports"),
	Mi("Military"),
	Ac("Academic"),
	Nv("Navy bases"),
	Sc("Scout bases"),
	Va("Vacuum environments"),
	Ho("Hot environments"),
	Co("Cold environments"),
	Po("Polluted environments"),
	Ex("Extreme environments"),
	De("Dry environments"),
	Wa("Water worlds"),
	Ge("General product"),
	// Type
	Vi("Vital"),
	Lu("Luxury"),
	Fo("Food"),
	Cl("Clothing"),
	Or("Ore"),
	Fi("Personal firearms"),
	En("Entertainment"),
	Me("Medical"),
	Ma("Machinary"),
	El("Electronics"),
	To("Tool"),
	// Quality
	Hq("High quality"),
	Lq("Low quality"),
	// Trade/storage codes
	Pe("Perisable"),
	Fr("Fragile"),
	Hz("Hazardous"),
	HZ("Extremely Hazardous"),
	/* Four character codes */
	// Building materials
	BldL("Light building materials"),
	BldH("Heavy building materials"),
	BldS("Strong building materials"),
	BldA("Advanced building materials"),
	BldU("Ultratech building materials"),
	// Household goods (needed/produced by residential)
	HoTl("Household tools"),
	HoCk("Cooking utensils"),
	HoLu("Artistic items"),
	HoCl("Household cloths, blankets etc"),
	// Tools
	ToAg("Farming tools"),
	ToHd("Hand tools"),
	// Military
	WpnH("Hand weapons"),
	ArmL("Light armour"),
	ArmH("Heavy armour"),
	// Clothing
	ClCv("Civilian clothing"),
	ClLb("Labourer's clothing"),
	ClLu("Luxury clothing"),
	// Food
	FoMe("Meat"),
	FoFi("Fish"),
	FoGa("Gatherable"),
	FoAg("Farmable"),
	FoHd("Herd animal"),
	FoHu("Huntable"),
	FoOr("Processed organic"),
	// Ores
	OrSi("Silicates"),
	OrFe("Ferrics"),
	OrCa("Carbonics"),
	OrAq("Aquam"),
	OrAu("Auram"),
	// Other
	V("Virtual"),
	Uq("Unique");
	
	private String name = null;
	
	CommodityCode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}

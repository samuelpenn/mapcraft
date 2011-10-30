/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.systems.codes;

/**
 * Define the main government types found in the galaxy.
 * 
 * @author Samuel Penn.
 */
public enum GovernmentType {
	Anarchy("An", -6, -2, -1, 40), 
	Corporation("Co", 0, +2, +2, 5),
	ParticipatingDemocracy("De", 0, 0, +1, 10), 
	SelfPerpetuatingOligarchy("Ol", 0, 0, 0, 10),
	RepresentativeDemocracy("De", 0, 0, +1, 10), 
	FeudalTechnocracy("Fd", +1, 0, -2, 5), 
	Captive("Ca", +2, -1, -1, 5), 
	Balkanization("Ba", 0, -1, 0, 25),
	CivilService("Bu", +1, -1, -1, 10),
	ImpersonalBureaucracy("Bu", +1, -1, 0, 10), 
	CharismaticLeader("Di", +1, 0, 0, 15),
	NonCharismaticLeader("Di", +2, -1, 0, 15), 
	CharismaticOligarchy("Ol", 0, 0, 0, 15), 
	TheocraticDictatorship("Th", +2, -2, -2, 5),
	TheocraticOligarchy("Th", +2, -1, -1, 5), 
	TotalitarianOligarchy("Ol", +2, -2, -2, 5),
	SmallStationOfFacility("Az", 0, -1, -1, 10), 
	Communist("Co", +1, 0, 0, 10),
	SplitControl("Az", 0, 0, 0, 15), SingleClan("Az", 0, 0, 0, 10), SingleMultiWorldClan("Az", 0, 0, 0, 5),
	MajorClan("Az", 0, 0, 0, 5), VassalClan("Az", 0, 0, 0, 5), MajorVassalClan("Az", 0, 0, 0, 5), 
	Family("Kk", 0, 0, 0, 15), Krurruna("Kk", 0, 0, 0, 10), Steppelord("Kk", 0, 0, 0, 10), 
	SeptGoverning("Hv", 0, 0, +1, 15), UnsupervisedAnarchy("Hv", -2, -2, -1, 35), SupervisedAnarchy("Hv", -2, -1, -1, 25), Committee("Hv", 0, 0, 0, 15),
	DroyneHierarchy("Dr", +2, -1, +1, 5);
	
	private String  abbreviation = null;
	private int		law = 0;
	private int		economy = 0;
	private int		tech = 0;
	private int		variability = 0;
	
	private GovernmentType(String abbreviation, int law, int economy, int tech, int variability) {
		this.abbreviation = abbreviation;
		this.law = law;
		this.economy = economy;
		this.tech = 0;
		this.variability = variability;
	}
	
	/**
	 * The category defines which broad class of goverment this specific
	 * government type belongs to. Categories include Anarchy, Dictatorship,
	 * Bureacracy, Oligarchy, Theocracy and a few others.
	 * 
	 * @return Category of this government type.
	 */
	public String getAbbreviation() {
		return abbreviation;
	}
	
	/**
	 * Gets the modifier applied to the world's law level when the world is
	 * randomly generated.
	 * 
	 * @return	Modifier to generated law level.
	 */
	public int getLawModifier() {
		return law;
	}
	
	/**
	 * Gets the minimum law level for this type of government. It is based
	 * on the law modifier.
	 * 
	 * @return	Minimum allowable law level for worlds with this government.
	 */
	public int getMinimumLaw() {
		switch (law) {
		case 1:  return 3;
		case 2:  return 5;
		}
		return 0;
	}
	
	/**
	 * Gets the maximum law level for this type of government. It is derived
	 * from the law modifier.
	 * 
	 * @return	Maximum allowable law level for worlds with this government.
	 */
	public int getMaximumLaw() {
		switch (law) {
		case -2: return 1;
		case -1: return 3;
		}
		return 6;
	}
	
	/**
	 * Gets the modifier to determine if this world is rich or poor.
	 * 
	 * @return	Modifier to economic level of the planet.
	 */
	public int getEconomyModifier() {
		return economy;
	}
	
	/**
	 * Gets the modifier to the tech level of this world. More stable and open
	 * governments positively affect the tech level, whilst repressive or
	 * chaotic governments reduce it.
	 * 
	 * @return	Modifier used to generate planet's tech level.
	 */
	public int getTechModifier() {
		return tech;
	}
	
	/**
	 * Gets the variance applied when calculating weekly production of
	 * facilities for this world. The variance can be positive or negative.
	 *  
	 * @return	Weekly production variance, as percentage.
	 */
	public int getVariability() {
		return variability;
	}
}
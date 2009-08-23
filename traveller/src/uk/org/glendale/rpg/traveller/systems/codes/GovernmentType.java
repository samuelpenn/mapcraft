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
 *
 */
public enum GovernmentType {
	Anarchy("An", -6, -2, -1), 
	Corporation("Co", 0, +2, +1),
	ParticipatingDemocracy("De", 0, 0, 0), 
	SelfPerpetuatingOligarchy("Ol", 0, 0, 0),
	RepresentativeDemocracy("De", 0, 0, 0), 
	FeudalTechnocracy("Fd", +1, 0, -2), 
	Captive("Ca", +2, -1, -1), 
	Balkanization("Ba", 0, -1, 0),
	CivilService("Bu", +1, -1, -1),
	ImpersonalBureaucracy("Bu", +1, -1, 0), 
	CharismaticLeader("Di", +1, 0, 0),
	NonCharismaticLeader("Di", +2, -1, 0), 
	CharismaticOligarchy("Ol", 0, 0, 0), 
	TheocraticDictatorship("Th", +2, -2, -2),
	TheocraticOligarchy("Th", +2, -1, -1), 
	TotalitarianOligarchy("Ol", +2, -2, -2),
	SmallStationOfFacility("Az", 0, -1, -1), 
	SplitControl("Az", 0, 0, 0), SingleClan("Az", 0, 0, 0), SingleMultiWorldClan("Az", 0, 0, 0),
	MajorClan("Az", 0, 0, 0), VassalClan("Az", 0, 0, 0), MajorVassalClan("Az", 0, 0, 0), 
	Family("Kk", 0, 0, 0), Krurruna("Kk", 0, 0, 0), Steppelord("Kk", 0, 0, 0), 
	SeptGoverning("Hv", 0, 0, 0), UnsupervisedAnarchy("Hv", -2, -2, -1), SupervisedAnarchy("Hv", -2, -1, -1), Committee("Hv", 0, 0, 0),
	DroyneHierarchy("Dr", +2, -1, +1);
	
	private String abbreviation = null;
	private int		law = 0;
	private int		economy = 0;
	private int		tech = 0;
	
	private GovernmentType(String abbreviation, int law, int economy, int tech) {
		this.abbreviation = abbreviation;
		this.law = law;
		this.economy = economy;
		this.tech = 0;
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
	
	public int getLawModifier() {
		return law;
	}
	
	public int getMinimumLaw() {
		switch (law) {
		case 1:  return 3;
		case 2:  return 5;
		}
		return 0;
	}
	
	public int getMaximumLaw() {
		switch (law) {
		case -2: return 1;
		case -1: return 3;
		}
		return 6;
	}
	
	public int getEconomyModifier() {
		return economy;
	}
	
	public int getTechModifier() {
		return tech;
	}
}
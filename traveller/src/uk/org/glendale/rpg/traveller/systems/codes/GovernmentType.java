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
	Anarchy("Ana"), 
	Corporation("Cor"),
	ParticipatingDemocracy("Dem"), 
	SelfPerpetuatingOligarchy("Oli"),
	RepresentativeDemocracy("Dem"), 
	FeudalTechnocracy("FdT"), 
	Captive("Cap"), 
	Balkanization("Blk"),
	CivilService("Civ"),
	ImpersonalBureaucracy("Bur"), 
	CharismaticLeader("Cha"),
	NonCharismaticLeader("NCh"), 
	CharismaticOligarchy("Oli"), 
	TheocraticDictatorship("The"),
	TheocraticOligarchy("The"), 
	TotalitarianOligarchy("Oli"),
	SmallStationOfFacility("Cln"), 
	SplitControl("Cln"), SingleClan("Cln"), SingleMultiWorldClan("Cln"),
	MajorClan("Cln"), VassalClan("Cln"), MajorVassalClan("Cln"), 
	Family("Fam"), Krurruna("Kru"), Steppelord("Ste"), 
	SeptGoverning("Sep"), UnsupervisedAnarchy("Ana"), SupervisedAnarchy("Ana"), Committee("Com"),
	DroyneHierarchy("Dro");
	
	private String abbreviation = null;
	private GovernmentType(String abbreviation) {
		this.abbreviation = abbreviation;
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
}
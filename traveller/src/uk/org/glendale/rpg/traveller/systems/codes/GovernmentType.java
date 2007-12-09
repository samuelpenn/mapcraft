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
	Anarchy("Anarchy"), 
	Corporation("Bureacracy"), 
	ParticipatingDemocracy("Democracy"), 
	SelfPerpetuatingOligarchy("Oligarchy"),
	RepresentativeDemocracy("Democracy"), 
	FeudalTechnocracy("Dictatorship"), 
	Captive("Dictatorship"), 
	Balkanization("Anarchy"),
	CivilService("Bureacracy"),
	ImpersonalBureaucracy("Bureacracy"), 
	CharismaticLeader("Dictatorship"),
	NonCharismaticLeader("Dictatorship"), 
	CharismaticOligarchy("Oligarchy"), 
	TheocraticDictatorship("Theocracy"),
	TheocraticOligarchy("Theocracy"), 
	TotalitarianOligarchy("Oligarchy"),
	SmallStationOfFacility("Clan"), 
	SplitControl("Clan"), SingleClan("Clan"), SingleMultiWorldClan("Clan"),
	MajorClan("Clan"), VassalClan("Clan"), MajorVassalClan("Clan"), 
	Family("Other"), Krurruna("Other"), Steppelord("Other"), 
	SeptGoverning("Other"), UnsupervisedAnarchy("Anarchy"), SupervisedAnarchy("Anarchy"), Committee("Bureacracy"),
	DroyneHierarchy("Oligarchy");
	
	private String category = null;
	private GovernmentType(String category) {
		this.category = category;
	}
	
	/**
	 * The category defines which broad class of goverment this specific
	 * government type belongs to. Categories include Anarchy, Dictatorship,
	 * Bureacracy, Oligarchy, Theocracy and a few others.
	 * 
	 * @return Category of this government type.
	 */
	public String getCategory() {
		return category;
	}
}
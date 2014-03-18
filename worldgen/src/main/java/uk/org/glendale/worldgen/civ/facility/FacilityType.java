/*
 * Copyright (C) 2009, 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

/**
 * The type of facility. This determines the behaviour of the
 * facility in terms of its primary resource, and how it should
 * be selected for and categorised.
 * 
 * @author Samuel Penn
 */
public enum FacilityType {
	/**
	 * Consume the primary resource. May produce and consume
	 * other resources as well.
	 */
	Residential,
	/**
	 * Turns the primary resource into a commodity.
	 */
	Mining,
	/**
	 * Turns the primary resource into a commodity.
	 */
	Agriculture,
	/**
	 * Converts one type of commodity into another type of
	 * commodity. Generally have no primary resources.
	 */
	Industry,
	/**
	 * Consumes many types of resources, generally has no
	 * primary resource. May create resources.
	 */
	StarPort,
	/**
	 * A resource which produces commodities.
	 */
	Resource,
	/**
	 * Something which is happening. Generally consumes resources. May
	 * be a war, civil unrest, etc. Events are dynamically added. May
	 * overlap with Culture, may Events are temporary.
	 */
	Event,
	/**
	 * Some aspect of the culture which consumes/produces commodities.
	 * Particularly powerful religious or academic pursuits, or warfare.
	 * There may be an overlap between Events and Culture. Culture are
	 * fixed.
	 */
	Culture;
}

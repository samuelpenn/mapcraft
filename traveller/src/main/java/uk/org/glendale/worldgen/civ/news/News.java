/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.news;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.civ.ship.Ship;

/**
 * Defines a news item. News travels around the galaxy at the speed of the
 * fastest ship. When an event occurs on a world, a record of the event is kept
 * in the system. A copy of the record is given to each ship that passes
 * through, and when a ship arrives at a new system, any 'new' news events are
 * copied to that system's repository.
 * 
 * This way events will slowly percolate around the galaxy. Records are deleted
 * over time, and the importance of the initial event and the distance from the
 * source affect how quickly.
 * 
 * There will be one original copy of any news event (originalId = 0), and zero
 * or more copies (originalId = id of the original instance). Each ship and star
 * system will have its own copy of each event.
 * 
 * We may only keep one copy of the actual message text (in the original), in
 * order to save on storage space.
 * 
 * @author Samuel Penn
 */
public class News {
	private int id;
	private long eventTime;
	private int priority;
	private News originalEvent;
	private NewsType type;

	private StarSystem originalSystem;
	private Planet originalPlanet;
	private Ship originalShip;
	private String text;

	private long recordTime;
	private StarSystem recordSystem;
	private Ship recordShip;

	public static int HIGHEST_PRIORITY = 10;
	public static int HIGH_PRIORITY = 7;
	public static int MEDIUM_PRIORITY = 5;
	public static int LOW_PRIORITY = 3;
	public static int LOWEST_PRIORITY = 1;

	/**
	 * Gets the unique id that identifies this news item.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the time the event that generated this news item occurred.
	 * 
	 * @return Time in seconds since start of simulation.
	 */
	public long getEventTime() {
		return eventTime;
	}

	/**
	 * Gets the priority of this news. Highest priority events are rated 10, and
	 * drop by one each time the news is replicated to a new system. Anything
	 * lower than 1 (LOWEST) is automatically deleted. High priority events stay
	 * around for longer.
	 * 
	 * @return Priority, 10 is highest, 1 is lowest.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Gets the original event this record is based on.
	 */
	public News getOriginalEvent() {
		return originalEvent;
	}

	/**
	 * Gets the type of the news event.
	 */
	public NewsType getType() {
		return type;
	}

	public StarSystem getOriginalSystem() {
		return originalSystem;
	}

	public Planet getOriginalPlanet() {
		return originalPlanet;
	}

	public Ship getOriginalShip() {
		return originalShip;
	}

	public String getText() {
		return text;
	}

	public long getRecordTime() {
		return recordTime;
	}

	public StarSystem getRecordSystem() {
		return recordSystem;
	}

	public Ship getRecordShip() {
		return recordShip;
	}
}

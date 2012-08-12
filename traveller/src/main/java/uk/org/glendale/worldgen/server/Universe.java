/*
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.server;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

/**
 * API into the core fundamental properties of the universe.
 */
@Repository
public class Universe {
	@PersistenceContext
	EntityManager	em;
	
	enum Property {
		TIME("time"),
		REALTIME("realtime"),
		TIMESCALE("timescale"),
		EPOC_START("epocstart"),
		SIM_START_YEAR("simstartyear"),
		DAYS_IN_YEAR("daysinyear"),
		SECONDS_IN_DAY("secondsinday");
		
		private String name;
		
		private Property(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	
	
	private UniversalNumber getNumber(Property name) {
		Query query = em.createQuery("from UniversalNumber where name = :name");
		query.setParameter("name", name.toString());
		
		UniversalNumber number = (UniversalNumber) query.getSingleResult();
		if (number == null) {
			number = new UniversalNumber(name.toString(), 0);
		}
	
		return number;
	}
	
	private void setNumber(Property name, long value) {
		UniversalNumber	number = getNumber(name);
		number.setValue(value);
		em.persist(number);
	}

	/**
	 * Gets the current time of the universe, in number of seconds since the
	 * start of the simulation.
	 * 
	 * @return		Simulation time in seconds.
	 */
	public long getCurrentTime() {
		return getNumber(Property.TIME).getValue();
	}
	
	public void setCurrentTime(long timeInSec) {
		setNumber(Property.TIME, timeInSec);
	}
	
	/**
	 * Gets the current real time that the simulation was last run. This, together
	 * with TIMESCALE, is used to determine how much time has passed in the
	 * simulation.
	 * 
	 * @return	Real time in milliseconds.
	 */
	public long getRealTime() {
		return getNumber(Property.REALTIME).getValue();
	}
	
	public void setRealTime(long timeInMs) {
		setNumber(Property.REALTIME, timeInMs);
	}
}

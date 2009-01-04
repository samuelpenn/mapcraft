/*
 * Copyright (C) 2008 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.database;

import java.sql.*;
import java.util.*;
import java.util.Date;

import uk.org.glendale.rpg.traveller.civilisation.Ship;
import uk.org.glendale.rpg.traveller.civilisation.trade.Trade;
import uk.org.glendale.rpg.traveller.systems.Planet;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.utils.Die;

/**
 * Keeps track of, and manages, the current time in the simulation.
 * 
 * @author Samuel Penn
 */
public class Simulation { //extends ObjectFactory {
	private static final String	CURRENT_TIME = "time";
	private long				currentTimeSec = 0;
	
	private static final String PREVIOUS_TIME = "realtime";
	private long				previousTimeMillis = 0;
	
	private static final String TIME_SCALE = "timescale";
	private long				timeScale = 1;
	
	private static final String START_YEAR = "simstartyear";
	private long				startTime = 0;
	
	private static final String SECONDS_IN_DAY = "secondsinday";
	private long				secondsInDay = 0;
	
	private static final String DAYS_IN_YEAR = "daysinyear";
	private long				daysInYear = 0;
	
	public enum LogType {
		ShipCreated,
		ShipDestroyed,
		JumpIn,
		JumpOut,
		Dock,
		UnDock
	}
	
	private ObjectFactory		factory = null;
	
	public Simulation() {
		factory = new ObjectFactory();

		// Get constants
		secondsInDay = factory.getNumberData(SECONDS_IN_DAY);
		daysInYear = factory.getNumberData(DAYS_IN_YEAR);
		timeScale = factory.getNumberData(TIME_SCALE);
		
		// Get common stats.
		startTime = getStartTime();
		previousTimeMillis = getPreviousTime();
		if (previousTimeMillis == 0) {
			previousTimeMillis = System.currentTimeMillis();
			setPreviousTime(previousTimeMillis);
			setCurrentTime(0);
		}
	}
	


	/**
	 * Get the realworld time that the campaign time was last updated.
	 * Time is in milli seconds since start of the epoc.
	 */
	public long getPreviousTime() {
		previousTimeMillis = factory.getNumberData(PREVIOUS_TIME);
		return previousTimeMillis;
	}
	
	public void setPreviousTime(long realTimeMillis) {
		factory.setNumberData(PREVIOUS_TIME, realTimeMillis);
	}
	
	/**
	 * Get the current campaign time. This is the last time recorded
	 * in the database. Time is in seconds since the start of the
	 * simulation. 
	 */
	public long getCurrentTime() {
		currentTimeSec = factory.getNumberData(CURRENT_TIME);
		return currentTimeSec;
	}

	public void setCurrentTime(long simTimeSeconds) {
		factory.setNumberData(CURRENT_TIME, simTimeSeconds);
		currentTimeSec = simTimeSeconds;
	}
	
	/**
	 * Get the start time of the simulation. In seconds since the
	 * start of the epoc.
	 */
	public long getStartTime() {
		if (startTime == 0) {
			int		year = (int)factory.getNumberData(START_YEAR);
			Date	date = new Date(year-1900, 0, 1);
			
			startTime = date.getTime()/1000;
		}
	
		return startTime;
	}
	
	public String formatTime(long time) {
		String		text = "";
		
		long		year=0, day=0, hour=0, minute=0, second=0;
		
		second = time%60;
		time /= 60;
		
		minute = time%60;
		time /= 60;
		
		hour = time%24;
		time /= 24;
		
		day = 1 + time%daysInYear;
		time /= daysInYear;
		
		year = time;
		
		return String.format("%04d-%03d %02d:%02d:%02d", year, day, hour, minute, second);
	}
	
	/**
	 * Bring the simulation time forward to the specified realtime point.
	 * Attempts to wind back the clock are ignored and do nothing.
	 */
	private void updateClock(long realTimeMillis) {
		previousTimeMillis = getPreviousTime();
		if (realTimeMillis <= previousTimeMillis) {
			return;
		}
		long		shift = realTimeMillis - previousTimeMillis;
		currentTimeSec = getCurrentTime();
		
		currentTimeSec += (shift * timeScale)/1000;
		
		setPreviousTime(realTimeMillis);
		setCurrentTime(currentTimeSec);
	}
	
	/**
	 * Get the actual current time in the simulation. This is not the last
	 * recorded time, but the time it would be set to if the clock was
	 * updated now. Time is returned in seconds.
	 * 
	 * @param realTimeMillis	The current time in the real world, in milliseconds.
	 * @return					The potential simulation time, in seconds.
	 */
	public long getActualCurrentTime(long realTimeMillis) {
		previousTimeMillis = getPreviousTime();
		currentTimeSec = getCurrentTime();
		if (realTimeMillis <= previousTimeMillis) {
			return currentTimeSec;
		}
		long		shift = realTimeMillis - previousTimeMillis;
		currentTimeSec = getCurrentTime();
		
		return currentTimeSec + (shift * timeScale)/1000;
	}
	
	
	public void simulate() {
		long		currentTimeMillis = System.currentTimeMillis();
		
		// Last time that everything was updated.
		previousTimeMillis = getPreviousTime();
		currentTimeSec = getCurrentTime();
		
		simulateAllShips(currentTimeMillis);
		simulateAllPlanets(currentTimeMillis);
		
		updateClock(currentTimeMillis);
	}
	
	private int simulateAllShips(long realTimeMillis) {
		int				count = 0;
		long			actualTime = getActualCurrentTime(realTimeMillis);
		ArrayList<Ship>	list = factory.getNextShips(actualTime);

		for (int i=0; i < list.size(); i++) {
			Ship		ship = list.get(i);
			ship.simulate(this, factory, actualTime);
			count++;
		}
		
		return count;
	}
	
	private int simulateAllPlanets(long realTimeMillis) {
		int				count = 0;
		long			actualTime = getActualCurrentTime(realTimeMillis);
		ObjectFactory	factory = new ObjectFactory();
		
		ArrayList<Planet>	list = factory.getNextPlanets(actualTime);
		long				nextWeek = actualTime + (86400 * 7) + Die.d100() - Die.d100();
		
		for (int i=0; i < list.size(); i++) {
			Planet		planet = list.get(i);
			System.out.println(planet.getName()+" ("+planet.getId()+")");

			Trade		trade = new Trade(factory, planet);
			trade.gatherResources();
			trade.consumeResources();
			
			factory.setPlanetEventTime(planet.getId(), nextWeek);
			count++;
		}
		
		return count;
	}
	
	public static void main(String[] args) throws Exception {
		Simulation		s = new Simulation();
		//s.updateClock(System.currentTimeMillis());
		
		while (true) {
			System.out.println(s.formatTime(s.getCurrentTime()));
			s.simulate();
			System.out.println(s.formatTime(s.getCurrentTime()));
			Thread.sleep(30000);
		}
	}
}

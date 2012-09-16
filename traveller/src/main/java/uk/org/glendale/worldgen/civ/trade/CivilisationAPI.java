/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.trade;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetFactory;
import uk.org.glendale.worldgen.server.Universe;

/**
 * Defines business interface for managing planetary civilisations. Used
 * to simulate economy of worlds. 
 * 
 * @author Samuel Penn
 */
@Controller
@Transactional
public class CivilisationAPI {
	@Autowired
	private CivilisationFactory		factory;
	
	@Autowired
	private PlanetFactory			planetFactory;
	
	@Autowired
	private Universe				universe;
	
	/**
	 * Simulate a single planet.
	 * 
	 * @param planetId		Id of the planet to run the simulation for.
	 */
	public void simulate(int planetId) {
		Planet			planet = planetFactory.getPlanet(planetId);
		simulate(planet);
	}
	
	public void simulate(Planet planet) {
		Civilisation	civ = factory.getCivilisation(planet);
		//List<Inventory>	inventory = factory.getPlanetInventory(planet);
		
		System.out.println("Simulating [" + planet.getName() + "]");
		civ.simulate();
		
		factory.persist(civ.getInventory());
	}
	
	/**
	 * Simulate the economy for every planet in the universe.
	 */
	public synchronized void simulate() {
		long	lastRealTime = universe.getRealTime();
		long	currentRealTime = System.currentTimeMillis();
		
		if (currentRealTime > lastRealTime) {
			long	realTimePassed = currentRealTime - lastRealTime;
			long	timePassed = (universe.getTimescale() * realTimePassed) / 1000;
			long	secondsInDay = universe.getSecondsInDay();
			long	daysPassed = timePassed / secondsInDay;
			long	currentTime = universe.getCurrentTime() + timePassed;
			
			System.out.println("Days passed: " + daysPassed);
			
			List<Planet> planets = planetFactory.getPlanetsWithEvent(currentTime, 10);
			for (Planet p : planets) {
				simulate(p);
				long nextTime = p.getNextEventTime() + secondsInDay * 6 + Die.die((int)secondsInDay, 2);
				if (nextTime < currentTime) {
					nextTime += (currentTime - nextTime) / 2;
				}
				p.setNextEventTime(nextTime);
				planetFactory.persist(p);
			}
			
			// And finally...
			universe.setCurrentTime(currentTime);
			universe.setRealTime(currentRealTime);
		}
	}
}

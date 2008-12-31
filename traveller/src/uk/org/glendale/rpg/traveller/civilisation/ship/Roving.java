package uk.org.glendale.rpg.traveller.civilisation.ship;

import java.util.ArrayList;

import uk.org.glendale.rpg.traveller.civilisation.Ship;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.database.Simulation;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.utils.Die;

/**
 * Basic, random role for ships. Ships with this role have no purpose,
 * and simply move randomly from space port to space port. They do not
 * land on planets, or enter orbit.
 * 
 * @author Samuel Penn
 */
public class Roving implements Role {
	private Ship			ship = null;
	private Simulation		simulation = null;
	private ObjectFactory	factory = null;
	private long			eventTime = 0;
	
	private StarSystem		currentSystem = null;

	public void init(Ship ship, Simulation simulation, ObjectFactory factory, long eventTime) {
		this.ship = ship;
		this.simulation = simulation;
		this.factory = factory;
		this.eventTime = eventTime;
		
		if (ship.getSystemId() != 0) {
			try {
				currentSystem = factory.getStarSystem(Math.abs(ship.getSystemId()));
			} catch (ObjectNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Ship is currently docked. May wait in dock for a period of time,
	 * or leave in order to jump out of system.
	 */
	public void docked() {
		if (Die.d6() == 1) {
			ship.setStatus(Ship.ShipStatus.FlightOut);
			ship.setNextEvent(eventTime + (12 * 3600 * 10 / ship.getAcceleration()));
			simulation.log(ship.getId(), ship.getSystemId(), ship.getPlanetId(), eventTime, Simulation.LogType.UnDock, "Undocks");
		} else {
			ship.setNextEvent(eventTime + Die.d12(2)*3600);
			System.out.println("Remains docked");
		}
	}

	/**
	 * Ship is flying towards the planet. Dock the ship, and wait
	 * for up to 12 hours before deciding what to do next.
	 */
	public void flightIn() {
		ship.setStatus(Ship.ShipStatus.Docked);
		ship.setNextEvent(eventTime + Die.d12()*3600);
	}

	public void flightOut() {
		ArrayList<StarSystem>	destinations = ship.getSystemsInJumpRange(factory);
		StarSystem		destination = destinations.get(Die.rollZero(destinations.size()));
		ship.setStatus(Ship.ShipStatus.Jump);
		ship.setSystemId(-destination.getId());
		ship.setPlanetId(0);
		// Jump time is 168 hours +- 10%
		int		jumpTime = 168*3600 + Die.rollZero(168*360) - Die.rollZero(168*360);
		ship.setNextEvent(eventTime + jumpTime);
		simulation.log(ship.getId(), currentSystem.getId(), 0, eventTime, Simulation.LogType.JumpOut, "Jumps for "+destination.getName());
	}

	public void inJump() {
		ship.setSystemId(currentSystem.getId());
		ship.setPlanetId(currentSystem.getMainWorld().getId());
		ship.setStatus(Ship.ShipStatus.FlightIn);
		ship.setNextEvent(eventTime + (48 * 3600 * 10 / ship.getAcceleration()));
		simulation.log(ship.getId(), ship.getSystemId(), ship.getPlanetId(), eventTime, Simulation.LogType.JumpIn, "Arrives at "+currentSystem.getName());
	}

	/**
	 * Should never be in orbit. Simply move to jump out.
	 */
	public void inOrbit() {
		ship.setStatus(Ship.ShipStatus.FlightOut);
		ship.setNextEvent(eventTime + 1800);
	}

	/**
	 * Should never be planet side. Simply move to jump out.
	 */
	public void planetSide() {
		ship.setStatus(Ship.ShipStatus.FlightOut);
		ship.setNextEvent(eventTime + 7200);
	}

}

package uk.org.glendale.worldgen.civ.ship;

/**
 * Used to keep track of the current activity of a ship. Ships always have
 * a status of Virtual when they are first created, but will almost immediately
 * be given another status as soon as they are placed in the universe.
 * 
 * @author Samuel Penn
 */
public enum ShipStatus {
	Virtual("Undefined"),
	Wreck("Wrecked"),
	Docked("Docked at station"),
	Jump("In Jump"),
	Orbit("Orbiting planet"),
	Planet("Landed on planet"),
	Flight("Orbiting star"),
	FlightIn("Moving out to Jump"),
	FlightOut("Moving towards planet");
	
	private String description;
	private ShipStatus(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}

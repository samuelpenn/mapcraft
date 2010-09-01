package uk.org.glendale.worldgen.civ.ship;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;

/**
 * Defines a space ship within the simulated universe.
 * 
 * @author Samuel Penn
 */
@XmlRootElement
public class Ship {
	private int			id;
	private String		name;
	private String		type;
	private long		inServiceDate;
	private int			displacement;
	private StarSystem	system;
	private Planet		planet;
	private ShipStatus	status;
	private long		nextEvent;
	private String		flag;
	
	private int			jumpCapability;
	private int			acceleration;
	private int			cargoSpace;
	private int			cash;
	
	Ship() {
		status = ShipStatus.Virtual;
	}
	
	/**
	 * Gets the unique id of this space ship.
	 */
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the type of this space ship. This is the ship's model, including
	 * any variant codes.
	 * 
	 * @return		The model of ship this is.
	 */
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	
	/**
	 * Gets the time stamp for when this ship was created in the
	 * simulation.
	 * @return		Simulation time ship was created, in seconds.
	 */
	public long getInServiceDate() {
		return inServiceDate;
	}
	
	/**
	 * Gets the size of this ship, in displacement tonnes of liquid
	 * hydrogen. This is the standard measurement of ship size.
	 * 
	 * @return		Size of ship in displacement tonnes.
	 */
	public int getDisplacement() {
		return displacement;
	}
	
	public void setDisplacement(int displacement) {
		if (displacement < 1) {
			throw new IllegalArgumentException("Displacement must be positive");
		}
		this.displacement = displacement;
	}
}

package uk.org.glendale.rpg.traveller.civilisation.ship;

import uk.org.glendale.rpg.traveller.civilisation.Ship;
import uk.org.glendale.rpg.traveller.database.*;

public interface Role {
	
	public void init(Ship ship, Simulation simulation, ObjectFactory factory, long eventTime);
	
	public void docked();
	public void flightOut();
	public void flightIn();
	public void inOrbit();
	public void planetSide();
	public void inJump();
}

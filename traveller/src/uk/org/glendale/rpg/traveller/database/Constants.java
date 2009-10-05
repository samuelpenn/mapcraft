package uk.org.glendale.rpg.traveller.database;

import java.util.Hashtable;

import uk.org.glendale.rpg.traveller.civilisation.trade.*;
import uk.org.glendale.rpg.traveller.sectors.Allegiance;

/**
 * Cache of database information which is relatively unchanging.
 * Currently includes facilities and commodity data.
 * 
 * TODO: Allow some way of refreshing the data.
 * 
 * @author Samuel Penn
 */
public class Constants {
	private static Hashtable<Integer,Facility>		facilities = null;
	private static Hashtable<Integer,Commodity>		commodities = null;
	private static Hashtable<String,Allegiance>		allegencies = null;
	
	public static void refresh() {
		ObjectFactory		factory = new ObjectFactory();
		
		facilities = factory.getFacilities();
		commodities = factory.getAllCommodities();
		//allegencies = factory.getAllAllegencies();
		
		factory.close();
	}
	
	/**
	 * Get a list of all the facility types. Each facility is
	 * identified by its unique id.
	 * 
	 * @return		Hashtable of facilities keyed by id.
	 */
	public static Hashtable<Integer,Facility> getFacilities() {
		if (facilities == null) refresh();
		return facilities;
	}
	
	/**
	 * Get a specific facility identified by its unique id.
	 * @param id		Id of the facility to get.
	 * @return			The facility, or null.
	 */
	public static Facility getFacility(int id) throws ObjectNotFoundException {
		if (facilities == null) refresh();
		
		Facility	f = facilities.get(id);
		if (f == null) throw new ObjectNotFoundException("Facility with id "+id+" does not exist.");
		
		return facilities.get(id);
	}
	
	public static Facility getFacility(String name) throws ObjectNotFoundException {
		if (facilities == null) refresh();
		
		for (Facility f : facilities.values()) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		throw new ObjectNotFoundException("Facility with name ["+name+"] does not exist.");
	}
	
	public static Hashtable<Integer,Commodity> getCommodities() {
		if (commodities == null) refresh();

		return commodities;
	}
	
	public static Commodity getCommodity(int id) {
		if (commodities == null) refresh();
		
		return commodities.get(id);
	}

	public static Commodity getCommodity(String name) throws ObjectNotFoundException {
		if (commodities == null) refresh();
		
		for (Commodity c : commodities.values()) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		throw new ObjectNotFoundException("Commodity with name ["+name+"] does not exist.");
	}
}

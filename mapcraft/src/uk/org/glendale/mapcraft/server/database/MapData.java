package uk.org.glendale.mapcraft.server.database;

import java.sql.Connection;


public class MapData {
	private Connection		cx = null;
	
	public MapData(Connection cx) {
		this.cx = cx;
	}
	
	/**
	 * Create a new map.
	 * 
	 * @param prefix		Prefix to use for the database tables.
	 */
	public void create(String prefix) {
		
	}
}

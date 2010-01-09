/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 */
package uk.org.glendale.mapcraft.server.database;

import javax.persistence.*;

/**
 * Represents a particular map. Contains information about the
 * scale, size and general properties of the map. To keep things
 * simple, each map uses a different database.
 * 
 * @author Samuel Penn
 */
@Entity
@Table(name="map")
public class Map {
	private String	name;
	private String	title;
	private String	description;
	
	private int		minimumScale = 1;
	private int		maximumScale = 125;
	private int		width = 44000;
	private int		height = 22000;
}

/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.planet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;

/**
 * Stores binary data which represents the image of a world map. A map can be
 * stored in particular projections - either Globe for a 3D view, or Mercator
 * for a flat image.
 * 
 * @author Samuel Penn
 */
@Embeddable
public class MapImage {
	/**
	 * Defines possible projection types.
	 */
	public enum Projection {
		Icosohedron, Mercator, Globe;
	}

	/** Type of projection for this map. */
	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private Projection type;

	/** Binary blob of image data. */
	@Column(name = "image")
	@Lob
	private byte[] imageData;

	/**
	 * Create an empty map image.
	 */
	public MapImage() {
	}

	/**
	 * Sets the projection to use for this map.
	 * 
	 * @param type
	 *            Type of projection to use.
	 */
	public void setType(final Projection type) {
		this.type = type;
	}

	/**
	 * Gets the type of projection that this map is using.
	 * 
	 * @return Type of projection.
	 */
	public Projection getType() {
		return type;
	}

	/**
	 * Sets the byte array which stores the image data. This is assumed to be a
	 * JPEG image. It is stored as a blob in the database.
	 * 
	 * @param imageData
	 *            Image data to store.
	 */
	public void setData(final byte[] imageData) {
		this.imageData = imageData;
	}

	/**
	 * Gets the byte array for the JPEG image.
	 * 
	 * @return Byte array of JPEG data.
	 */
	public byte[] getData() {
		return imageData;
	}
}

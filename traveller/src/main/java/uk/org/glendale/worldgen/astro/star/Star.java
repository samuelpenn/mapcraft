/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.astro.star;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import uk.org.glendale.worldgen.astro.starsystem.StarSystem;

/**
 * Represents a Star in a solar system. A system will consist of one or more
 * stars.
 * 
 * @author Samuel Penn
 */
@Entity
@Table(name = "star")
@XmlRootElement(name = "star")
public class Star {
	// Unique identifier used as primary key.
	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	// Persisted fields.
	@Column(name = "name")
	private String name;

	// Astronomical data
	@ManyToOne
	@JoinColumn(name = "system_id", referencedColumnName = "id")
	private StarSystem system;

	@Column(name = "parent_id")
	private int parentId;
	@Column(name = "distance")
	private int distance;
	@Enumerated(EnumType.STRING)
	@Column(name = "form")
	private StarForm form;
	@Enumerated(EnumType.STRING)
	@Column(name = "class")
	private StarClass classification;
	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private SpectralType type;

	Star() {

	}

	Star(StarSystem system) {
		this.system = system;
	}

	/**
	 * Gets the unique id of the star. All star ids are unique across the entire
	 * universe.
	 * 
	 * @return Unique id of this star.
	 */
	public int getId() {
		return id;
	}

	public StarSystem getSystem() {
		return system;
	}

	/**
	 * Gets the name of the star. Names should be unique within a star system.
	 * If there are multiple stars, normally the first is named Alpha, the
	 * second Beta etc.
	 * 
	 * @return Name of the star.
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Name is not valid");
		}
		this.name = name.trim();
	}

	/**
	 * Gets the id of the parent around which this star orbits. If the star has
	 * no parent, this is zero.
	 * 
	 * @return Gets the parent id of this star.
	 */
	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * Gets the orbit distance of this star, in millions of kilometres. If the
	 * star has no parent, this will normally be zero. Support for multiple
	 * stars orbiting a common centre of gravity is not yet supported.
	 * 
	 * @return Distance from parent star, in Mkm.
	 */
	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		if (distance < 0) {
			throw new IllegalArgumentException("Distance cannot be negative");
		}
		this.distance = distance;
	}

	/**
	 * Gets the form of this star. The majority of stars will be of form 'Star'.
	 * A small number will be 'WhiteDwarf', and there may exist 'NeutroStar' or
	 * 'BlackHole' forms, but they are so rare they may not exist.
	 * 
	 * @return 'Star', 'WhiteDwarf' etc.
	 */
	public StarForm getForm() {
		return form;
	}

	public void setForm(StarForm form) {
		this.form = form;
	}

	/**
	 * Gets the classification of this star, from class VI dwarfs up to class Ia
	 * super giants. Most stars are class V.
	 * 
	 * @return Star class, from VI up to Ia.
	 */
	public StarClass getClassification() {
		return classification;
	}

	public void setClassification(StarClass classification) {
		this.classification = classification;
	}

	/**
	 * Gets the spectral type of the star, using the Hertzsprung Russell
	 * diagram. This is a two character code, e.g. our sun is G2.
	 * 
	 * @return Spectral type of star.
	 */
	public SpectralType getSpectralType() {
		return type;
	}

	public void setSpectralType(SpectralType type) {
		this.type = type;
	}
}

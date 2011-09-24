/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

import java.util.EnumSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

/**
 * Represents a commodity. Commodities can be produced, consumed and traded.
 * Lists of commodities are actually represented as type TradeGood, which
 * includes quantity and price.
 * 
 * Not all commodities are used - some are generic parents for more specific
 * types which are used.
 * 
 * @author Samuel Penn
 */
@Entity
@Table(name = "commodity")
public class Commodity {
	// Unique identifier used as primary key.
	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	// Parent of this commodity type, if any. Note we need the
	// Hibernate annotation @NotFound, because we use 0 for no parent.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", nullable = true)
	@NotFound(action = NotFoundAction.IGNORE)
	private Commodity parent;

	// Persisted fields.
	@Column(name = "name")
	private String name;
	@Column(name = "image")
	private String imagePath;
	@Enumerated(EnumType.STRING)
	@Column(name = "source")
	private Source source;
	@Column(name = "cost")
	private int cost;
	@Column(name = "dt")
	private int volume;
	@Column(name = "production")
	private int production;
	@Column(name = "consumption")
	private int consumption;
	@Column(name = "law")
	private int lawLevel;
	@Column(name = "tech")
	private int techLevel;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "commodity_codes", joinColumns = @JoinColumn(name = "commodity_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "code")
	private Set<CommodityCode> codes = EnumSet.noneOf(CommodityCode.class);

	// @Column(name = "codes")
	// private String c = "";

	/**
	 * Set up a commodity with default values.
	 */
	protected Commodity() {
		source = Source.Mi;
		consumption = production = 20;
		volume = 1;
		lawLevel = 6;
		techLevel = 0;
		parent = null;
		cost = 100;
	}

	/**
	 * Gets the unique id for this commodity.
	 * 
	 * @return Unique commodity id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the parent of this commodity. A parent will be a more generic type
	 * of this commodity.
	 * 
	 * @return Parent commodity, or null if none.
	 */
	public Commodity getParent() {
		return parent;
	}

	void setParent(Commodity parent) {
		this.parent = parent;
	}

	/**
	 * Gets the unique name of this commodity.
	 * 
	 * @return Unique commodity name.
	 */
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	/**
	 * Gets the path to the icon which represents this commodity.
	 * 
	 * @return Icon name.
	 */
	public String getImagePath() {
		return imagePath;
	}

	void setImagePath(final String imagePath) {
		this.imagePath = imagePath;
	}

	/**
	 * Gets the origin of this type of commodity. This will be Mined,
	 * Agriculture, Industry, Art, Knowledge goods etc.
	 * 
	 * @return What produces this type of commodity.
	 */
	public Source getSource() {
		return source;
	}

	void setSource(Source source) {
		this.source = source;
	}

	/**
	 * Gets the base cost of this commodity type. This is different to the price
	 * of an individual instance, but affects it.
	 * 
	 * @return Average cost of this commodity, in credits.
	 */
	public int getCost() {
		return cost;
	}

	void setCost(final int cost) {
		this.cost = cost;
	}

	/**
	 * Gets the volume of one unit of this commodity type. This will nearly
	 * always be one.
	 * 
	 * @return Volume of one unit, in displacement tonnes.
	 */
	public int getVolume() {
		return volume;
	}

	void setVolume(int volume) {
		this.volume = volume;
	}

	/**
	 * Gets the production rating of this commodity. Low production ratings are
	 * easier to produce, since it governs the population required to produce
	 * one unit. The scale is logarithmic, with PR 0 = 1 person, and each +2
	 * multiplying the requirement by 10 (e.g., PR 10 = 100,000 people). Gives a
	 * weekly production rate.
	 * 
	 * @return Production rating, from 1 to 20.
	 */
	public int getProductionRating() {
		return production;
	}

	long getProduction(final long population) {
		return (long) (population / Math.pow(10, production / 2.0));
	}

	void setProductionRating(final int pr) {
		this.production = pr;
	}

	/**
	 * Gets the consumption rating of this commodity. Uses the same scale as the
	 * production rating, but governs how many people are required to consume
	 * one unit per week. If PR is higher than CR, then the commodity will be in
	 * high demand since it will be consumed quicker than it is produced, all
	 * else being equal.
	 * 
	 * @return Consumption rating, from 1 to 20.
	 */
	public int getConsumptionRating() {
		return consumption;
	}

	long getConsumption(final long population) {
		return (long) (population / Math.pow(10, consumption / 2.0));
	}

	void setConsumptionRating(final int cr) {
		this.consumption = cr;
	}

	/**
	 * Gets the legality of this commodity. If the commodity law level is lower
	 * than the planet's law level, then the commodity will be restricted or
	 * illegal.
	 * 
	 * @return Law level, 0 (Mostly illegal) to 6 (legal everywhere).
	 */
	public int getLawLevel() {
		return lawLevel;
	}

	void setLawLevel(final int lawLevel) {
		this.lawLevel = lawLevel;
		if (this.lawLevel < 0) {
			this.lawLevel = 0;
		} else if (this.lawLevel > 6) {
			this.lawLevel = 6;
		}

	}

	/**
	 * Gets the tech level which is required to produce this good.
	 * 
	 * @return Tech level required to produce good.
	 */
	public int getTechLevel() {
		return techLevel;
	}

	void setTechLevel(final int techLevel) {
		this.techLevel = techLevel;
		if (this.techLevel < 0) {
			this.techLevel = 0;
		}
	}

	public Set<CommodityCode> getCodes() {
		return codes;
	}

	void addCode(final CommodityCode code) {
		codes.add(code);
	}

	public boolean hasCode(final CommodityCode code) {
		return codes.contains(code);
	}

}

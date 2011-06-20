/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.commodity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Maps an action to a commodity type. Used when producing resources from other
 * types of resources. For example, 'Grasses' might map to 'Grain' when
 * 'Farming' (AgFa) is applied.
 * 
 * @author Samuel Penn
 */
@Entity
@Table(name = "commodity_map")
public class CommodityMap {
	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "commodity_id", nullable = false)
	private Commodity commodity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "output_id", nullable = false)
	private Commodity output;

	@Column(name = "mode")
	private String operation;

	@Column(name = "efficiency")
	private int efficiency;

	private CommodityMap() {
		// Private empty constructor for JPA.
	}

	CommodityMap(Commodity commodity, Commodity output, String operation) {
		this.commodity = commodity;
		this.output = output;
		this.operation = operation;
		this.efficiency = 100;
	}

	CommodityMap(Commodity commodity, Commodity output, String operation,
			int efficiency) {
		this.commodity = commodity;
		this.output = output;
		this.operation = operation;
		this.efficiency = efficiency;
	}

	public Commodity getCommodity() {
		return commodity;
	}

	public Commodity getOutput() {
		return output;
	}

	public String getOperation() {
		return operation;
	}

	public int getEfficiency() {
		return efficiency;
	}
}

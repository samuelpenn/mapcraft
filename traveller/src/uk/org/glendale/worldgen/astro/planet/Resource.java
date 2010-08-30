package uk.org.glendale.worldgen.astro.planet;

import javax.persistence.*;

import uk.org.glendale.worldgen.civ.commodity.Commodity;

/**
 * Represents a planetary resource. A resource is a type of commodity and a
 * density, typically from 1 - 100. Resources may be mined/farmed to provide
 * goods which can be used or sold.
 * 
 * @author Samuel Penn
 */
@Embeddable
public class Resource {
	@ManyToOne @JoinColumn(name="commodity_id")
	private Commodity	commodity;
	@Column(name="density")
	private int			density;
	
	private Resource() {
	}
	
	public Resource(Commodity c, int density) {
		if (density > 100) {
			density = 100;
		}
		if (c == null) {
			throw new IllegalArgumentException("Resource must have a valid commodity");
		}
		if (density < 1) {
			throw new IllegalArgumentException("Resource density for ["+c.getName()+"] cannot be zero or less");
		}
		this.commodity = c;
		this.density = density;
	}
	
	/**
	 * Gets the commodity type that this resource provides.
	 * 
	 * @return		Type of commodity.
	 */
	public Commodity getCommodity() {
		return commodity;
	}
	
	/**
	 * Gets the density of this resource. Densities will typically range
	 * from 1 - 100, though may in rare instances be higher. The density
	 * will never be lower than 1. If density is 0 or less, the resource
	 * will not be listed.
	 * 
	 * @return		Density, from 1 - 100.
	 */
	public int getDensity() {
		return density;
	}
}

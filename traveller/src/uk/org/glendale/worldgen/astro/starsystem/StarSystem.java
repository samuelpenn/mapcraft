package uk.org.glendale.worldgen.astro.starsystem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import uk.org.glendale.worldgen.astro.sector.Sector;

@Entity @Table(name="system")
@XmlRootElement(name="system")
public class StarSystem {
	// Unique identifier used as primary key.
	@Id @GeneratedValue
	@Column(name="id")			private	int			id;
	
	// Persisted fields.
	@Column(name="name")		private String		name;
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="sector_id", referencedColumnName = "id")
	private Sector		sector;
	@Column(name="x")			private int			x;
	@Column(name="y")			private int			y;
	@Column(name="allegiance")	private String		allegiance;
	@Column(name="zone")		private String		zone;
	@Column(name="base")		private String		base;
	@Column(name="uwp")			private String		uwp;
	@Column(name="selection")	private int			selection;
	

	public StarSystem() {
		
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Sector getSector() {
		return sector;
	}
}

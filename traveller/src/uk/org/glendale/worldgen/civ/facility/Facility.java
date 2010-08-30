package uk.org.glendale.worldgen.civ.facility;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

import uk.org.glendale.rpg.traveller.civilisation.trade.FacilityType;

/**
 * 
 * @author Samuel Penn
 */
@XmlRootElement
@Entity
public class Facility {
	// Unique identifier used as primary key.
	@Id @GeneratedValue
	@Column(name="id")			private	int				id;

	@Column(name="name")		private String			name;
	@Enumerated(EnumType.STRING)
	@Column(name="type")		private FacilityType	type;
	@Column(name="image")		private String			imagePath;
	@Column(name="techLevel")	private int				techLevel;
	@Column(name="capacity")	private int				capacity;
	@Column(name="resource_id")	private int				resourceId;
	@Column(name="inputs")		private String			inputs;
	@Column(name="outputs")		private String			outputs;
	@Column(name="codes")		private String			codes;
}

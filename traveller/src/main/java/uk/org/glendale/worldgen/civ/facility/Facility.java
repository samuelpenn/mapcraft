package uk.org.glendale.worldgen.civ.facility;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import uk.org.glendale.rpg.traveller.civilisation.trade.CommodityCode;
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
	/*
	@MapKey(name="facility_id") @JoinTable(name="facility_requirements", joinColumns=@JoinColumn(name="facility_id"))
	@AttributeOverrides({
		@AttributeOverride(name="key", column=@Column(name="code")),
		@AttributeOverride(name="value", column=@Column(name="value"))
	})
	*/
	
	@OneToMany @JoinTable(name="facility_requirements")
	@MapKeyColumn(name="facility_id")
	private Map<String,Integer>	requirementList = new Hashtable<String,Integer>();
	
	/**
	 * Gets the unique id of this facility.
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public FacilityType getType() {
		return type;
	}
	
	public void setType(FacilityType type) {
		this.type = type;
	}
	
	public int getMinimumTechLevel() {
		return techLevel;
	}
	
	public void setMinimumTechLevel(int techLevel) {
		this.techLevel = techLevel;
	}
}

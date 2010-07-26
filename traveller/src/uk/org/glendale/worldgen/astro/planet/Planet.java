package uk.org.glendale.worldgen.astro.planet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity @Table(name="planet")
@XmlRootElement(name="planet")
public class Planet {
	// Unique identifier used as primary key.
	@Id @GeneratedValue
	@Column(name="id")			private	int			id;
	
	// Persisted fields.
	@Column(name="name")		private String		name;

	
}

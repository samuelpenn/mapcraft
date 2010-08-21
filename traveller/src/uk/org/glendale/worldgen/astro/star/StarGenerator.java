package uk.org.glendale.worldgen.astro.star;

import uk.org.glendale.rpg.traveller.systems.codes.SpectralType;
import uk.org.glendale.rpg.traveller.systems.codes.StarClass;
import uk.org.glendale.rpg.traveller.systems.codes.StarForm;
import uk.org.glendale.rpg.utils.Die;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;


/**
 * Generates stars for a star system. Created stars are not persisted, this
 * is left to the StarSystemGenerator to handle as part of system creation.
 * 
 * @author Samuel Penn
 */
public class StarGenerator {
	private StarSystem	system;
	private boolean		multipleStars;
	private Star		primary, secondary, tertiary;
	
	public StarGenerator(StarSystem system, boolean multipleStars) {
		this.system = system;
		this.multipleStars = multipleStars;
	}
	
	public Star generatePrimary() {
		primary = new Star(system);
		primary.setName(system.getName()+((multipleStars)?" Alpha":""));
		
		primary.setForm(StarForm.Star);
		primary.setClassification(StarClass.V);
		primary.setSpectralType(SpectralType.G2);
		
		return primary;
	}
	
	public Star generateSecondary() {
		if (!multipleStars) {
			throw new IllegalStateException("This system has only one star");
		}
		if (primary == null || primary.getId() == 0) {
			throw new IllegalStateException("Primary star has not been defined");
		}
		secondary = new Star(system);
		secondary.setName(system.getName()+" Beta");
		
		secondary.setForm(StarForm.Star);
		secondary.setClassification(StarClass.VI);
		secondary.setSpectralType(SpectralType.M5);
		
		// This is a place holder value.
		secondary.setParentId(primary.getId());
		secondary.setDistance(Die.d10(10)*10000);
		
		return secondary;
	}

	public Star generateTertiary() {
		if (!multipleStars) {
			throw new IllegalStateException("This system has only one star");
		}
		if (secondary == null || secondary.getId() == 0) {
			throw new IllegalStateException("Secondary star has not been defined");
		}
		tertiary = new Star(system);
		tertiary.setName(system.getName()+" Gamma");
		
		tertiary.setForm(StarForm.WhiteDwarf);
		tertiary.setClassification(StarClass.D);
		tertiary.setSpectralType(SpectralType.D7);
		
		tertiary.setParentId(secondary.getId());
		tertiary.setDistance(Die.d10(5)*1000);
		
		return tertiary;
	}
}

package uk.org.glendale.worldgen.astro.star;

import uk.org.glendale.rpg.traveller.systems.codes.StarClass;
import uk.org.glendale.rpg.traveller.systems.codes.StarForm;
import uk.org.glendale.rpg.utils.Die;


/**
 * Generates stars.
 * 
 * @author Samuel Penn
 */
public class StarGenerator {
	private String		systemName;
	private boolean		multipleStars;
	private Star		primary, secondary, tertiary;
	
	public StarGenerator(String systemName, boolean multipleStars) {
		this.systemName = systemName;
		this.multipleStars = multipleStars;
	}
	
	public Star generatePrimary() {
		primary = new Star();
		primary.setName(systemName+((multipleStars)?" Alpha":""));
		
		primary.setForm(StarForm.Star);
		primary.setClassification(StarClass.V);
		primary.setType("G2");
		
		return primary;
	}
	
	public Star generateSecondary() {
		if (!multipleStars) {
			throw new IllegalArgumentException("This system has only one star");
		}
		secondary = new Star();
		secondary.setName(systemName+" Beta");
		
		secondary.setForm(StarForm.Star);
		secondary.setClassification(StarClass.VI);
		secondary.setType("M5");
		
		// This is a place holder value.
		secondary.setParentId(1);
		secondary.setDistance(Die.d10(10)*10000);
		
		return secondary;
	}

	public Star generateTertiary() {
		if (!multipleStars) {
			throw new IllegalArgumentException("This system has only one star");
		}
		tertiary = new Star();
		tertiary.setName(systemName+" Gamma");
		
		tertiary.setForm(StarForm.WhiteDwarf);
		tertiary.setClassification(StarClass.D);
		tertiary.setType("D");
		
		tertiary.setParentId(2);
		tertiary.setDistance(Die.d10(5)*1000);
		
		return tertiary;
	}
}

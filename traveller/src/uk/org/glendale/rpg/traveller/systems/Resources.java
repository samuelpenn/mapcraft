package uk.org.glendale.rpg.traveller.systems;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.utils.Die;

public class Resources {
	private static final String VEGETABLES = "Vegetables";
	private static final String MEAT = "Meat";
	private static final String SEAFOOD = "Seafood";
	private static final String ALGAE = "Algae";
	private static final String WOOD = "Wood";
	
	private static final String FERRIC = "Ferric ore";
	private static final String CARBONIC = "Carbonic ore";
	private static final String SILICATE = "Silicate ore";
	private static final String AQUAM = "Aquam solution";
	private static final String AURAM = "Auram gas";
	
	private static final String KRYSITE = "Krysite ore";
	private static final String MAGNESITE = "Magnesite ore";
	private static final String ERICATE = "Ericate ore";
	
	private static final String HELIACATE = "Heliacate ore";
	private static final String ACENITE = "Acenite ore";
	private static final String PARDENIC = "Pardenic ore";
	
	private static final String VARDONNEK = "Vardonnek ore";
	private static final String LARATHIC = "Larathic ore";
	private static final String XITHANTITE = "Xithantite ore";
	
	private static final String DORIC = "Doric crystals";
	private static final String ISKINE = "Iskine crystals";
	private static final String OORCINE = "Oorcine ices";
	
	private static final String REGIAM = "Regiam gas";
	private static final String TRITANIUM = "Tritanium gas";
	private static final String SYNTHOSIUM = "Synthosium gas";
	
	/**
	 * Resources for Gaian type worlds.
	 */
	private static void setGaian(ObjectFactory factory, StarSystem system, Planet planet) {
		
		System.out.println("Setting Gaian for ["+planet.getName()+"]");
		
		// Basic mineral resources
		planet.addResource(SILICATE, 35+Die.d20(2));
		planet.addResource(CARBONIC, 35+Die.d20(2));
		planet.addResource(FERRIC, planet.getRadius()/150 + Die.d20(2));
		
		planet.addResource(AQUAM, (planet.getHydrographics()*planet.getHydrographics())/10000);
		switch (planet.getAtmospherePressure()) {
		case None: case Trace:
			break;
		case Thin: case VeryThin:
			planet.addResource(AURAM, 5+Die.d6(1));
			break;
		default:
			planet.addResource(AURAM, 20+Die.d12(2));
			break;
		}
		
		if (Die.d4() == 1) planet.addResource(KRYSITE, 30+Die.d12(2));
		if (Die.d4() == 1) planet.addResource(MAGNESITE, 5+Die.d4());
		if (Die.d6() == 1) planet.addResource(VARDONNEK, 10+Die.d8(2));
		
		// Basic organic resources.
		switch (planet.getLifeLevel()) {
		case None:
			// No organic resources.
			break;
		case Proteins:
			planet.addResource(ALGAE, 5+Die.d6(2));
			break;
		case Protozoa:
			planet.addResource(ALGAE, 15+Die.d12(2));
			break;
		case Metazoa:
			switch (Die.d6()) {
			case 1: case 2: case 3:
				planet.addResource("Algae", 35+Die.d20(2));
				break;
			case 4: case 5:
				planet.addResource("Algae", 25+Die.d20(2));
				planet.addResource("Jellyfish", Die.d20(1));
				break;
			case 6:
				planet.addResource("Algae", 15+Die.d20(2));
				planet.addResource("Jellyfish", 15+Die.d20(1));
				planet.addResource("Seaweed", 10+Die.d20(1));
				break;
			}
			break;
		case ComplexOcean:
			planet.addResource("Algae", 5+Die.d12(2));
			planet.addResource("Jellyfish", 10+Die.d12(3));
			planet.addResource("Seaweed", 10+Die.d12(2));
			planet.addResource("Shellfish", 30+Die.d20(2));
			planet.addResource("Fish", 40+Die.d20(2));
			break;
		case SimpleLand:
			planet.addResource("Algae", Die.d6(2));
			planet.addResource("Jellyfish", 5+Die.d6(2));
			planet.addResource("Seaweed", 10+Die.d6(2));
			planet.addResource("Shellfish", 20+Die.d20(2));
			planet.addResource("Fish", 40+Die.d20(3));
			
			planet.addResource("Vegetables", 20+Die.d20(2));
			if (Die.d6() < 5) planet.addResource("Fruits", 10+Die.d12(2));
			if (Die.d6() < 3) planet.addResource("Wood", 5+Die.d6(2));
			break;
		case ComplexLand:
			if (Die.d6() < 4) planet.addResource("Jellyfish", 5+Die.d6(2));
			if (Die.d6() < 4) planet.addResource("Seaweed", 10+Die.d6(2));
			planet.addResource("Shellfish", 20+Die.d20(2));
			planet.addResource("Fish", 40+Die.d20(3));
			planet.addResource("Grain", 35+Die.d20(3));
			planet.addResource("Vegetables", 35+Die.d20(3));
			planet.addResource("Fruits", 20+Die.d20(2));
			planet.addResource("Wood", 20+Die.d20(2));
			planet.addResource("Meat", 10+Die.d12(2));
			break;
		case Extensive:
			if (Die.d6() < 3) planet.addResource("Jellyfish", 5+Die.d6(2));
			if (Die.d6() < 3) planet.addResource("Seaweed", 10+Die.d6(2));
			planet.addResource("Shellfish", 20+Die.d20(2));
			planet.addResource("Fish", 40+Die.d20(3));
			planet.addResource("Grain", 40+Die.d20(3));
			planet.addResource("Vegetables", 40+Die.d20(3));
			planet.addResource("Fruits", 30+Die.d20(2));
			planet.addResource("Wood", 30+Die.d20(2));
			planet.addResource("Meat", 30+Die.d20(2));
			break;
		}		
	}
	
	/**
	 * Set the resources on the given planet.
	 */
	public static void setResources(ObjectFactory factory, StarSystem system, Planet planet) {
		System.out.println(planet.getType());
		switch (planet.getType()) {
		case Gaian:
		case EoGaian:
		case MesoGaian:
		case PostGaian:
		case ArchaeoGaian:
		case GaianTundral:
			setGaian(factory, system, planet);
			break;
		}
	}
	
	public static void main(String[] args) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		Planet				planet = factory.getPlanet(224128);
		StarSystem			system = null;
		
		setResources(factory, system, planet);
		planet.persist();
	}

}

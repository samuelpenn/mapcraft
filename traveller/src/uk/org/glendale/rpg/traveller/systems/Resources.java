package uk.org.glendale.rpg.traveller.systems;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.TradeCode;
import uk.org.glendale.rpg.utils.Die;

/**
 * Defines resources for a world, based on the world type. This class
 * only has static methods.
 * 
 * @see http://mapcraft.glendale.org.uk/worldgen/planets/lifelevel
 * 
 * @author Samuel Penn
 */
public class Resources {
	private static final String VEGETABLES = "Vegetables";
	private static final String MEAT = "Meat";
	private static final String SEAFOOD = "Seafood";
	private static final String WOOD = "Wood";
	
	private static final String FERRIC = "Ferric ore";
	private static final String CARBONIC = "Carbonic ore";
	private static final String SILICATE = "Silicate ore";
	private static final String AQUAM = "Aquam";
	private static final String AURAM = "Auram";
	
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
	 * Add resources to the planet based on the atmosphere composition
	 * and density. Special atmospheric resources aren't included here.
	 */
	private static void basicAtmosphere(ObjectFactory factory, StarSystem system, Planet planet) {
		String	airResource = null;
		int		air = 0;

		switch (planet.getAtmosphereType()) {
		case WaterVapour: case Primordial:
			air = 10;
			break;
		case OrganicToxins: case Pollutants:
			airResource = AURAM;
			air = 40;
			break;
		case LowOxygen: case HighCarbonDioxide: case Tainted:
			airResource = AURAM;
			air = 80;
			break;
		case HighOxygen: case Standard:
			airResource = AURAM;
			air = 100;
			break;
		default:
			air = 0;
		}
		
		switch (planet.getAtmospherePressure()) {
		case None:
			air = 0;
			break;
		case Trace:
			air *= 0.1;
			break;
		case VeryThin:
			air *= 0.5;
			break;
		case Thin:
			air *= 0.75;
			break;
		case Dense:
			air *= 1.5;
			break;
		case VeryDense:
			air *= 2.0;
			break;
		case SuperDense:
			air *= 4.0;
			break;
		}
		
		if (air > 100) air = 100;
		
		if (air > 0 && airResource != null) {
			planet.addResource(airResource, air);
		}
	}
	
	private static void basicHydrographics(ObjectFactory factory, StarSystem system, Planet planet) {
		String	waterResource = null;
		int		water = planet.getHydrographics()*2;

		if (water > 100) water = 100;
		
		if (planet.hasTradeCode(TradeCode.Fl)) {
			// Non-water oceans;
			water = 0;
		} else if (water > 0) {
			waterResource = AQUAM;
		}
		
		if (water > 0 && waterResource != null) {
			planet.addResource(waterResource, water);
		}
	}

	// Organics
	private static final String		BASE_ORGANICS = "Base organics";

	// Archaean
	private static final String		SIMPLE_ORGANICS = "Simple organics";
	private static final String		METAZOA = "Metazoa";

	// Metazoa
	private static final String		SPONGES = "Sponges";
	private static final String		ALGAE = "Algae";
	private static final String		PLANKTON = "Plankton";
	private static final String		SEAWEED = "Seaweed";
	private static final String		JELLYFISH = "Jellyfish";

	// Complex Ocean
	private static final String		SIMPLE_MARINE = "Simple marine";
	private static final String		FISH = "Fish";
	private static final String		CRUSTACEAN = "Shellfish";

	private static void basicLife(ObjectFactory factory, StarSystem system, Planet planet) {
		// Basic organic resources.
		int		life = 0;

		switch (planet.getLifeLevel()) {
		case None:
			// No organic resources.
			break;
		case Organic:
			// Basic organic compounds. No actual living things.
			life = Die.d4() + planet.getHydrographics()/10;
			
			switch (planet.getTemperature()) {
			case VeryCold:
				life -= 2;
				break;
			case Cold:
				life -= 1;
				break;
			case Cool: case Standard:
				break;
			case Warm:
				life += 1;
				break;
			case Hot: case VeryHot:
				life += 2;
				break;
			default:
				life -= 3;
				break;
			}
			// Zero doesn't mean no life, just no modifier.
			if (life < 0) life = 0;
			
			switch (Die.d6()) {
			case 1: case 2: case 3:
				planet.addResource(BASE_ORGANICS, Die.d6()+life*5);
				break;
			case 4: case 5:
				planet.addResource(BASE_ORGANICS, Die.d6(3)+life*5);
				break;
			case 6:
				planet.addResource(BASE_ORGANICS, Die.d12(3)+life*5);
				planet.addResource(SIMPLE_ORGANICS, Die.d6());
				break;
			}
			break;
		case Archaean:
			// Simple organisms, mostly bacteria dominated
			life = Die.d4() + planet.getHydrographics()/10;
			
			switch (planet.getTemperature()) {
			case Cold:
				life -= 1;
				break;
			case Cool: case Standard:
				break;
			case Warm: case Hot:
				life += 1;
				break;
			case VeryHot:
				life -= 1;
				break;
			default:
				life -= 3;
				break;
			}
			// Zero doesn't mean no life, just no modifier.
			if (life < 0) life = 0;
			switch (Die.d6()) {
			case 1:
				// Early period
				planet.addResource(BASE_ORGANICS, Die.d6(2)+life*3);
				planet.addResource(SIMPLE_ORGANICS, Die.d6(3)+life*4);
				break;
			case 2: case 3:
				// Early period
				planet.addResource(BASE_ORGANICS, Die.d6()+life);
				planet.addResource(SIMPLE_ORGANICS, Die.d12(3)+life*5);
				break;
			case 4: case 5:
				// Mid period
				planet.addResource(SIMPLE_ORGANICS, Die.d12(3)+life*5);
				planet.addResource(METAZOA, Die.d12(2)+life*3);
				break;
			case 6:
				// Late period
				planet.addResource(SIMPLE_ORGANICS, Die.d6(2)+life*2);
				planet.addResource(METAZOA, Die.d12(3)+life*5);
				planet.addResource(SPONGES, Die.d12());
				planet.addResource(ALGAE, Die.d12());
				break;
			}
			break;
		case Aerobic:
			// Oxygen breathing life
			life = Die.d4() + planet.getHydrographics()/10;
			
			switch (planet.getTemperature()) {
			case Cold:
				life -= 1;
				break;
			case Cool: case Standard:
				break;
			case Warm: case Hot:
				life += 1;
				break;
			case VeryHot:
				life -= 1;
				break;
			default:
				life -= 3;
				break;
			}

			switch (Die.d6()) {
			case 1: case 2: case 3:
				planet.addResource(METAZOA, Die.d6(2)+life);
				planet.addResource(SPONGES, Die.d12(3)+life*5);
				planet.addResource(ALGAE, Die.d12(3)+life*5);
				break;
			case 4: case 5:
				planet.addResource(SPONGES, Die.d6(2)+life*3);
				planet.addResource(ALGAE, Die.d6(2)+life*3);
				planet.addResource(SEAWEED, Die.d6(3)+life*5);
				planet.addResource(PLANKTON, Die.d12(3)+life*5);
				break;
			case 6:
				planet.addResource(SPONGES, Die.d6()+life);
				planet.addResource(ALGAE, Die.d6()+life);
				planet.addResource(SEAWEED, Die.d6(3)+life*3);
				planet.addResource(PLANKTON, Die.d6(3)+life*3);
				planet.addResource(JELLYFISH, Die.d12(2)+life*3);
				planet.addResource(SIMPLE_MARINE, Die.d12(3)+life*4);
				break;
			}
			break;
		case ComplexOcean:
			// Complex ocean life
			life = Die.d4() + planet.getHydrographics()/10;

			switch (planet.getTemperature()) {
			case Cold:
				life -= 1;
				break;
			case Cool: case Standard:
				break;
			case Warm: case Hot:
				life += 1;
				break;
			case VeryHot:
				life -= 1;
				break;
			default:
				life -= 3;
				break;
			}
			switch (Die.d6()) {
			case 1: case 2: case 3:
				planet.addResource(SEAWEED, Die.d6()+life);
				planet.addResource(PLANKTON, Die.d6()+life);
				planet.addResource(JELLYFISH, Die.d6(2)+life*2);
				planet.addResource(SIMPLE_MARINE, Die.d12(3)+life*5);
				planet.addResource(CRUSTACEAN, Die.d6(3)+life*3);
				planet.addResource(FISH, Die.d6(2)+life*3);
				break;
			case 4: case 5:
				planet.addResource(SEAWEED, Die.d6()+life);
				planet.addResource(PLANKTON, Die.d6()+life);
				planet.addResource(JELLYFISH, Die.d6(2)+life*2);
				planet.addResource(SIMPLE_MARINE, Die.d6(3)+life*3);
				planet.addResource(CRUSTACEAN, Die.d6(3)+life*4);
				planet.addResource(FISH, Die.d12(3)+life*5);
				break;
			case 6:
				planet.addResource(SEAWEED, Die.d6()+life);
				planet.addResource(PLANKTON, Die.d6()+life);
				planet.addResource(JELLYFISH, Die.d6(2)+life*2);
				planet.addResource(SIMPLE_MARINE, Die.d6(3)+life*3);
				planet.addResource(CRUSTACEAN, Die.d6(3)+life*4);
				planet.addResource(FISH, Die.d12(4)+life*5);
				break;
			}
			break;
		case SimpleLand:
			life = Die.d4() + planet.getHydrographics()/10;
			planet.addResource(SEAWEED, Die.d6(2)+life);
			planet.addResource(JELLYFISH, Die.d6(2)+life);
			planet.addResource(SIMPLE_MARINE, Die.d6(2)+life*3);
			planet.addResource(CRUSTACEAN, Die.d6(3)+life*4);
			planet.addResource(FISH, Die.d12(5)+life*5);
			switch (Die.d6()) {
			case 1: case 2: case 3:
				break;
			case 4: case 5:
				break;
			case 6:
				break;
			}
			
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
			break;
		}		
		
	}
	
	
	
	/**
	 * Resources for Gaian type worlds. This includes MesoGaian, EoGaian,
	 * Gaian, GaianTundral, ArchaeoGaian, PostGaian
	 * 
	 */
	private static void setGaian(ObjectFactory factory, StarSystem system, Planet planet) {		
		System.out.println("Setting Gaian for ["+planet.getName()+"/"+planet.getType()+"]");
		
		// Basic mineral resources
		planet.addResource(SILICATE, 35+Die.d20(2));
		planet.addResource(CARBONIC, 35+Die.d20(2));
		planet.addResource(FERRIC, planet.getRadius()/150 + Die.d20(2));

		if (Die.d4() == 1) planet.addResource(KRYSITE, 30+Die.d12(2));
		if (Die.d4() == 1) planet.addResource(MAGNESITE, 5+Die.d4());
		if (Die.d6() == 1) planet.addResource(VARDONNEK, 10+Die.d8(2));
		
		basicAtmosphere(factory, system, planet);
		basicHydrographics(factory, system, planet);
		basicLife(factory, system, planet);
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
		Planet				planet = factory.getPlanet(224138);
		StarSystem			system = null;
		
		setResources(factory, system, planet);
		planet.persist();
	}

}

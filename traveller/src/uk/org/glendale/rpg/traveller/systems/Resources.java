package uk.org.glendale.rpg.traveller.systems;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.systems.codes.AtmosphereType;
import uk.org.glendale.rpg.traveller.systems.codes.LifeType;
import uk.org.glendale.rpg.traveller.systems.codes.StarClass;
import uk.org.glendale.rpg.traveller.systems.codes.Temperature;
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
	private static final String WOOD = "Wood";
	
	private static final String FERRIC = "Ferric ore";
	private static final String CARBONIC = "Carbonic ore";
	private static final String SILICATE = "Silicate ore";
	private static final String AQUAM = "Water";
	private static final String AURAM = "Air";
	
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
	
	private static final String REGIAM = "Regiam gas";            // Hydrogen/Helium gases
	private static final String TRITANIUM = "Tritanium gas";      // Radioactive gases (Tritium)
	private static final String SYNTHOSIUM = "Synthosium gas";    // Complex organic molecules, extreme cold.
	private static final String HALOGEN = "Halogen gas";          // Highly reactive gases (Chlorine, Fluorine)
	
	private static final String PETROLEUM = "Petroleum";          // Liquid hydrocarbons
	private static final String METHANE = "Methane gas";          // Natural gas (Methane)
	private static final String ACIDS = "Acid";                   // Acids                  

	
	/**
	 * Add resources to the planet based on the atmosphere composition
	 * and density. Special atmospheric resources aren't included here.
	 */
	private static void basicAtmosphere(ObjectFactory factory, Star star, Planet planet) {
		String	airResource = null;
		int		air = 0;

		switch (planet.getAtmosphereType()) {
		case WaterVapour:
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
		case Chlorine:
			airResource = HALOGEN;
			air = 50;
			break;
		case Flourine:
			airResource = HALOGEN;
			air = 80;
			break;
		case Primordial:
			airResource = METHANE;
			air = 60;
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
	
	private static void basicHydrographics(ObjectFactory factory, Star star, Planet planet) {
		String	waterResource = null;
		int		water = planet.getHydrographics()*2;

		if (water > 100) water = 100;
		
		if (planet.hasTradeCode(TradeCode.Fl)) {
			// Non-water oceans;
			waterResource = ACIDS;
			water /= 3;
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
	
	// Simple Land
	private static final String		MOSS = "Moss";
	private static final String		FERNS = "Ferns";
	private static final String		FUNGI = "Fungi";
	private static final String		INSECTS = "Insects";
	private static final String		AMPHIBIANS = "Amphibians";
	private static final String		TINY_ANIMALS = "Tiny animals";
	private static final String		SMALL_ANIMALS = "Small animals";
	private static final String		MEDIUM_ANIMALS = "Medium animals";
	private static final String		LARGE_ANIMALS = "Large animals";
	private static final String		HUGE_ANIMALS = "Huge animals";
	private static final String		FRUITS = "Fruits";
	private static final String		GRAIN = "Grain";
	
	/**
	 * Get resources based on a world with Organic level of life.
	 */
	private static void organicLife(ObjectFactory factory, Star star, Planet planet) {
		int		life = 0;
		switch (planet.getTemperature()) {
		case UltraCold:	     life = 2;  break;
		case ExtremelyCold:  life = 5;  break;
		case VeryCold:       life = 10;	break;
		case Cold:           life = 15; break;
		case Cool:           life = 20;	break;
		case Standard:       life = 25;	break;
		case Warm:           life = 35; break;
		case Hot:            life = 15; break;
		case VeryHot:        life = 5;  break;
		case ExtremelyHot:   life = 2;  break;
		case UltraHot:       life = 1;  break;
		}
	}

	private static void basicLife(ObjectFactory factory, Star star, Planet planet) {
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
				planet.addResource(PETROLEUM, Die.d4()+life);
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
				planet.addResource(PETROLEUM, Die.d4()+life);
				break;
			case 4: case 5:
				planet.addResource(SEAWEED, Die.d6()+life);
				planet.addResource(PLANKTON, Die.d6()+life);
				planet.addResource(JELLYFISH, Die.d6(2)+life*2);
				planet.addResource(SIMPLE_MARINE, Die.d6(3)+life*3);
				planet.addResource(CRUSTACEAN, Die.d6(3)+life*4);
				planet.addResource(FISH, Die.d12(3)+life*5);
				planet.addResource(PETROLEUM, Die.d6()+life);
				break;
			case 6:
				planet.addResource(SEAWEED, Die.d6()+life);
				planet.addResource(PLANKTON, Die.d6()+life);
				planet.addResource(JELLYFISH, Die.d6(2)+life*2);
				planet.addResource(SIMPLE_MARINE, Die.d6(3)+life*3);
				planet.addResource(CRUSTACEAN, Die.d6(3)+life*4);
				planet.addResource(FISH, Die.d12(4)+life*5);
				planet.addResource(PETROLEUM, Die.d8()+life);
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
				planet.addResource(MOSS, Die.d6(2) + life*5);
				planet.addResource(PETROLEUM, Die.d6(2)+life);
				break;
			case 4: case 5:
				planet.addResource(MOSS, Die.d12(3) + life*5);
				planet.addResource(FERNS, Die.d12(2) + life*3);
				planet.addResource(FUNGI, Die.d12(2) + life*3);
				planet.addResource(INSECTS, Die.d12(2) + life);
				planet.addResource(PETROLEUM, Die.d6(2)+life*2);
				break;
			case 6:
				planet.addResource(FERNS, Die.d12(3) + life*5);
				planet.addResource(FUNGI, Die.d12(2) + life*3);
				planet.addResource(WOOD, Die.d6(2) + life);
				planet.addResource(INSECTS, Die.d12(2) + life);
				planet.addResource(SMALL_ANIMALS, Die.d6(2) + life);
				planet.addResource(PETROLEUM, Die.d6(2)+life*3);
				break;
			}
			break;
		case ComplexLand:
			life = Die.d4() + planet.getHydrographics()/10;
			planet.addResource(SEAWEED, Die.d6(2)+life);
			planet.addResource(JELLYFISH, Die.d6(2)+life);
			planet.addResource(SIMPLE_MARINE, Die.d6(2)+life*3);
			planet.addResource(CRUSTACEAN, Die.d6(3)+life*4);
			planet.addResource(FISH, Die.d12(5)+life*5);
			planet.addResource(PETROLEUM, Die.d12(2)+life*3);
			switch (Die.d6()) {
			case 1: case 2: case 3:
				planet.addResource(FERNS, Die.d6(2) + life*3);
				planet.addResource(FUNGI, Die.d6(2) + life*3);
				planet.addResource(WOOD, Die.d12(2) + life*4);
				planet.addResource(INSECTS, Die.d6(2) + life);
				planet.addResource(TINY_ANIMALS, Die.d12(2) + life*2);
				planet.addResource(SMALL_ANIMALS, Die.d6() + life);
				break;
			case 4: case 5:
				planet.addResource(FERNS, Die.d6(2) + life*3);
				planet.addResource(FUNGI, Die.d6(2) + life*3);
				planet.addResource(WOOD, Die.d12(2) + life*5);
				planet.addResource(VEGETABLES, Die.d6(2) + life);
				planet.addResource(INSECTS, Die.d6(2) + life);
				planet.addResource(TINY_ANIMALS, Die.d12(3) + life*3);
				planet.addResource(SMALL_ANIMALS, Die.d6(2) + life);
				break;
			case 6:
				planet.addResource(WOOD, Die.d12(3) + life*5);
				planet.addResource(VEGETABLES, Die.d12(3) + life*5);
				planet.addResource(FRUITS, Die.d12(2) + life*3);
				planet.addResource(SMALL_ANIMALS, Die.d12(3) + life*5);
				planet.addResource(MEDIUM_ANIMALS, Die.d6(2) + life*2);
				break;
			}
			break;
		case Extensive:
			life = Die.d4() + planet.getHydrographics()/10;
			planet.addResource(SEAWEED, Die.d6(2)+life);
			planet.addResource(JELLYFISH, Die.d6(2)+life);
			planet.addResource(SIMPLE_MARINE, Die.d6(2)+life*3);
			planet.addResource(CRUSTACEAN, Die.d6(3)+life*4);
			planet.addResource(FISH, Die.d12(5)+life*5);
			planet.addResource(WOOD, Die.d12(3) + life*5);
			planet.addResource(VEGETABLES, Die.d12(3) + life*5);
			planet.addResource(FRUITS, Die.d12(2) + life*3);
			planet.addResource(GRAIN, Die.d12(3) + life*3);
			planet.addResource(SMALL_ANIMALS, Die.d12(3) + life*5);
			planet.addResource(MEDIUM_ANIMALS, Die.d6(3) + life*3);
			planet.addResource(LARGE_ANIMALS, Die.d6() + life*2);
			planet.addResource(PETROLEUM, Die.d12(3)+life*4);
			break;
		}		
		
	}
	
	
	
	/**
	 * Resources for Gaian type worlds. This includes MesoGaian, EoGaian,
	 * Gaian, GaianTundral, ArchaeoGaian, PostGaian
	 * 
	 */
	private static void setGaian(ObjectFactory factory, Star star, Planet planet) {
		// Basic mineral resources
		planet.addResource(SILICATE, 35+Die.d20(2));
		planet.addResource(CARBONIC, 35+Die.d20(2));
		planet.addResource(FERRIC, planet.getRadius()/150 + Die.d20(2));

		if (Die.d4() == 1) planet.addResource(KRYSITE, 30+Die.d12(2));
		if (Die.d4() == 1) planet.addResource(MAGNESITE, 5+Die.d4());
		if (Die.d6() == 1) planet.addResource(VARDONNEK, 10+Die.d8(2));
		
		basicAtmosphere(factory, star, planet);
		basicHydrographics(factory, star, planet);
		basicLife(factory, star, planet);
	}
	
	private static void setBelt(ObjectFactory factory, Star star, Planet planet) {
		switch (planet.getType()) {
		case AsteroidBelt:
			planet.addResource(SILICATE, 20+Die.d20(4));
			if (Die.d4()==1) planet.addResource(KRYSITE, 10+Die.d12(2));
			planet.addResource(CARBONIC, 20+Die.d20(3));
			planet.addResource(FERRIC, 10+Die.d20(2));
			if (Die.d4()==1) planet.addResource(VARDONNEK, 10+Die.d12(2));
			break;
		case IceBelt:
			if (Die.d3() == 1) planet.addResource(SILICATE, Die.d6(3));
			planet.addResource(CARBONIC, 10+Die.d12(2));
			planet.addResource(AQUAM, 40+Die.d20(4));
			if (Die.d2() == 1) planet.addResource(AURAM, Die.d12(3));
			if (Die.d3() == 1) {
				planet.addResource(DORIC, 10+Die.d20(2)); 
			} else {
				planet.addResource(DORIC, Die.d6(2));
			}
			break;
		case OortCloud:
			planet.addResource(CARBONIC, 5+Die.d6(2));
			planet.addResource(AQUAM, 20+Die.d20(4));
			if (Die.d3() == 1) planet.addResource(AURAM, Die.d6(2));
			planet.addResource(DORIC, 10+Die.d20(2));
			planet.addResource(OORCINE, 10+Die.d12(3));
			break;
		}
	}
	
	private static void setAsteroid(ObjectFactory factory, Star star, Planet planet) {
		int		silicate = 0, krysite = 0, magnesite = 0, ericate =  0;
		int		carbonic = 0, heliacate = 0, acenite = 0, pardenic = 0;
		int		ferric = 0, vardonnek = 0, larathic = 0, xithantite = 0;
		int		water = 0, doric = 0, iskine = 0, oorcine = 0;
		int		air = 0, regiam = 0, tritanium = 0, synthosium = 0;
		int		petroleum = 0;
		
		switch (planet.getType()) {
		case Vulcanian:
			silicate = Die.d20(4);
			ferric = Die.d20(3);
			vardonnek = ferric/2;
			larathic = Die.d6(3)*2;
			break;
		case Silicaceous:
			silicate = 20 + Die.d20(5);
			krysite = planet.getRadius()/10 + Die.d4();
			ferric = planet.getRadius()/20;
			break;
		case Sideritic:
			ferric = 40 + Die.d20(5);
			vardonnek = 20 + Die.d20(4);
			silicate = Die.d20(2);
			if (star != null && star.getStarClass() == StarClass.D) {
				xithantite = planet.getRadius()/5;
			}
			break;
		case Basaltic:
			silicate = 20 + Die.d20(4);
			krysite = planet.getRadius()/5 + Die.d6();
			magnesite = planet.getRadius()/10 + Die.d4();
			ferric = planet.getRadius()/20;
			if (star != null && star.getStarClass() == StarClass.D) {
				xithantite = planet.getRadius()/10;
			}
			break;
		case Carbonaceous:
			silicate = Die.d20(3);
			ferric = Die.d10(3);
			carbonic = 40 + Die.d20(4);
			if (planet.getTemperature().isHotterThan(Temperature.Hot)) {
				heliacate = Die.d20(3);
			}
			break;
		case Enceladean:
			water = 40 + Die.d20(4);
			if (Die.d3() == 1) doric = Die.d12(3);
			if (Die.d4() == 1) iskine = Die.d12(3);
			air = Die.d12(3);
			break;
		case Mimean:
			water = 40 + Die.d20(4);
			if (Die.d3() == 1) doric = Die.d12(2);
			if (Die.d3() == 1) iskine = Die.d12(3);
			air = Die.d12(2);
			break;
		case Oortean:
			water = Die.d20(5);
			if (Die.d2() == 1) carbonic = Die.d4(2);
			oorcine = Die.d20(2);
			if (Die.d4() == 1) synthosium = Die.d6(2);
			break;
		default:
			silicate = Die.d20();
		}

		if (silicate > 0) planet.addResource(SILICATE, silicate);
		if (krysite > 0) planet.addResource(KRYSITE, krysite);
		if (magnesite > 0) planet.addResource(MAGNESITE, magnesite);
		if (ericate > 0) planet.addResource(ERICATE, ericate);
		if (carbonic > 0) planet.addResource(CARBONIC, carbonic);
		if (heliacate > 0) planet.addResource(HELIACATE, heliacate);
		if (acenite > 0) planet.addResource(ACENITE, acenite);
		if (pardenic > 0) planet.addResource(PARDENIC, pardenic);
		if (ferric > 0) planet.addResource(FERRIC, ferric);
		if (vardonnek > 0) planet.addResource(VARDONNEK, vardonnek);
		if (larathic > 0) planet.addResource(LARATHIC, larathic);
		if (xithantite > 0) planet.addResource(XITHANTITE, xithantite);
		if (water > 0) planet.addResource(AQUAM, water);
		if (doric > 0) planet.addResource(DORIC, doric);
		if (iskine > 0) planet.addResource(ISKINE, iskine);
		if (oorcine > 0) planet.addResource(OORCINE, oorcine);
		if (air > 0) planet.addResource(AURAM, air);
		if (regiam > 0) planet.addResource(REGIAM, regiam);
		if (tritanium > 0) planet.addResource(TRITANIUM, tritanium);
		if (synthosium > 0) planet.addResource(SYNTHOSIUM, synthosium);
		if (petroleum > 0) planet.addResource(PETROLEUM, petroleum);
	}
	
	private static void setIcyDwarf(ObjectFactory factory, Star star, Planet planet) {
		switch (planet.getType()) {
		case Kuiperian:
			planet.addResource(AQUAM, Die.d12(3));
			planet.addResource(DORIC, Die.d6(2));
			planet.addResource(ISKINE, 20+Die.d20(3));
			planet.addResource(OORCINE, 10+Die.d12(3));
			if (Die.d3()==1) planet.addResource(TRITANIUM, Die.d6(3));
			if (Die.d4()==1) planet.addResource(SYNTHOSIUM, Die.d6(2));
			break;
		case Iapetean:
			planet.addResource(AQUAM, 10 + Die.d20(3));
			planet.addResource(DORIC, Die.d12(3));
			break;
		case Tritonic:
			planet.addResource(AQUAM, Die.d20(3));
			planet.addResource(DORIC, 10+Die.d6(2));
			planet.addResource(ISKINE, 15+Die.d20(3));
			if (Die.d6()==1) planet.addResource(TRITANIUM, Die.d6(2));
			break;
		case Europan:
			planet.addResource(AQUAM, 20+Die.d20(5));
			break;
		case Stygian:
			planet.addResource(AQUAM, Die.d12(3));
			planet.addResource(DORIC, Die.d20(4));
			planet.addResource(ISKINE, Die.d12(3));
			if (Die.d3()==1) planet.addResource(OORCINE, Die.d8(2));
			if (Die.d6()==1) planet.addResource(TRITANIUM, Die.d6(2));
			if (Die.d10()==1) planet.addResource(SYNTHOSIUM, Die.d4());
			break;
		case LithicGelidian:
			planet.addResource(AQUAM, 10 + Die.d20(3));
			planet.addResource(SILICATE, 10 + Die.d20(3));
			if (Die.d4()==1) planet.addResource(DORIC, Die.d8(2)-1);
			break;
		}
	}

	private static void setRockyDwarf(ObjectFactory factory, Star star, Planet planet) {
		switch (planet.getType()) {
		case Cerean:
			planet.addResource(SILICATE, 10+Die.d20(2));
			planet.addResource(CARBONIC, 15+Die.d20(3));
			planet.addResource(AQUAM, 10+Die.d12(4));
			break;
		case Hadean:
			planet.addResource(SILICATE, 20+Die.d20(2));
			planet.addResource(FERRIC, 30+Die.d20(4));
			planet.addResource(VARDONNEK, 10+Die.d20(3));
			planet.addResource(LARATHIC, 15+Die.d12(4));
			planet.addResource(ERICATE, 15+Die.d12(3));
			if (Die.d4()==1) planet.addResource(HELIACATE, Die.d10(2));
			break;
		case Vesperian:
			planet.addResource(FERRIC, 10+Die.d20(2));
			planet.addResource(SILICATE, 20+Die.d20(2));
			planet.addResource(AQUAM, Die.d12(2));
			break;
		case Vestian:
			planet.addResource(SILICATE, 40+Die.d20(4));
			if (Die.d2()==1) planet.addResource(KRYSITE, 10+Die.d12(2));
			if (Die.d6()==1) planet.addResource(KRYSITE, Die.d12());
			if (Die.d4()==1) planet.addResource(AQUAM, Die.d10(2));
			break;
		case Hephaestian:
			planet.addResource(SILICATE, 20+Die.d20(2));
			planet.addResource(KRYSITE, 15+Die.d12(4));
			planet.addResource(MAGNESITE, 15+Die.d20(3));
			if (Die.d2()==1) {
				planet.addResource(CARBONIC, Die.d10(3));
				planet.addResource(ACENITE, Die.d6(2));
			}
			break;
		case Ferrinian:
			planet.addResource(SILICATE, 30+Die.d20(3));
			planet.addResource(FERRIC, 10+Die.d20(3));
			planet.addResource(VARDONNEK, 10+Die.d20(3));
			if (planet.getTemperature().isHotterThan(Temperature.Hot)) {
				planet.addResource(LARATHIC, 10+Die.d12(3));
			}
			if (planet.getRadius() > 8000) {
				planet.addResource(ERICATE, 15+Die.d12(3));
			}
			break;
		case Selenian:
			planet.addResource(SILICATE, Die.d20(3));
			if (Die.d4()==1) planet.addResource(TRITANIUM, Die.d6());
			break;
		}
	}
	private static void setTitanian(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(AQUAM, 10+Die.d20(3));
		planet.addResource(ISKINE, 10+Die.d12(3));
		planet.addResource(PETROLEUM, Die.d12(2));
	}

	private static void setUtgardian(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(AQUAM, 10+Die.d20(2));
		planet.addResource(ISKINE, 20+Die.d20(4));
		planet.addResource(REGIAM, Die.d20(2));		
	}

	private static void setJovian(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(AURAM, 40+Die.d20(5));
		planet.addResource(REGIAM, 10+Die.d20(2));
		planet.addResource(TRITANIUM, 10+Die.d20(2));
		planet.addResource(AQUAM, Die.d10(2));
	}
	
	private static void setChlorine(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(SILICATE, Die.d20(4));
		planet.addResource(KRYSITE, Die.d12(4));
		planet.addResource(MAGNESITE, Die.d12(2));
		planet.addResource(FERRIC, Die.d20(2));
		planet.addResource(REGIAM, Die.d20(3));
		if (Die.d4()==1) planet.addResource(SYNTHOSIUM, Die.d12(2));
	}
	
	private static void setSulphur(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(SILICATE, Die.d20(4));
		planet.addResource(FERRIC, Die.d20(2));
		
	}

	private static void setArean(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(SILICATE, Die.d20(4));
		planet.addResource(KRYSITE, Die.d12(4));
		planet.addResource(MAGNESITE, Die.d12(2));
		planet.addResource(FERRIC, Die.d20(2));
		if (Die.d2()==1) planet.addResource(ACENITE, Die.d12(3));
		
		switch (planet.getLifeLevel()) {
		case Organic:
			planet.addResource(BASE_ORGANICS, Die.d12());
			break;
		case Archaean:
			planet.addResource(BASE_ORGANICS, Die.d6(3)*3);
			planet.addResource(SIMPLE_ORGANICS, Die.d6(4)*5);
			break;
		case Aerobic:
			break;
		case ComplexOcean:
			break;
		case SimpleLand:
			break;
		case ComplexLand:
			break;
		case Extensive:
			break;
		}
	}
	
	private static void setHotAtmosphere(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(SILICATE, Die.d20(4));
		planet.addResource(FERRIC, Die.d20(2));
		
		switch (planet.getType()) {
		case Cytherean:
			if (Die.d3()==1) planet.addResource(ACENITE, Die.d10(2));
			planet.addResource(PARDENIC, Die.d10(3));
			planet.addResource(REGIAM, 20+Die.d20(4));
			break;
		case Phosphorian:
			if (Die.d3()==1) planet.addResource(ACENITE, Die.d10(3));
			planet.addResource(PARDENIC, Die.d10(1));
			planet.addResource(REGIAM, 10+Die.d20(3));
			if (Die.d4()==1) planet.addResource(MAGNESITE, 10+Die.d6(2));
			break;
		case JaniLithic:
			planet.addResource(AURAM, Die.d20(2));
			planet.addResource(REGIAM, Die.d12(2));
			break;
		}
	}
	
	private static void setHotBarren(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(SILICATE, Die.d20(4));
		planet.addResource(FERRIC, Die.d20(2));
		planet.addResource(HELIACATE, Die.d12());
	}
	
	private static void setWorldOcean(ObjectFactory factory, Star star, Planet planet) {
		planet.addResource(AQUAM, 100);
	}
	
	/**
	 * Set the resources on the given planet.
	 */
	public static void setResources(ObjectFactory factory, Star star, Planet planet) {
		if (planet.getType().isBelt()) {
			setBelt(factory, star, planet);
		} else if (planet.getType().isAsteroid()) {
			setAsteroid(factory, star, planet);
		} else if (planet.getType().isDwarfPlanet()) {
			switch (planet.getType()) {
			case Cerean:
			case Hadean:
			case Vesperian:
			case Vestian:
			case Hephaestian:
			case Ferrinian:
			case Selenian:
				setRockyDwarf(factory, star, planet);
				break;
			case Kuiperian:
			case Iapetean:
			case Tritonic:
			case Europan:
			case Stygian:
			case LithicGelidian:
				setIcyDwarf(factory, star, planet);
				break;
			case MesoTitanian: case EuTitanian: case TitaniLacustric:
				setTitanian(factory, star, planet);
				break;
			case MesoUtgardian:	case EuUtgardian: case UtgardiLacustric:
				setUtgardian(factory, star, planet);
				break;
			}
		} else if (planet.getType().isTerrestrial()) {
			switch (planet.getType()) {
			case Gaian:
			case EoGaian:
			case MesoGaian:
			case PostGaian:
			case ArchaeoGaian:
			case GaianTundral:
				setGaian(factory, star, planet);
				break;
			case EoChloritic:
			case MesoChloritic:
			case ArchaeoChloritic:
			case Chloritic:
			case ChloriticTundral:
			case PostChloritic:
				setChlorine(factory, star, planet);
				break;
			case EoThio:
			case MesoThio:
			case ArchaeoThio:
			case Thio:
			case ThioTundral:
			case PostThio:
				setSulphur(factory, star, planet);
				break;
			case EoArean:
			case AreanLacustric:
			case Arean:
			case MesoArean:
			case AreanXenic:
				setArean(factory, star, planet);
				break;
			case Hermian:
				setHotBarren(factory, star, planet);
				break;
			case Cytherean:
			case Phosphorian:
			case JaniLithic:
				setHotAtmosphere(factory, star, planet);
				break;
			case Pelagic:
			case Panthalassic:
				setWorldOcean(factory, star, planet);
				break;
			}
		} else if (planet.getType().isJovian()) {
			setJovian(factory, star, planet);
		}
	}
	
	public static void main(String[] args) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		StarSystem			system = factory.getStarSystem(58619);
		Star				star = null;
		
		for (Planet planet : system.getPlanets()) {
			setResources(factory, star, planet);
			planet.persist();
		}
	}

}

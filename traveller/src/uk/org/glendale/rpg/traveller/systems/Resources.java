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
	private static void setGaian(Planet planet) {
		
		System.out.println("Setting Gaian for ["+planet.getName()+"]");
		
		// Basic mineral resources
		planet.addResource(SILICATE, 50+Die.d20(2));
		planet.addResource(CARBONIC, 50+Die.d20(2));
		planet.addResource(FERRIC, planet.getRadius()/150 + Die.d20(2));
		
		planet.addResource(AQUAM, planet.getHydrographics());		
		planet.addResource(AURAM, 20+Die.d12(2));
		
		planet.addResource(KRYSITE, 30+Die.d12(2));
		planet.addResource(MAGNESITE, 5+Die.d4());
		planet.addResource(VARDONNEK, 10+Die.d8(2));
		
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
			planet.addResource(ALGAE, 35+Die.d20(2));
			planet.addResource(SEAFOOD, Die.d20(1));
			break;
		case ComplexOcean:
			planet.addResource(ALGAE, 40+Die.d12(2));
			planet.addResource(SEAFOOD, 40+Die.d20(2));
			break;
		case SimpleLand:
			planet.addResource(ALGAE, 30+Die.d12(2));
			planet.addResource(SEAFOOD, 40+Die.d20(2));
			planet.addResource(VEGETABLES, 20+Die.d12(2));
			if (Die.d6() < 3) {
				planet.addResource(WOOD, 5+Die.d6(2));
			}
			break;
		case ComplexLand:
			planet.setLifeLevel(LifeType.ComplexLand);
			planet.addResource(ALGAE, 20+Die.d12(2));
			planet.addResource(SEAFOOD, 50+Die.d20(2));
			planet.addResource(VEGETABLES, 40+Die.d20(2));
			planet.addResource(MEAT, 10+Die.d8(2));
			planet.addResource(WOOD, 20+Die.d20(2));
			break;
		case Extensive:
			planet.addResource(ALGAE, 15+Die.d12(2));
			planet.addResource(SEAFOOD, 50+Die.d20(2));
			planet.addResource(VEGETABLES, 50+Die.d20(2));
			planet.addResource(MEAT, 20+Die.d12(2));
			planet.addResource(WOOD, 35+Die.d20(2));
		}		
	}
	
	/**
	 * Set the resources on the given planet.
	 */
	public static void setResources(ObjectFactory factory, Planet planet) {
		System.out.println(planet.getType());
		switch (planet.getType()) {
		case Gaian:
		case EoGaian:
		case MesoGaian:
		case PostGaian:
		case ArchaeoGaian:
		case GaianTundral:
			setGaian(planet);
			break;
		}
	}
	
	public static void main(String[] args) throws Exception {
		ObjectFactory		factory = new ObjectFactory();
		Planet				planet = factory.getPlanet(223065);
		
		setResources(factory, planet);
		planet.persist();
	}

}

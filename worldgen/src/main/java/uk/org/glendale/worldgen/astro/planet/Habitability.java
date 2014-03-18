package uk.org.glendale.worldgen.astro.planet;

import uk.org.glendale.worldgen.astro.star.Temperature;

public enum Habitability {
	/** Can survive on world naked. */
	Ideal(3),
	/** Habitable, but difficult. */
	Habitable(6),
	/** Requires special equipment to survive more than an hour. */
	Difficult(12),
	/** Non-breathable, too hot or too cold to survive more than minutes. */
	Inhospitable(25),
	/** Even a vac-suit isn't enough, e.g. Mercury. */
	Hostile(100),
	/** Venus. */
	VeryHostile(1000);
	
	private int limit = 0;
	
	private Habitability(int limit) {
		this.limit = limit;
	}
	
	
	public static Habitability getHabitability(Planet planet) {
		if (planet.getType().isJovian()) {
			return VeryHostile;
		}

		int	badness = 0;
		badness += planet.getTemperature().getBadness();
		badness += planet.getAtmosphere().getBadness();
		badness += planet.getPressure().getBadness();
		
		for (Habitability h : values()) {
			if (h.limit > badness) {
				return h;
			}
		}
		
		return VeryHostile;
	}
}

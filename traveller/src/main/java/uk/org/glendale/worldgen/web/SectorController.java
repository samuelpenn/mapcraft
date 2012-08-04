package uk.org.glendale.worldgen.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemGenerator;

@Controller
public class SectorController {
	@Autowired
	private SectorFactory	sectorFactory;
	
	@Autowired
	private StarSystemFactory starSystemFactory;
	
	@Autowired
	private StarSystemGenerator	starSystemGenerator;
	
	@RequestMapping("/sector/{name}")
	public final String getSector(@PathVariable String name) {
		return name;
	}

}

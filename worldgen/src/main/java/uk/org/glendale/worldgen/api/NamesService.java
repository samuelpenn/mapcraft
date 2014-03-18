package uk.org.glendale.worldgen.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class NamesService {
	private final String BASE_URI = "/api/names/";
	
	@RequestMapping()
	public String[] getNames() {
		return null;
	}
}

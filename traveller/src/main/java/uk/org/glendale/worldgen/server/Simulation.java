package uk.org.glendale.worldgen.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import uk.org.glendale.worldgen.civ.trade.CivilisationAPI;

@Service
public class Simulation {
	@Autowired
	private CivilisationAPI		civilisation;
	
	@Scheduled(fixedDelay=60000)
	public void run() {
		civilisation.simulate();
	}
}

package uk.org.glendale.worldgen.dashboard;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.server.AppManager;

@ManagedBean(name="dashboard")
@SessionScoped
public class Dashboard {
	private		AppManager	app = AppManager.getInstance();
	
	private		String		title = "KnownSpace";
	
	private		String		selectedSector = null;
	private		String		selectedSystem = null;
	
	public Dashboard() {
		
	}

	public String getTitle() {
		return title;
	}
	
	public String getSelectedSector() {
		return selectedSector;
	}
	
	public void setSelectedSector(String sector) {
		this.selectedSector = sector;
	}
	
	public String getSelectedSystem() {
		return selectedSystem;
	}
	
	public void setSelectedSystem(String system) {
		this.selectedSystem = system;
	}
	
	public List<StarSystem> getSystemsList() {
		SectorFactory		sectorFactory = new SectorFactory(app.getEntityManager());
		StarSystemFactory	systemFactory = new StarSystemFactory(app.getEntityManager());
		
		Sector	currentSector = sectorFactory.getSector(selectedSector);
		List<StarSystem> list = systemFactory.getStarSystemsInSector(currentSector);
		
		return list;
	}
}

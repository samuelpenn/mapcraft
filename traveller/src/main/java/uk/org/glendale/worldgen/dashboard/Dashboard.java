package uk.org.glendale.worldgen.dashboard;

import java.util.List;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.sector.SubSector;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.server.AppManager;

/**
 * Manages the dashboard interface, keeping track of session information
 * for the user.
 * 
 * @author Samuel Penn
 */
public class Dashboard {
	private		AppManager	app = AppManager.getInstance();
	
	private		String		title = "KnownSpace";
	
	private		String		selectedSector = null;
	private		int			selectedSystemId = 0;
	
	private SectorFactory	sectorFactory;
	
	public Dashboard() {
		
	}

	public String getTitle() {
		return title;
	}
	/*
	public void selectedSector(ValueChangeEvent event) {
		setSelectedSector((String)event.getNewValue());
	}
	*/
	
	public void setSectorFactory(SectorFactory factory) {
		this.sectorFactory = factory;
	}
	
	public String getSelectedSector() {
		if (selectedSector == null) {
			List<Sector>		list = sectorFactory.getAllSectors();
			selectedSector = list.get(0).getName();
		}
		return selectedSector;
	}
	
	public void setSelectedSector(String sector) {
		this.selectedSector = sector;
		this.selectedSystemId = 0;
		this.selectedSystemId = getSelectedSystem();
	}
	
	public int getSelectedSystem() {
		if (selectedSystemId == 0) {
			if (selectedSector == null) {
				selectedSector = getSelectedSector();
			}
			selectedSystemId = getSystemsList().get(0).getId();
		}
		return selectedSystemId;
	}
	
	public void setSelectedSystem(int systemId) {
		StarSystemFactory	systemFactory = new StarSystemFactory(app.getEntityManager());
		StarSystem			system = systemFactory.getStarSystem(systemId);
		
		if (system.getSector().getName().equals(selectedSector)) {
			this.selectedSystemId = systemId;
		} else {
			this.selectedSystemId = 0;
		}
	}
	
	public List<StarSystem> getSystemsList() {
		StarSystemFactory	systemFactory = new StarSystemFactory(app.getEntityManager());
		
		Sector	currentSector = sectorFactory.getSector(selectedSector);
		List<StarSystem> list = systemFactory.getStarSystemsInSector(currentSector);
		
		systemFactory.close();
		
		return list;
	}
	
	public StarSystem getSystemData() {
		StarSystemFactory	systemFactory = new StarSystemFactory(app.getEntityManager());
		
		if (selectedSystemId == 0) {
			selectedSystemId = getSelectedSystem();
		}
		
		return systemFactory.getStarSystem(selectedSystemId);
	}
	
	public String getSubSectorURL() {
		if (selectedSystemId == 0) {
			selectedSystemId = getSelectedSystem();
		}
		if (selectedSystemId != 0) {
			/*
			String x = selectedSystem.replaceAll(".*\\(([0-9]{2})([0-9]{2})\\)", "$1");
			String y = selectedSystem.replaceAll(".*\\(([0-9]{2})([0-9]{2})\\)", "$2");
			*/
			
			StarSystemFactory	systemFactory = new StarSystemFactory(app.getEntityManager());
			StarSystem	system = systemFactory.getStarSystem(selectedSystemId);
			
			//SubSector	subSector = SubSector.getSubSector(Integer.parseInt(x), Integer.parseInt(y));
			SubSector	subSector = SubSector.getSubSector(system.getX(), system.getY());
			
			Sector	currentSector = sectorFactory.getSector(selectedSector);
			return "/traveller/api/subsector/"+currentSector.getId()+"/"+subSector+"?scale=32";
		}
		return "";
	}
}

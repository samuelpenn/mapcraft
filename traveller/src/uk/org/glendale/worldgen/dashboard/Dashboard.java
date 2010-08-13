package uk.org.glendale.worldgen.dashboard;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;

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
@ManagedBean(name="dashboard")
@SessionScoped
public class Dashboard {
	private		AppManager	app = AppManager.getInstance();
	
	private		String		title = "KnownSpace";
	
	private		String		selectedSector = null;
	private		int			selectedSystemId = 0;
	
	public Dashboard() {
		
	}

	public String getTitle() {
		return title;
	}
	
	public void selectedSector(ValueChangeEvent event) {
		selectedSector = (String)event.getNewValue();
	}
	
	public String getSelectedSector() {
		return selectedSector;
	}
	
	public void setSelectedSector(String sector) {
		this.selectedSector = sector;
	}
	
	public int getSelectedSystem() {
		return selectedSystemId;
	}
	
	public void setSelectedSystem(int systemId) {
		this.selectedSystemId = systemId;
	}
	
	public List<StarSystem> getSystemsList() {
		SectorFactory		sectorFactory = new SectorFactory(app.getEntityManager());
		StarSystemFactory	systemFactory = new StarSystemFactory(app.getEntityManager());
		
		Sector	currentSector = sectorFactory.getSector(selectedSector);
		List<StarSystem> list = systemFactory.getStarSystemsInSector(currentSector);
		
		sectorFactory.close();
		systemFactory.close();
		
		return list;
	}
	
	public StarSystem getSystemData() {
		StarSystemFactory	systemFactory = new StarSystemFactory(app.getEntityManager());
		return systemFactory.getStarSystem(selectedSystemId);
	}
	
	public String getSubSectorURL() {
		if (selectedSystemId != 0) {
			/*
			String x = selectedSystem.replaceAll(".*\\(([0-9]{2})([0-9]{2})\\)", "$1");
			String y = selectedSystem.replaceAll(".*\\(([0-9]{2})([0-9]{2})\\)", "$2");
			*/
			
			StarSystemFactory	systemFactory = new StarSystemFactory(app.getEntityManager());
			StarSystem	system = systemFactory.getStarSystem(selectedSystemId);
			
			//SubSector	subSector = SubSector.getSubSector(Integer.parseInt(x), Integer.parseInt(y));
			SubSector	subSector = SubSector.getSubSector(system.getX(), system.getY());
			
			SectorFactory		sectorFactory = new SectorFactory(app.getEntityManager());
			Sector	currentSector = sectorFactory.getSector(selectedSector);
			sectorFactory.close();
			return "/traveller/api/subsector/"+currentSector.getId()+"/"+subSector+"?scale=32";
		}
		return "";
	}
}

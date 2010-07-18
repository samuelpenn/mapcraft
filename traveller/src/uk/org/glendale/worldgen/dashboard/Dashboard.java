package uk.org.glendale.worldgen.dashboard;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import uk.org.glendale.worldgen.server.AppManager;

@ManagedBean(name="dashboard")
@SessionScoped
public class Dashboard {
	private		AppManager	app = AppManager.getInstance();
	
	private		String		title = "KnownSpace";
	
	private		String		selectedSector = null;
	
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
}

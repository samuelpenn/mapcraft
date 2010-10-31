package uk.org.glendale.mapcraft.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.*;

import uk.org.glendale.mapcraft.server.database.MapInfo;
import uk.org.glendale.mapcraft.server.database.MapManager;

/**
 * A bean which provides access to information about the server.
 * 
 * @author Samuel Penn
 *
 */
@ManagedBean(name="serverInfo")
public class ServerInfo {
	private MapManager	manager = null;
	
	public ServerInfo() throws SQLException {
		manager = new MapManager(AppManager.getInstance().getDatabaseConnection());
	}
	
	/**
	 * Gets the name of this map server. This is a short description.
	 * 
	 * @return	User readable server name.
	 */
	public String getServerName() {
		return "Daiakuji Test Maps";
	}
	
	public List<String> getMapNames() {
		ArrayList<String>	list = new ArrayList<String>();
		
		for (MapInfo info : manager.getMaps()) {
			list.add(info.getTitle());
		}
		/*
		list.add("Test One");
		list.add("Habisfern");
		list.add("Something Awful");
		*/
		return list;
	}
	
	public MapInfo[] getMaps() {
		return manager.getMaps();
	}
}

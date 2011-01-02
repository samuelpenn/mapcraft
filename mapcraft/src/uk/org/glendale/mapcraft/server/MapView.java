package uk.org.glendale.mapcraft.server;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * Bean to store information about a UI map view. Records the
 * X and Y coordinates of the map.
 * 
 * @author Samuel Penn
 */
@ManagedBean(name="mapView") @RequestScoped
public class MapView {
	private String 		name;
	private int			x;
	private int			y;
	
	public MapView() {
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
}

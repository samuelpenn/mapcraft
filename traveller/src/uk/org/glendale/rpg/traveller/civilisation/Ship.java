package uk.org.glendale.rpg.traveller.civilisation;

import java.sql.*;

import uk.org.glendale.rpg.traveller.database.ShipFactory;
import uk.org.glendale.rpg.traveller.sectors.Allegiance;

public class Ship {
	private int 		id;
	private String		name;
	private String		type;
	private String		flag;
	private int			displacement;
	private int			cargo;
	private int			jump;
	private int			accl;
	private ShipStatus	status;
	private int			systemId;
	private int			planetId;
	private long		nextEvent = 0;
	
	private ShipFactory	factory = null;
	
	
	public enum ShipStatus {
		Virtual,
		Wreck,
		Docked,
		Planet,
		Orbit,
		Flight,
		Jump;
	}
	
	public Ship(String name, String type, int displacement, int jump, int cargo) {
		this.name = name;
		this.type = type;
		this.displacement = displacement;
		this.cargo = cargo;
		this.jump = jump;
		
		status = ShipStatus.Virtual;
		systemId = 0;
		planetId = 0;
		flag = "Imperium";
	}
	
	private void read(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		type = rs.getString("type");
		systemId = rs.getInt("system_id");
		planetId = rs.getInt("planet_id");
		status = ShipStatus.valueOf(rs.getString("status"));
		displacement = rs.getInt("displacement");
		cargo = rs.getInt("cargo");
		jump = rs.getInt("jump");
		flag = rs.getString("flag");
		nextEvent = rs.getLong("next_event");
	}
	
	public void persist() {
		factory.persist(this);
	}
	
	public Ship(ShipFactory factory, ResultSet rs) throws SQLException {
		this.factory = factory;
		read(rs);
	}
}

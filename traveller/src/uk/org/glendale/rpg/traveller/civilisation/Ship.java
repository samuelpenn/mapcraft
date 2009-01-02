/*
 * Copyright (C) 2008 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.3 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.civilisation;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

import uk.org.glendale.rpg.traveller.civilisation.ship.Role;
import uk.org.glendale.rpg.traveller.civilisation.trade.TradeGood;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.database.Simulation;
import uk.org.glendale.rpg.traveller.sectors.Sector;
import uk.org.glendale.rpg.traveller.systems.StarSystem;
import uk.org.glendale.rpg.utils.Die;

/**
 * Model the characteristics and behaviour of a ship in the universe.
 * 
 * @author Samuel Penn
 */
public class Ship {
	private int 		id;
	private String		name;
	private String		type;
	private String		flag;
	private int			displacement;
	private int			cargoCapacity;
	private int			jump;
	private int			accl;
	private ShipStatus	status;
	private int			systemId;
	private int			planetId;
	private long		nextEvent = 0;
	private long		inService = 0;
	private String		role = null;
	private int			cash = 0;
	
	private Hashtable<Integer,TradeGood>	cargo = null;
	
	public enum ShipStatus {
		Virtual,
		Wreck,
		Docked,
		Planet,
		Orbit,
		Flight, FlightIn, FlightOut,
		Jump;
	}
	
	public Ship(String name, String type, int displacement, int jump, int accl, int cargo) {
		this.name = name;
		this.type = type;
		this.displacement = displacement;
		this.cargoCapacity = cargo;
		this.jump = jump;
		this.accl = accl;
		
		role = "Roving";
		cash = 100;
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
		cargoCapacity = rs.getInt("cargo");
		jump = rs.getInt("jump");
		flag = rs.getString("flag");
		nextEvent = rs.getLong("nextevent");
		inService = rs.getLong("inservice");
		accl = rs.getInt("accl");
		role = rs.getString("role");
		cash = rs.getInt("cash");
	}

	public void persist(ObjectFactory factory) {
		Hashtable<String,Object>	data = new Hashtable<String,Object>();
		
		data.put("id", id);
		data.put("name", name);
		data.put("type", type);
		data.put("system_id", systemId);
		data.put("planet_id", planetId);
		data.put("status", status.toString());
		data.put("displacement", displacement);
		data.put("cargo", cargoCapacity);
		data.put("jump", jump);
		data.put("flag", flag);
		data.put("nextEvent", nextEvent);
		data.put("inservice", inService);
		data.put("accl", accl);
		data.put("role", role);
		data.put("cash", cash);
		
		int auto = factory.persist("ship", data);
		if (id == 0) id = auto;
	}
	
	public Ship(ResultSet rs) throws SQLException {
		read(rs);
	}
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
	
	public int getSystemId() {
		return systemId;
	}
	
	public int getPlanetId() {
		return planetId;
	}
	
	public int getJumpRating() {
		return jump;
	}
	
	public long getNextEvent() {
		return nextEvent;
	}
	
	public long getInServiceDate() {
		return inService;
	}
	
	/**
	 * Acceleration of the ship, in m/s.
	 */
	public int getAcceleration() {
		return accl;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setNextEvent(long nextEvent) {
		this.nextEvent = nextEvent;
	}
	
	public void setInServiceDate(long inService) {
		this.inService = inService;
	}
	
	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}
	
	public void setPlanetId(int planetId) {
		this.planetId = planetId;
	}
	
	public ShipStatus getStatus() {
		return status;
	}
	
	public void setStatus(ShipStatus status) {
		this.status = status;
	}
	
	public int getCash() {
		return cash;
	}
	
	public void setCash(int cash) {
		this.cash = cash;
	}
	
	public int getCargoCapacity() {
		return cargoCapacity;
	}
	
	public int getCargoUsed() {
		int		used = 0;
		
		if (cargo != null) {
			for (int i : cargo.keySet()) {
				used += cargo.get(i).getAmount();
			}
		}
		
		return used;
	}
	
	public void setCargo(Hashtable<Integer,TradeGood> cargo) {
		this.cargo = cargo;
	}

	public Hashtable<Integer,TradeGood> getCargo() {
		return cargo;
	}
	
	public void addCargo(TradeGood good) {
		if (cargo == null) {
			cargo = new Hashtable<Integer,TradeGood>();
		}
		cargo.put((int)(Integer.MIN_VALUE * Math.random()), good);
	}
	
	public void modifyCargo(int id, int amount) {
		if (cargo != null) {
			if (amount == 0) {
				cargo.remove(id);
			} else {
				TradeGood good = cargo.get(id);
				good.setAmount(good.getAmount() + amount);
				if (good.getAmount() < 0) cargo.remove(id);
			}
		}
	}
	
	public ArrayList<StarSystem> getSystemsInJumpRange(ObjectFactory factory) {
		ArrayList<StarSystem>	list = new ArrayList<StarSystem>();
		
		if (systemId < 1) {
			// No idea where the ship is.
			return null;
		}
		
		try {
			StarSystem		homeSystem = factory.getStarSystem(systemId);
			Sector			homeSector = new Sector(factory, homeSystem.getSectorId());
			
			for (int sy = homeSystem.getY() - jump; sy <= homeSystem.getY()+jump; sy++) {
				for (int sx = homeSystem.getX() - jump; sx <= homeSystem.getX()+jump; sx++) {
					StarSystem	s2 = homeSector.getSystem(sx, sy);
					if (s2 == null || s2.getId() == homeSystem.getId()) continue;
	
					list.add(s2);
				}
			}
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * Model what a ship might do when docked at a space port. Space ports
	 * may be in orbit, or on a planet's surface. Both states are considered
	 * to be the same for simplicity.
	 * 
	 * CurrentState: Docked
	 * SystemId:     Set
	 * PlanetId:     Set
	 */
	private void simulateWhenDocked(Simulation simulation, ObjectFactory factory, long eventTime) {
		if (Die.d6() == 1) {
			setStatus(Ship.ShipStatus.Flight);
			setNextEvent(eventTime + (12 * 3600 * 10 / getAcceleration()));
			simulation.log(getId(), getSystemId(), getPlanetId(), eventTime, Simulation.LogType.UnDock, "Undocks");
		} else {
			setNextEvent(eventTime + Die.d12(2)*3600);
			System.out.println("Remains docked");
		}
	}

	public void simulate(Simulation simulation, ObjectFactory factory, long actualTime) {
		System.out.println("simulate: ["+getName()+" / "+getType()+" / "+getStatus()+"]");
		if (getInServiceDate() == 0) {
			setInServiceDate(actualTime);
		}
		
		if (getNextEvent() == 0) {
			setNextEvent(actualTime);
		}
		long	eventTime = getNextEvent();
		if (eventTime < actualTime) {
			// So we don't end up with a huge backlog of events to handle,
			// force older events rapidly into the future. Note that if
			// we process an event a long time after it happens, then follow
			// on events may finish in the past.
			eventTime = (eventTime + actualTime) / 2;
		}
		
		try {
			Class	roleClass = Class.forName("uk.org.glendale.rpg.traveller.civilisation.ship."+role);
			Role	model = (Role)roleClass.newInstance();
			model.init(this, simulation, factory, eventTime);
			
			switch (getStatus()) {
			case Docked:
				model.docked();
				break;
			case FlightOut:
				model.flightOut();
				break;
			case FlightIn:
				model.flightIn();
				break;
			case Jump:
				model.inJump();
				break;
			case Orbit:
				model.inOrbit();
				break;
			case Planet:
				model.planetSide();
				break;
			}
			persist(factory);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args) {
		ObjectFactory		factory = new ObjectFactory();
		
		
		String[] names = { "Child of Adkynson", "Child of Free Enterprise", "Child of Boccob",
				           "Child of Lust", "Child of Greed", "Child of Liberty", "Child of Mathematics",
				           "Child of Vacuum", "Child of the Stars", "Child of Serendipity" };
		
		for (int i=0; i < names.length; i++) {
			Ship	ship = new Ship(names[i], "Adder", 100, 1, 30, 30);
			ship.setSystemId(14132);
			ship.setPlanetId(174453);
			ship.setStatus(ShipStatus.Docked);
			
			ship.persist(factory);
		}
	}
}

/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.server;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import uk.org.glendale.rpg.traveller.database.ObjectFactory;

/**
 * WorldGen application manager.
 * 
 * @author Samuel Penn
 */
public class AppManager {
	private static Properties properties = new Properties();

	private static String universe = null;
	private static String rootPath = null;

	static {
		try {
			setConfig("worldgen");
		} catch (Throwable e) {

		}
	}

	public static void setConfig(final String bundleName) {
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName);

		Enumeration<String> e = bundle.getKeys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			String value = bundle.getString(key);
			properties.setProperty(key, value);
		}

		universe = properties.getProperty("universe");
	}



	public static void main(String[] args) throws Exception {
		AppManager app = new AppManager();

	}

	private static AppManager appManager = null;

	public static String getUniverse() {
		return universe;
	}

	public static String getRootPath() {
		return rootPath;
	}

	public static boolean getDrawGlobe() {
		return properties.getProperty(universe + ".planet.drawGlobe", "false")
				.equals("true");
	}

	public static boolean getStretchMap() {
		return properties.getProperty(universe + ".planet.stretchMap", "false")
				.equals("true");
	}

	public static boolean getDrawMap() {
		return properties.getProperty(universe + ".planet.drawMap", "true")
				.equals("true");
	}

	public static AppManager getInstance() {
		if (appManager == null) {
			appManager = new AppManager();
		}
		return appManager;
	}

}

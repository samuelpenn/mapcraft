/*
 * Copyright (C) 2004 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version. See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package net.sourceforge.mapcraft.utils;

import javax.swing.filechooser.FileFilter;

import net.sourceforge.mapcraft.MapCraft;

import java.io.*;

/**
 * File filter dialog to load map files.
 * Keeps track of where files were loaded from previously, and presents
 * this location as the first choice, otherwise defaults to home directory.
 */
public class MapFileFilter extends FileFilter {
	private String 			MAP_EXTENSION = "map";
	private static String	lastLocation = null;
	
	/**
	 * Get the last location that a file was loaded from. This should be
	 * passed to the FileChooser object with setCurrentDirectory() when
	 * the FileChooser is instantiated.
	 * 
	 * @return	The last location, as a File object.
	 */
	public static File
	getLastLocation() {
		if (lastLocation == null) {
			String	path = MapCraft.getProperty("file.lastLocation", null);
			if (path != null) {
				lastLocation = path;
				return new File(path);
			}
			return (File) null;
		}
		
		return new File(lastLocation);
	}
	
	/**
	 * Record the location that a map file was last loaded from. This is
	 * either the file itself, or its parent directory. In either case, the
	 * parent directory is stored for future use.
	 * 
	 * @param file	File loaded, or the directory it is contained in.
	 */
	public static void
	setLastLocation(File file) {
		if (file.isDirectory()) {
			lastLocation = file.getAbsolutePath();
		} else {
			lastLocation = file.getParent();
		}
		MapCraft.setProperty("file.lastLocation", lastLocation);
	}
	
	public
	MapFileFilter() {
	}

	public String
	getDescription() {
		return "Mapcraft map files";
	}

	public String
	getExtension(File f) {
		if(f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if(i>0 && i<filename.length()-1) {
				return filename.substring(i+1).toLowerCase();
			};
		}
		return null;
	}

	public boolean
	accept(File f) {
		if(f != null) {
			if(f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if(extension != null && extension.equals(MAP_EXTENSION)) {
				return true;
			}
		}
		return false;
	}
}


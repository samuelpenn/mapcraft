/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller;

import java.io.*;

import uk.org.glendale.logging.*;


public class Log extends AbstractLogger {
	/**
	 * Initialise the logger, with a debugging level.
	 * The levels are 0 = errors, 1 = info, 2 = debug.
	 * If called multiple times, subsequent calls are ignored.
	 * 
	 * @param level	Debug level to use, from 0 (errors) - 2 (full).
	 */
	public static void init(int level) {
		if (logger != null) {
			// Only init once.
			return;
		}
		logger = new GenericLogger("traveller");
		if (level < 1) {
			logger.setWarn();
		} else if (level == 1) {
			logger.setInfo();
		} else {
			logger.setDebug();
		}
		try {
			logger.setOutputFile(new File("traveller.log"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Traveller logger is starting at level "+level);
	}
	
	public static void init(String path, int level) {
		if (logger != null) {
			// Only init once.
			return;
		}
		logger = new GenericLogger("traveller");
		if (level < 1) {
			logger.setWarn();
		} else if (level == 1) {
			logger.setInfo();
		} else {
			logger.setDebug();
		}
		try {
			logger.setOutputFile(new File(path+"../../logs/traveller.log"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Traveller logger is starting at level "+level);
	}
}

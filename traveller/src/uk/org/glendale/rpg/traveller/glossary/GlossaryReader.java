/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.glossary;

import java.io.*;

/**
 * Reads glossary entries from the file system, and inserts them into the
 * database.
 */
import uk.org.glendale.rpg.traveller.database.*;

public class GlossaryReader {
	private File				parent;
	private GlossaryFactory		factory = null;
	
	public GlossaryReader(File parent) {
		this.parent = parent;
		factory = new GlossaryFactory();
	}
	
	public void process() {
		File[]		files = parent.listFiles();
		
		for (int i=0; i < files.length; i++) {
			File		file = files[i];
			if (file.getName().equals("CVS") || file.getName().startsWith(".")) {
				// Ignore CVS files.
				continue;
			}
			System.out.println(file.getName());
			
			try {
				GlossaryEntry	entry = readEntryFromFile(file);
				factory.setEntry(entry);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (GlossaryException e) {
				e.printStackTrace();
			}
		}
	}
	
	private GlossaryEntry	readEntryFromFile(File file) throws FileNotFoundException {
		GlossaryEntry		entry = null;
		String				uri = file.getName();
		String				title = null;
		String				text = null;
		boolean				done = false;
		
		FileReader			reader = new FileReader(file);
		LineNumberReader	lineReader = new LineNumberReader(reader);
		StringBuffer		buffer = new StringBuffer();
		
		try {
			title = lineReader.readLine().replace("#", "").trim();
			while (!done) {
				String		line = lineReader.readLine();
				if (line == null) {
					done = true;
				} else if (line.startsWith("#")) {
					// Ignore (for now).
				} else {
					buffer.append(line+"\n");
				}
			}
			text = buffer.toString();
			entry = new GlossaryEntry(0, uri, title, text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return entry;
	}
	
	public static void main(String[] args) {
		GlossaryReader		reader = new GlossaryReader(new File("data/glossary"));
		reader.process();
	}
}

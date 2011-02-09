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

/**
 * Describes a single entry from the glossary.
 * 
 * @author Samuel Penn
 *
 */
public class GlossaryEntry {
	private int 	id;
	private String	uri;
	private String	title;
	private String	text;
	
	public GlossaryEntry(int id, String uri, String title, String text) {
		this.id = id;
		this.uri = uri;
		this.title = title;
		this.text = text;
	}
	
	public int getId() {
		return id;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getText() {
		return text;
	}

}

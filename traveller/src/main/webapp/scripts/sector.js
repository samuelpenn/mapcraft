/*
 * sector.js
 *
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 * 
 * Define the Sector class.
 */

function Sector (sector) {
	this.id = sector.id;
	this.name = sector.name;
	this.x = sector.x;
	this.y = sector.y;
	this.subSectors = sector.subSectors;
	this.systems = new Array();
	
	for (var i=0; i < sector.systems.length; i++) {
		this.systems.push(new StarSystem(sector.systems[i]));
	}
}

Sector.prototype.getName = function() {
	return this.name;
};

Sector.prototype.getX = function() {
	return this.x;
};

Sector.prototype.getY = function() {
	return this.y;
};

Sector.prototype.getSystems = function() {
	return this.systems;
}
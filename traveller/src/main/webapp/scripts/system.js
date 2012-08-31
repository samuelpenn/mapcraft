/*
 * system.js
 *
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 * 
 * Define the StarSystem class.
 */


/**
 * Define a System class object.
 */
function StarSystem (system) {
	this.system = system;
}


StarSystem.prototype.getFullName = function() {
	return this.system.sectorName + " / " + this.system.name;
};

StarSystem.prototype.getSectorName = function() {
	return this.system.sectorName;
};

StarSystem.prototype.getName = function() {
	return this.system.name;
};

StarSystem.prototype.getX = function() {
	return this.system.x;
};

StarSystem.prototype.getY = function() {
	return this.system.y;
};

StarSystem.prototype.getStars = function() {
	var a = new Array();
	
	for (var i=0; i < this.system.stars.length; i++) {
		a.push(new Star(this.system.stars[i]));
	}
	
	return a;
};

StarSystem.prototype.getStar = function(id) {
	for (var i=0; i < this.system.stars.length; i++) {
		if (this.system.stars[i].id == id) {
			return new Star(this.system.stars[i]);
		}
	}
	return null;
};

StarSystem.prototype.getPlanets = function(starId) {
	var a = new Array();
	
	for (var i=0; i < this.system.planets.length; i++) {
		if (this.system.planets[i].isMoon == false) {
			if (starId == null || this.system.planets[i].parentId == starId) {
				var p = new Planet(this.system.planets[i]);
				if (p.getId() == this.system.mainWorld.id) {
					p.mainWorld = true;
				}
				a.push(p);
			}
		}
	}

	return a;	
};

StarSystem.prototype.getPlanet = function(id) {
	for (var i=0; i < this.system.planets.length; i++) {
		if (this.system.planets[i].id == id) {
			var p = new Planet(this.system.planets[i]);
			if (p.getId() == this.system.mainWorld.id) {
				p.mainWorld = true;
			}
			return p;
		}
	}
	return null;
};

StarSystem.prototype.getMoons = function(id) {
	var a = new Array();
	
	for (var i=0; i < this.system.planets.length; i++) {
		if (this.system.planets[i].isMoon == true && 
				this.system.planets[i].parentId == id) {
			var p = new Planet(this.system.planets[i]);
			if (p.getId() == this.system.mainWorld.id) {
				p.mainWorld = true;
			}
			a.push(p);
		}
	}
	
	return a;	
};

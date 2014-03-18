/*
 * star.js
 *
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 * 
 * Define the Star class.
 */

function Star (star) {
	this.star = star;
}

Star.prototype.getName = function() {
	return this.star.name;
};

Star.prototype.getSpectralType = function() {
	return this.star.spectralType;
};

Star.prototype.getClassification = function() {
	return this.star.classification;
};

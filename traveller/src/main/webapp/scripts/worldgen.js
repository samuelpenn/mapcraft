/*
 * worldgen.js
 *
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 * 
 * Collects together all the functions for accessing WorldGen Javascript
 * objects and REST interfaces.
 */






WG = {
	system: null,
	sector: null,
		
	getStar: function (id) {
		return null;
	},
		
	getParent: function(id) {
		return null;
	},

	/**
	 * Gets a pretty printed value for a camel case enum by adding
	 * spaces between words. For example, the enum value "ExampleEnum" 
	 * is returned as "Example Enum".
	 * 
	 * @param val	CamelCase enum to be pretty printed.
	 * @returns		Pretty printed version of enum.
	 */
	formatEnum: function(val) {
 	   if (val == null) {
 		   return "";
 	   } else {
	    	   val = "" + val;
            return val.replace(/([A-Z])/g, " $1").trim();
 	   }	    	   
    },

    /**
     * Given a number, formats it as a string with commas between
     * everything set of 3 digits. Does not cope with decimals or
     * negative numbers.
     * 
     * @param number	Number to be formatted.
     * @returns			Formatted number.
     */
	addCommas: function (number) {
        number += '';
        var x = number.split('.');
        var x1 = x[0];
        var x2 = x.length > 1 ? '.' + x[1] : '';
        var rgx = /(\d+)(\d{3})/;
        while (rgx.test(x1)) {
            x1 = x1.replace(rgx, '$1' + ',' + '$2');
        }
        return x1 + x2;
    },
    
    /**
     * Gets data on the planet identified by the unique id. The planet must
     * exist in the currently selected star system unless a star system is
     * provided.
     * 
     * @param id		Unique id of the planet.
     * @param system	Optional system to look for planet in. 
     * @returns			Data object for the planet, or null if not found.
     */	
	getPlanet: function (id, system) {
		if (system == null) {
			system = this.system;
		}
		return system.getPlanet(id);
		
		if (system == null || system.planets == null) {
			return null;
		}
		for (var i=0; i < system.planets.length; i++) {
			if (system.planets[i].id == id) {
				return system.planets[i];
			}
		}
		return null;
	},
	    

		
	version: function() {
		return "0.1";
	}
};

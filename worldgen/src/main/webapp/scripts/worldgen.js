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
	},
	
	/**
	 * Load the moons for this planet, then display them.
	 */
	loadMoons: function(id, func) {
		var	 p = this.system.getPlanet(id);
		if (p.planet.gotMoons != true) {
			$.getJSON("/traveller/api/planet/"+id+"/moons", function(data) {
				for (var i=0; i < data.length; i++) {
					WG.system.system.planets.push(data[i]);
				}
				if (func != null) {
					var moons = WG.system.getMoons(id);
					for (var i=0; i < moons.length; i++) {
						func(moons[i].getId());
					}
				}
			});
			// Make sure we only fetch the data once.
			p.planet.gotMoons = true;
		} else if (func != null) {
			var moons = WG.system.getMoons(id);
			for (var i=0; i < moons.length; i++) {
				func(moons[i].getId());
			}
		}
	},
	
	loadInventory: function(id, func) {
		var	p = this.system.getPlanet(id);
		if (p.planet.gotInventory != true) {
			p.planet.inventory = new Array();
			$.getJSON("/traveller/api/planet/" + id + "/inventory", function (data) {
				for (var i=0; i < data.length; i++) {
					p.planet.inventory.push(data[i]);
				}
				if (func != null) {
					func(p);
				}
			});
			p.planet.gotInventory = true;
		} else if (func != null) {
			func(p);
		}
	},

		
	version: function() {
		return "0.1";
	}
};

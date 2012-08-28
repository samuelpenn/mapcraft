/**
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
	    
       
    /**
     * Gets the type of the planet, pretty formatted.
     * 
     * @param planet	Planet object to get the type for.
     * @returns			Pretty formatted planet type.
     */
    getType: function(planet) {
	    return formatEnum(planet.type);
    },
   
    getDistance: function(planet) {
	   if (planet.isMoon) {
		   return addCommas(planet.distance) + " km";
       }
       return addCommas(planet.distance) + " MKm";
    },
   
    getRadius: function(planet) {
	   if (planet.tradeCodes.indexOf("As") != -1) {
		   return addCommas(planet.radius * 4 + " Mkm");
	   }
	   
	   return addCommas(planet.radius) + " km";
    },
       
    getPopulation: function(planet) {
	    return addCommas(planet.population);
    },
       
    getAtmosphere: function(planet) {
    	if (planet.pressure == "None") {
    		return "Vacuum";
    	} else if (planet.pressure == "Standard") {
    	    return formatEnum(planet.atmosphere);
    	} else {
    	    return formatEnum(planet.pressure + " " + planet.atmosphere);
    	}
    },

    getTemperature: function(planet) {
    	return formatEnum(planet.temperature);
    },
       
    getHydrographics: function(planet) {
    	return planet.hydrographics + "%";
    },
       
    getGovernment: function(planet) {
    	return formatEnum(planet.government);
    },
       
    getAxialTilt: function(planet) {
    	if (planet.tradeCodes.indexOf("As") != -1) {
            return "--";
        }
    	return planet.axialTilt + "&#176;";
    },

    getDayLength: function(planet) {
        if (planet.tradeCodes.indexOf("As") != -1) {
            return "--";
        }
    	return planet.dayLengthText;
    },
       
    getLawLevel: function(planet) {
	    switch (planet.lawLevel) {
	    case 0: return "0 (Lawless)";
        case 1: return "1 (Libertarian)";
        case 2: return "2 (Liberal)";
        case 3: return "3 (Typical)";
        case 4: return "4 (Strict)";
        case 5: return "5 (Restrictive)";
        case 6: return "6 (Authoritarian)";
	    }
    	return "" + planet.lawLevel;
    },
       
    getTechLevel: function(planet) {
	    switch (planet.techLevel) {
	    case  0: return "0 (Stone)";
	    case  1: return "1 (Bronze)";
	    case  2: return "2 (Iron)";
	    case  3: return "3 (Medieval)";
	    case  4: return "4 (Renaissance)";
	    case  5: return "5 (Steam)";
	    case  6: return "6 (Mechanical)";
	    case  7: return "7 (Atomic)";
	    case  8: return "8 (Digital)";
	    case  9: return "9 (Interplanetary)";
        case 10: case 11: 
     	    return planet.techLevel + " (Interstellar)";
        case 12: case 13: case 14:
            return planet.techLevel + " (Low Imperium)";
        case 15: case 16: case 17:
            return planet.techLevel + " (High Imperium)";
        case 18: case 19: case 20:
            return planet.techLevel + " (Advanced)";
        default:
            return planet.techLevel + " (Magic)";
	    }
	    return planet.techLevel;
    },
       
    getLifeLevel: function(planet) {
    	return formatEnum(planet.lifeLevel);
    },
       
    getStarPort: function(planet) {
    	return "" + planet.starport;
    },
       
    getTradeCodes: function(planet) {
    	return "" + planet.tradeCodes;
    },
       
    getTradeIcons: function(planet) {
    	var codes = getTradeCodes(planet).split(" ");
    	   
    	var base = "/traveller/images/symbols/64x64/";
    	   
    	var list = { "ag": "Agricultural",
     		         "na": "Non-Agricultural",
    			     "in": "Industrial",
    			     "ni": "Non-Industrial",
    			     "hi": "High Population",
    			     "lo": "Low Population",
    			     "ri": "Rich",
    			     "po": "Poor",
    			     "ba": "Barren",
    			     "va": "Vacuum",
    			     "de": "Desert",
    			     "as": "Asteroid Belt",
    			     "ic": "Ice-Capped",
    			     "wa": "Water World",
    			     "fl": "Fluid Oceans",
    			     "cp": "Sub-Sector Capital",
    			     "cx": "Sector Capital"
    	   };
    	   
   	   var html = "";
   	   for (var i=0; i < codes.length; i++) {
   		   var code = codes[i].toLowerCase();
   		   var text = list[code];
    		   
   		   if (text != null) {
               var icon = base + "trade_"+code+".png";
               html += "<img src='" + icon + "' title='"+ text + "'/>";
   		   }
   	   }
  	   if (planet.starport != "X") {
   		   var icon = base + "port_"+getStarPort(planet).toLowerCase()+".png";
   		   var text = "Class " + planet.starport + " Starport";
   		   html += "<img src='" + icon + "' title='"+ text + "'/>";
   	   }
    	   
   	   return html;
    },

		
	version: function() {
		return "0.1";
	}
};

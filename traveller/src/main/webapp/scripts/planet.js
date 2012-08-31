/*
 * planet.js
 *
 * Copyright (C) 2012 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 * 
 * Define the Planet class.
 */


/**
 * A Planet class.
 */
function Planet (planet) {
	this.planet = planet;
}

Planet.prototype.getName = function() {
	return "" + this.planet.name;
};

Planet.prototype.getId = function() {
	return this.planet.id;
};

Planet.prototype.isMainWorld = function() {
	if (this.mainWorld == true) {
		return true;
	}
	return false;
};

Planet.prototype.isMoon = function() {
	return this.planet.isMoon;
}

/**
 * Gets the type of the planet, pretty formatted.
 * 
 * @param planet	Planet object to get the type for.
 * @returns			Pretty formatted planet type.
 */
Planet.prototype.getType = function() {
    return WG.formatEnum(this.planet.type);
};

Planet.prototype.getDistance = function() {
   if (this.planet.isMoon) {
	   return WG.addCommas(this.planet.distance) + " km";
   }
   return WG.addCommas(this.planet.distance) + " MKm";
};

Planet.prototype.getRadius = function() {
   if (this.planet.tradeCodes.indexOf("As") != -1) {
	   return WG.addCommas(this.planet.radius * 4 + " Mkm");
   }
   
   return WG.addCommas(this.planet.radius) + " km";
};

Planet.prototype.isPopulated = function() {
	return (this.planet.population > 0);
}
   
Planet.prototype.getPopulation = function() {
    return WG.addCommas(this.planet.population);
};
   
Planet.prototype.getAtmosphere = function() {
	if (this.planet.pressure == "None") {
		return "Vacuum";
	} else if (this.planet.pressure == "Standard") {
	    return WG.formatEnum(this.planet.atmosphere);
	} else {
	    return WG.formatEnum(this.planet.pressure + " " + this.planet.atmosphere);
	}
};

Planet.prototype.getTemperature = function() {
	return WG.formatEnum(this.planet.temperature);
};
   
Planet.prototype.getHydrographics = function() {
	return this.planet.hydrographics + "%";
};
   
Planet.prototype.getGovernment = function() {
	return WG.formatEnum(this.planet.government);
};
   
Planet.prototype.getAxialTilt = function() {
	if (this.planet.tradeCodes.indexOf("As") != -1) {
        return "--";
    }
	return this.planet.axialTilt + "&#176;";
};

Planet.prototype.getDayLength = function() {
    if (this.planet.tradeCodes.indexOf("As") != -1) {
        return "--";
    }
	return this.planet.dayLengthText;
},
   
Planet.prototype.getLawLevel = function() {
    switch (this.planet.lawLevel) {
    case 0: return "0 (Lawless)";
    case 1: return "1 (Libertarian)";
    case 2: return "2 (Liberal)";
    case 3: return "3 (Typical)";
    case 4: return "4 (Strict)";
    case 5: return "5 (Restrictive)";
    case 6: return "6 (Authoritarian)";
    }
	return "" + this.planet.lawLevel;
};
   
Planet.prototype.getTechLevel = function() {
    switch (this.planet.techLevel) {
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
 	    return this.planet.techLevel + " (Interstellar)";
    case 12: case 13: case 14:
        return this.planet.techLevel + " (Low Imperium)";
    case 15: case 16: case 17:
        return this.planet.techLevel + " (High Imperium)";
    case 18: case 19: case 20:
        return this.planet.techLevel + " (Advanced)";
    default:
        return this.planet.techLevel + " (Magic)";
    }
    return this.planet.techLevel;
};
   
Planet.prototype.getLifeLevel = function() {
	return WG.formatEnum(this.planet.lifeLevel);
};
   
Planet.prototype.getStarPort = function() {
	return "" + this.planet.starport;
};
   
Planet.prototype.getTradeCodes = function() {
	return "" + this.planet.tradeCodes;
};

Planet.prototype.isBelt = function() {
	if ((""+this.planet.tradeCodes).indexOf("As") > -1) {
		return true;
	}
	return false;
};
   
Planet.prototype.getTradeIcons = function() {
	var codes = this.getTradeCodes().split(" ");
	   
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
	   if (this.planet.starport != "X") {
		   var icon = base + "port_"+this.getStarPort().toLowerCase()+".png";
		   var text = "Class " + this.planet.starport + " Starport";
		   html += "<img src='" + icon + "' title='"+ text + "'/>";
	   }
	   
	   return html;
};

Planet.prototype.getDescription = function() {
	return this.planet.description;
};

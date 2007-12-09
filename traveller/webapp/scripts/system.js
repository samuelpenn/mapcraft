/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/01/01 11:04:14 $
 */


function selectStar(id) {
	var i = 0;
	for (i = 0; i < 9; i++) {
		var		div = document.getElementById("planets_"+i);
		if (div != null && i != id) {
			div.style.display = "none";
		}
	}
	
	div = document.getElementById("planets_"+id);
	if (div != null) {
		div.style.display = "block";
	}
}

/**
 * Used to handle clicking on the image map in mapinfo.xhtml
 * 
 * Author: Samuel Penn
 */

/**
 * Called when the world map is clicked on. Grab the coordinates
 * and load the map view page centred on the correct location.
 */
function mapClick(event) {
	var		x = event.offsetX;
	var		y = event.offsetY;
	
	var		imageWidth = $("#worldmap").width();
	var		mapWidth = $("#mapWidth").text().replace(/[^0-9]/g, "");
	var		mapName = $("#mapName").text();
			
//	alert(""+imageWidth+" ("+x+","+y+")");
	
	x = Math.floor((x * mapWidth)/imageWidth);
	y = Math.floor((y * mapWidth)/imageWidth);
	
	var		url = "map.xhtml?name="+mapName+"&x="+x+"&y="+y;
	
	document.location = url;
}


function pageLoaded() {
	$("#worldmap").click(mapClick);
}

$(document).ready(pageLoaded);


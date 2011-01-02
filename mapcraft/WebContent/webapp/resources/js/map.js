/**
 * Javascript code for map.xhtml
 * 
 * Author: Samuel Penn
 */

function dragMap(event) {
	var		x = event.pageX - xDragOrigin;
	var		y = event.pageY - yDragOrigin;
	
	if (!dragging) {
		//return;
	}
	
	for (var i=0; i < tiles.length; i++) {
		var		left = $(tiles[i]).css('left').replace(/[^0-9]/g, "");
		var		top = $(tiles[i]).css('top').replace(/[^0-9]/g, "");
		
		//var		left = (""+document.getElementById(tiles[i].substring(1)).style.left).replace(/[^0-9]/g, "");
		//var		top = (""+document.getElementById(tiles[i].substring(1)).style.top).replace(/[^0-9]/g, "");
		//$('#data').html("<p>"+left+","+top+"</p>");
		
		left = 1 * left;
		top = 1 * top;
		left += x;
		top += y;
		
		//document.getElementById(tiles[i].substring(1)).style.left = left+"px";
		//document.getElementById(tiles[i].substring(1)).style.top = top+"px";
		$(tiles[i]).css('left', left+"px");
		$(tiles[i]).css('top', top+"px");
	}
	
	xDragOrigin = event.pageX;
	yDragOrigin = event.pageY;
}

function displayMap() {
	var	x = mapX - mapX%32;
	var	y = mapY - mapY%40;

	for (var tx=0; tx < 3; tx++) {
		for (var ty=0; ty < 2; ty++) {
			var		x = xOrigin + tx*32;
			var		y = yOrigin + ty*40;
			
			var		id="tile_"+x+"_"+y;
			$("#map").append("<img id='"+id+"' src='/mapcraft/rest/map/"+mapName+"?x="+x+"&y="+y+"&b=true' width='"+tileWidth+"' height='"+tileHeight+"'/>");
			var jid = '#'+id;
			$(jid).css('position', 'absolute');
			$(jid).css('left', tx * tileWidth+'px');
			$(jid).css('top', ty * tileHeight+'px');
			
			tiles.push(jid);
			$(jid).draggable({ drag: function(event,ui) { dragMap(jid,event,ui);}});
		}
	}
	/*
	$('#map').mousedown(function(event) {
		// Begin drag event.
		//dragging = true;
		xDragOrigin = event.pageX;
		yDragOrigin = event.pageY;
	});
	$('#map').mousemove(function(event) {
		dragMap(event);
	});
	$('#map').mouseleave(function(event) {
		dragging = false;
	});
	$('#map').mouseup(function(event) {
		dragging = false;
	});
	*/
}

/**
 * Initial onload function. Needs to be called from jQuery.
 * 
 */
function pageLoaded() {
	mapName = $("#name").val();
	mapX = $("#x").val();
	mapY = $("#y").val();
	
	xOrigin = mapX - mapX%32;
	yOrigin = mapY - mapY%40;
	
	displayMap();
}

// Global variables
var		mapX = 0;
var		mapY = 0;
var		mapName = "";

var		tiles = new Array();
var		xOffset = 0;
var		yOffset = 0;
var		xOrigin = 0;
var		yOrigin = 0;

var		tileWidth = 768;
var		tileHeight = 1107;

var		dragging = false;
var		xDragOrigin = 0;
var		yDragOrigin = 0;


// Initiate callbacks when page has finished loading.
$(document).ready(pageLoaded);

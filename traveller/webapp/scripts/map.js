/**
 * map.js
 *
 * Author: Samuel Penn
 * License: GPL v2
 *
 * Display an interactive Traveller style map which can be dragged around,
 * and which displays information about selected systems and sectors.
 */


// Array holding metadata on all the image tiles currently in memory.
// We keep a 4x4 grid of subsectors loaded at all times. We don't really
// need to do this, but it simplifies things.
var     tile = new Array();
var     subSectors = [ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P" ];

// Pixel offset defining where to draw the 'top left' sub sector.
// Will always be zero or negative. If positive, we need to rebase
// the tiles to load a new 'top left' sub sector (and corresponding
// row or column).
var     baseX = 0;
var     baseY = 0;

// The galactic coordinates of the sector for the top left subsector.
// (these are now set in the JSP, so we don't define them here).
if (sectorX == undefined) {
	var  sectorX = 0;
	var  sectorY = 0;
}

// The galactic coordinates of the top left subsector (always 0-3).
var     subX = 0, subY = 0;

var     currentSubX = 0;
var     currentSubY = 0;

// The size of each image tile, in pixels.
var     SCALE = 48;
var     WIDTH = SCALE * 12;
var     HEIGHT = 1108 * (SCALE / 64.0);
//var     WIDTH = 768;
//var     HEIGHT = 1108;
// Number of tiles to cache. This is also the maximum number to display.
var     COLUMNS = 4;
var     ROWS = 4;

var     viewPort = null;
var     MAP_WIDTH = 0;
var     MAP_HEIGHT = 0;

// Where we can load the sector tiles from. This is a servlet, which
// needs to have parameters added to the URL. Returns a JPEG as a
// datastream.
//var     GET = "http://www.glendale.org.uk/traveller/data/get?";
//var     BASE = "http://www.glendale.org.uk/traveller/data/subsector";
var     GET = "get?";
var     BASE = "subsector";

var     files = new Array();
var     maps = new Array();
var		dragging = false;
var     dragX = 0;
var     dragY = 0;

function setMessage(message) {
//    var     t = document.getElementById("text");
//    t.childNodes[0].nodeValue = message;
}

/**
 * Return URL to the subsector image.
 */
function getImageUrl(x, y, sx, sy) {
    return BASE+"?x="+x+"&y="+y+"&sx="+sx+"&sy="+sy+"&scale="+SCALE;
}

function getImageId(x, y, sx, sy) {
    return "TILE_"+(x+10)+"_"+(y+10)+"_"+sx+""+sy;
}


function displayMap() {
    div = document.getElementById("map");

    var     min_ssx = 10000;
    var     max_ssx = -10000;
    var     min_ssy = 10000;
    var     max_ssy = -10000;

    // Display the map tiles that we need to display.
    for (var py = baseY; py < MAP_HEIGHT; py += HEIGHT) {
        for (var px = baseX; px < MAP_WIDTH; px += WIDTH) {
            // Offsets of current tile in number of subsectors.
            var     xo = Math.floor((px - baseX)/WIDTH);
            var     yo = Math.floor((py - baseY)/HEIGHT);

            var     sx = 0 + subX + xo;
            var     sy = 0 + subY + yo;

            var     x  = 0 + sectorX;
            var     y = 0 + sectorY;

            while (sx > 3) {
                sx -= 4;
                x++;
            }
            while (sy > 3) {
                sy -= 4;
                y++;
            }

            while (sx < 0) {
                sx += 4;
                x--;
            }

            while (sy < 0) {
                sy += 4;
                y--;
            }

            if ((x*4 + sx) < min_ssx) min_ssx = x*4 + sx;
            if ((x*4 + sx) > max_ssx) max_ssx = x*4 + sx;
            if ((y*4 + sy) < min_ssy) min_ssy = y*4 + sy;
            if ((y*4 + sy) > max_ssy) max_ssy = y*4 + sy;

            tile = document.getElementById(getImageId(x, y, sx, sy));
            if (tile == null) {
	            //alert("Get tile ["+getImageId(x, y, sx, sy)+"]");
                tile = document.createElement("img");
                tile.id = getImageId(x, y, sx, sy);
                tile.src = getImageUrl(x, y, sx, sy);
                tile.style.position = "absolute";
                tile.style.left = ""+px+"px";
                tile.style.top = ""+py+"px";
                tile.onmousedown = beginDrag;
//                tile.onmouseup = stopDrag;
                tile.ssx = x*4 + sx;
                tile.ssy = y*4 + sy;
                tile.sectorX = x;
                tile.sectorY = y;
                tile.subX = sx;
                tile.subY = sy;
                viewPort.appendChild(tile);
            } else {
                tile.style.left = ""+px+"px";
                tile.style.top = ""+py+"px";
            }
        }
    }

    // Finally, remove any tiles which aren't being used.
    var     tiles = viewPort.childNodes;
    var     list = "";
    for (var i=0; i < tiles.length; i++) {
        var     tile = tiles[i];
        if (tile.id == null || tile.id.indexOf("TILE_") != 0) {
            continue;
        }
        list += "("+tile.ssx+","+tile.ssy+") ";
        if (tile.ssx < min_ssx || tile.ssx > max_ssx || tile.ssy < min_ssy || tile.ssy > max_ssy) {
            viewPort.removeChild(tile);
        }
    }
    //setMessage(tiles.length+"; "+min_ssx+"/"+max_ssx+", "+min_ssy+"/"+max_ssy+": "+list);
}

/**
 * All the HTML on the page has finished loading. We can now set up
 * our initial tiles etc.
 */
function loadedPage() {
    viewPort = document.getElementById("map");
    if (viewPort == null) {
        alert("No parent");
    }
    MAP_WIDTH = viewPort.offsetWidth;
    MAP_HEIGHT = viewPort.offsetHeight;

    displayMap();
    return;
}

function rebaseMap() {
    if (dragging == false) {
        return;
    }

    if (baseX > 0) {
        baseX -= WIDTH;
        subX--;
        if (subX < 0) {
            subX = 3;
            sectorX--;
        }
    }

    if (baseY > 0) {
        baseY -= HEIGHT;
        subY--;
        if (subY < 0) {
            subY = 3;
            sectorY--;
        }
    }

    displayMap();
}

function beginDrag(event) {
    img = event.target;
    dragX = event.screenX;
    dragY = event.screenY;
    dragging = true;
    img.onmousemove = dragMap;

    return false;
}

function findPosX(obj) {
    var curleft = 0;
    if(obj.offsetParent) {
        while(1) {
          curleft += obj.offsetLeft;
          if(!obj.offsetParent)
            break;
          obj = obj.offsetParent;
        }
    } else if(obj.x) {
        curleft += obj.x;
    }
    return curleft;
  }

function findPosY(obj) {
    var curtop = 0;
    if(obj.offsetParent) {
        while(1) {
          curtop += obj.offsetTop;
          if(!obj.offsetParent)
            break;
          obj = obj.offsetParent;
        }
    } else if(obj.y) {
        curtop += obj.y;
    }
    return curtop;
  }


function stopDrag(event) {
    img = event.target;
    img.onmousemove = null;

    var moveX = 0 + event.screenX - dragX;
    var moveY = 0 + event.screenY - dragY;

    dragging = false;

    // This is a click, rather than a drag.
    if (moveX == 0 && moveY == 0) {
        var     ssx = 0 + event.target.ssx;
        var     ssy = 0 + event.target.ssy;

        if (ssx == null || ssy == null) {
            return false;
        }
        var		offsetX, offsetY;
        if (event.offsetX) {
        	// Konqueror
        	offsetX = event.offsetX;
        	offsetY = event.offsetY;
        } else {
        	// Mozilla
        	var left = findPosX(event.target);
        	var top = findPosY(event.target);
        	offsetX = event.clientX - left;
        	offsetY = event.clientY - top;
        }

        // Very rough estimate of which hex the click occurred in.
        var     imgX = Math.floor(offsetX / (WIDTH/8)) + 1;
        var     imgY = Math.floor(offsetY / (HEIGHT/10)) + 1;
        if (imgX % 2 == 0) {
            imgY = Math.floor((offsetY-HEIGHT/20) / (HEIGHT/10)) + 1;
        }
        // Find the current sector
        var     x = 0 + event.target.sectorX;
        var     y = 0 + event.target.sectorY;
        // Find the current subsector
        currentSubX = 0 + event.target.subX * 8 + imgX;
        currentSubY = 0 + event.target.subY * 10 + imgY;

	    //var     divNode = document.getElementById("sector");
	    //divNode.innerHTML="<p>Coordinate "+x+","+y+"  "+currentSubX+","+currentSubY+"</p>";

        updateSectorInfo(x, y);
    }

    return false;
}

function dragMap(event) {
	if (!dragging) return false;
    var moveX = event.screenX - dragX;
    var moveY = event.screenY - dragY;
    dragX = event.screenX;
    dragY = event.screenY;
    baseX += moveX;
    baseY += moveY;
    rebaseMap();

    return false;
}

function keyPressed(event) {
	var		moveX = 0;
	var		moveY = 0;
	var		shift = 64;
	
	switch (event.keyCode) {
	case 37: // Left
		moveX += shift;
		break;
	case 38: // Up
		moveY += shift;
		break;
	case 39: // Right
		moveX -= shift;
		break;
	case 40: // Down
		moveY -= shift;
		break;
	}
    baseX += moveX;
    baseY += moveY;
    dragging = true;
    rebaseMap();
    dragging = false;
	return false;
}

function updateSectorInfo(x, y) {
    var     url = GET+"type=sector&format=xml&x="+x+"&y="+y+"&detailX="+currentSubX+"&detailY="+currentSubY;
    httpRequest("GET", url, true, gotSectorInfo);
}

function gotSectorInfo() {
    if (request.readyState != 4) {
    	setMessage("ReadyState = "+request.readyState);
        return;
    }
    var     divNode = document.getElementById("sector");
    if (request.status == 200) {
        var     root = request.responseXML;
        var     sectorNode = root.getElementsByTagName("sector")[0];        
        var     systems = root.getElementsByTagName("system").length;
        var     sectorName = sectorNode.getAttribute("name");
        var     sectorId = sectorNode.getAttribute("id");
        var     subSector = "";
        var     systemName = "";
        var		systemId = 0;
        var		systemAllegiance = "";

        var     subNodes = root.getElementsByTagName("subsector");
        var		node = null;
        for (i = 0; i < subNodes.length; i++) {
            var     x = subNodes[i].getAttribute("x");
            var     y = subNodes[i].getAttribute("y");
            
            if (x == Math.floor(currentSubX/8) && y == Math.floor(currentSubY/10)) {
                subSector = subNodes[i].getAttribute("name");
                break;
            }
        }

		// Get data on this system user has clicked on.
        var     systemNodes = root.getElementsByTagName("system");
        for (i = 0; i < systemNodes.length; i++) {
            var     x = systemNodes[i].getAttribute("x");
            var     y = systemNodes[i].getAttribute("y");

            if (x == currentSubX && y == currentSubY) {
                systemName = systemNodes[i].getAttribute("name");
                systemId = systemNodes[i].getAttribute("id");
                node = systemNodes[i].getElementsByTagName("allegiance");
                if (node != null && node.length > 0) {
                	systemAllegiance = node[0].childNodes[0].nodeValue;
                }
                break;
            }
        }
        
        // Sector data includes full info on the user's selected system.
        // Find this and pull out the data on the first populated world.
        var		planetNodes = root.getElementsByTagName("planet");
        var		planetPopulation = -1, planetNumber = 0;
        for (var p=0; planetNodes != null && p < planetNodes.length; p++) {
        	node = planetNodes[p].getElementsByTagName("population")[0];
        	if (node != null) planetPopulation = node.childNodes[0].nodeValue;
        	if (planetPopulation > 0) {
        		planetNumber = p+1;
        		// Type
        		node = planetNodes[p].getElementsByTagName("type")[0];
        		if (node != null) planetType = node.childNodes[0].nodeValue;
        		// Government
        		node = planetNodes[p].getElementsByTagName("government")[0];
        		if (node != null) planetGovernment = node.childNodes[0].nodeValue;
        		// Atmosphere
        		node = planetNodes[p].getElementsByTagName("atmosphere")[0];
        		if (node != null) planetAtmosphere = node.childNodes[0].nodeValue;
        		// Pressure
        		node = planetNodes[p].getElementsByTagName("pressure")[0];
        		if (node != null) planetPressure = node.childNodes[0].nodeValue;
        		// Hydrographics
        		node = planetNodes[p].getElementsByTagName("hydrographics")[0];
        		if (node != null) planetHydrographics = node.childNodes[0].nodeValue;
        		// Temperature
        		node = planetNodes[p].getElementsByTagName("temperature")[0];
        		if (node != null) planetTemperature = node.childNodes[0].nodeValue;
        		// Tech
        		node = planetNodes[p].getElementsByTagName("tech")[0];
        		if (node != null) planetTech = node.childNodes[0].nodeValue;
        		// Law
        		node = planetNodes[p].getElementsByTagName("law")[0];
        		if (node != null) planetLaw = node.childNodes[0].nodeValue;
        		
        		break;
        	}
        }
        
        var		sectorText;        
        sectorText = "<p><b><a href=\""+GET+"type=sector&format=html&id="+sectorId+"\">"+sectorName+"</a></b> ("+systems+" systems) / "+subSector+"</p>";
        if (systemName != "") {
	        sectorText += "<p>";
        	sectorText += "<a href=\""+GET+"type=system&format=html&id="+systemId+"\">"+systemName+"</a>: ("+systemAllegiance+") "+planetNodes.length+" planets";
        	if (planetNumber > 0) {
        		sectorText+=", Main world: planet "+planetNumber+" ("+planetType+"); ";
        		sectorText+=""+planetTemperature+", "+planetAtmosphere+" atmosphere ("+planetPressure+")";
        	}
	        sectorText += "</p>";
        }
        if (planetPopulation > 0) {
	        sectorText += "<p>";
        	sectorText += "<b>Population:</b> "+addCommas(planetPopulation);
        	sectorText += "; <b>Government:</b> "+planetGovernment;
        	sectorText += "; <b>Tech Level:</b> "+planetTech;
        	sectorText += "; <b>Law Level:</b> "+planetLaw;
        	sectorText += "</p>";
        }
        divNode.innerHTML = sectorText;

    } else {
        divNode.nodeValue = "ERROR: "+request.status;
    }
	return false;
}

function addCommas(number) {
    number += "";
    x = number.split('.');
    x1 = x[0];
    x2 = x.length > 1 ? '.' + x[1] : '';
    var rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1)) {
        x1 = x1.replace(rgx, '$1' + ',' + '$2');
    }
    return x1 + x2;
}

function changeScale(target) {
	var     idx = target.selectedIndex;
    var     scale = target.options[idx].value;
    setScale(scale);
}

function setScale(scale) {
	SCALE = scale;
    WIDTH = SCALE * 12;
    HEIGHT = 1108 * (SCALE / 64.0);
    // Remove all current maps
    
    while (viewPort.childNodes.length > 0) {
    	viewPort.removeChild(viewPort.childNodes[0]);
    }
    displayMap();
}


document.onmouseup = stopDrag;
document.onkeypress = keyPressed;

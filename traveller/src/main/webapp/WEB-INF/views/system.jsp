<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page pageEncoding="UTF-8"%>
<%@ page session="false" %>
<html>
	<head>
		<title>WorldGen</title>
		<link rel="stylesheet" href="/traveller/css/default.css"/> 
        <link rel="stylesheet" href="/traveller/css/system.css"/> 
		<script type="text/javascript" src="/traveller/scripts/jquery.js"></script>
	    <script type="text/javascript">
	       var  _system = null;
	    
	       function displaySystemData(system) {
	    	   _system = system;
	    	   
	    	   $("#title").html(system.sectorName + " / " + system.name);
	    	   
	    	   /*
	    	   $("#systemData").append("<b>Main world:</b> " + system.mainWorld.name + "<br/>");
               $("#systemData").append("<b>World type:</b> " + system.mainWorld.type + "<br/>");
               $("#systemData").append("<b>Distance:</b> " + system.mainWorld.distance + " Mkm<br/>");
               $("#systemData").append("<b>Radius:</b> " + system.mainWorld.radius + " km<br/>");
               $("#systemData").append("<b>Population:</b> " + system.mainWorld.population + "<br/>");
	    	   */
	    	   
	    	   for (var i = 0; i < system.stars.length; i++) {
	    		   var starName = system.stars[i].name;
	    		   starName += " (" + system.stars[i].spectralType + " " +
	    				   system.stars[i].classification + ")";
	    		   $("#systemStars").html("<h1>" + starName + "</h1>");
	    		   $("#systemStars").append("<div id='star" + system.stars[i].id + "'></div>");

	    		   $("#systemStars").append("<div id='links'></div>");
	    		   	    		   
	    		   listPlanetsForStar(system, system.stars[i].id);
	    	   }
	    	   
	    	   $("#systemStars").append("<div id='planetData'></div>");
	    	   
	    	   showPlanet(system.mainWorld.id);
	    	   
	    	   //$("#sectorMap").html(minX+","+maxX+","+minY+","+maxY);
	       }

	       function listPlanetsForStar(system, starId) {
               for (var i = 0; i < system.planets.length; i++) {
                   var planet = system.planets[i];
                   if (planet.parentId != starId) {
                       continue;
                   }
                   
                   var number = (planet.name+"").replace(/.* /g, "");
                   if (planet.id == system.mainWorld.id) {
                	   number += "*";
                   }
                   var html = "<span id='link" + planet.id + "' onclick='javascript: showPlanet(" + planet.id + ")'>";
                   html += number;
                   html += "</span>";
                   
                   $("#links").append(html);
               }
	       }
	       
	       function showPlanet(id) {
	    	   var planet = null;
               for (var i = 0; i < _system.planets.length; i++) {
                   var p = _system.planets[i];
                   if (p.id == id) {
                	   planet = p;
                       break;
                   }
               }
               if (planet == null) {
            	   $("#planetData").html("No planet selected");
            	   return;
               }
               
               $(".selected").removeClass("selected");
               $("#link"+id).addClass("selected");
               
               var fullname = planet.name + " (" + getType(planet) + ")";
           	   fullname += "<span id='codes'>" + getTradeIcons(planet) + "</span>";
               $("#planetData").html("<h2>" + fullname + "</h2>");
               
               if (planet.starport != "X") {
            	//    $("#planetData").append("<div id='starport'>" + planet.starport + "</div>");
               }
               
               $("#planetData").append("<canvas id='globe' width='200px' height='200px'>Not supported</canvas>");
               var texture="/traveller/api/planet/" + id + "/projection.jpg";
               // TODO: Need to cancel the previous animation. 
               createSphere(document.getElementById("globe"), texture);
               
               $("#planetData").append("<div id='statBlock'></div>");
               
               // It's a table. I can't think of a better way of laying this
               // out which doesn't involve a table.
               var para = "<table class='data'>";
               
               var labels = [ "Distance", "Radius", "Axial Tilt", "Day Length" ];
               var data = [ getDistance(planet), getRadius(planet), 
                            getAxialTilt(planet), getDayLength(planet) ];
               
               para += mkTable(labels, data);
               
               var labels = [ "Temperature", "Atmosphere", "Hydrographics", "Life" ];
               var data = [ getTemperature(planet), getAtmosphere(planet), 
                            getHydrographics(planet), getLifeLevel(planet) ];
               
               para += mkTable(labels, data);
               
               if (planet.population > 0) {
	               var labels = [ "Population", "Tech Level", "Government", "Law Level" ];
	               var data = [ getPopulation(planet), getTechLevel(planet), 
	                            getGovernment(planet), getLawLevel(planet) ];
	               
	               para += mkTable(labels, data);
               }
               
               para += "</table>";
               $("#statBlock").append(para);
               
               $("#planetData").append("<p id='description'>" + planet.description + "</p>")
               
               $("#planetData").append("<p style='clear:both'/>");

               $("#planetData").append("<div id='resources'></div>");
               
               $.getJSON("/traveller/api/planet/" + planet.id + "/resources", function(data) {
                   displayPlanetResources(data);
               });
	       }
	       
	       function displayPlanetResources(list) {
	    	   $("#resources").html("<h3>Resources</h3>");
	    	   	    	   
	    	   for (var i=0; i < list.length; i++) {
	    		   var c = list[i];
	    		   var image = "/traveller/images/trade/" + c.imagePath + ".png";
	    		   var name = c.name + " " + c.amount + "%";
	    		   var html = "<img src='"+image+"' width='64' height='64' title='"+name+"'/>";
	    		   
	    		   $("#resources").append(html);
	    		   
	    	   }
	       }
	       
	       function mkTable(labels, data) {
	    	   var    th = "";
	    	   var    td = "";
	    	   
	    	   for (var i=0; i < labels.length; i++) {
	    		   th += "<th>" + labels[i] + "</th>";
	    		   td += "<td>" + data[i] + "</td>"; 
	    	   }
	    	   
	    	   return "<tr>" + th + "</tr><tr>" + td + "</tr>";
	       }
	       
	       function addData(label, value) {
	    	   return "<dt>" + label + "</dt> <dd>" + value + "</dd>";
	       }
	       
	       function showPlanetsForStar(system, starId) {
	    	   for (var i = 0; i < system.planets.length; i++) {
	    		   var planet = system.planets[i];
	    		   if (planet.parentId != starId) {
	    			   continue;
	    		   }
	    		   
	    		   $("#systemStars").append("<div id='planet" + planet.id + "'></div>");
	    		   var pid = "#planet" + planet.id;
	    		   
	    		   $(pid).append("<h2>" + planet.name + " (" + planet.type + ")</h2>");
	    		   
	    		   var para = "";
	    		   para += "<b>Distance: </b>" + getDistance(planet) + "; ";
                   para += "<b>Radius: </b>" + getRadius(planet) + "; ";
                   para += "<b>Axial Tilt: </b>" + getAxialTilt(planet) + ";";
                   para += "<b>Length of Day: </b>" + getDayLength(planet) + ";";
                   para += "<b>Temperature: </b>" + getTemperature(planet) + "; ";
                   para += "<b>Atmosphere: </b>" + getAtmosphere(planet) + "; ";
                   para += "<b>Hydrographics: </b>" + getHydrographics(planet) + ";";
                   $(pid).append("<p>" + para + "</p>");
                   
                   if (planet.population > 0) {
	                   para = "";
	                   para += "<b>Population: </b>" + getPopulation(planet) + "; ";
	                   para += "<b>Government: </b>" + getGovernment(planet) + "; ";
	                   $(pid).append("<p>" + para + "</p>");
                   }	    		   
	    		   
	    		   $(pid).append("<p id='description'>" + planet.description + "</p>");
	    	   }
	       }
	       
	       function addCommas(number) {
	    	    number += '';
	    	    x = number.split('.');
	    	    x1 = x[0];
	    	    x2 = x.length > 1 ? '.' + x[1] : '';
	    	    var rgx = /(\d+)(\d{3})/;
	    	    while (rgx.test(x1)) {
	    	        x1 = x1.replace(rgx, '$1' + ',' + '$2');
	    	    }
	    	    return x1 + x2;
	       }
	       
	       function formatEnum(val) {
	    	   if (val == null) {
	    		   return "";
	    	   } else {
		    	   val = "" + val;
	               return val.replace(/([A-Z])/g, " $1").trim();
	    	   }	    	   
	       }
	       
	       function getType(planet) {
	    	   return formatEnum(planet.type);
	       }
	       
	       function getDistance(planet) {
	    	   return addCommas(planet.distance) + " MKm";
	       }
	       
	       function getRadius(planet) {
	    	   return addCommas(planet.radius) + " km";
	       }
	       
	       function getPopulation(planet) {
	    	   return addCommas(planet.population);
	       }
	       
	       function getAtmosphere(planet) {
	    	   if (planet.pressure == "None") {
	    		   return "Vacuum";
	    	   } else if (planet.pressure == "Standard") {
	    		   return formatEnum(planet.atmosphere);
	    	   } else {
	    		    return formatEnum(planet.pressure + " " + planet.atmosphere);
	    	   }
	       }
	       
	       function getTemperature(planet) {
	    	   return formatEnum(planet.temperature);
	       }
	       
	       function getHydrographics(planet) {
	    	   return planet.hydrographics + "%";
	       }
	       
	       function getGovernment(planet) {
	    	   return formatEnum(planet.government);
	       }
	       
	       function getAxialTilt(planet) {
	    	   return planet.axialTilt + "&#176;";
	       }
	       
	       function getDayLength(planet) {
	    	   return planet.dayLengthText;
	       }
	       
	       function getLawLevel(planet) {
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
	       }
	       
	       function getTechLevel(planet) {
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
	       }
	       
	       function getLifeLevel(planet) {
	    	   return formatEnum(planet.lifeLevel);
	       }
	       
	       function getStarPort(planet) {
	    	   return "" + planet.starport;
	       }
	       
	       function getTradeCodes(planet) {
	    	   return "" + planet.tradeCodes;
	       }
	       
	       function getTradeIcons(planet) {
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
	       }
	       
	       $(document).ready(function() {
	    	   
	    	   $.getJSON("/traveller/api/system/${systemId}", function(data) {
	               displaySystemData(data);
	    	   });
	    	   
	    	   /*
               var canvas = document.getElementById("sphere");
               var context = canvas.getContext("2d");
               context.strokeStyle = "#000000";
               context.fillStyle = "#FFFF00";
               context.beginPath();
               context.arc(100,100,50,0,Math.PI*2,true);
               context.closePath();
               context.stroke();
               context.fill();
               */
               //var texture="http://localhost:8080/traveller/api/planet/488/projection.jpg";
               //var texture="http://localhost:8080/traveller/images/earth1024x1024.jpg";
               //createSphere(document.getElementById("globe"), texture);
	       });
	    </script>
	    
	    <script type="text/javascript" src="/traveller/scripts/samhaslers/requestanimationframe.polyfill.js"></script>
	    <script type="text/javascript" src="/traveller/scripts/samhaslers/sphere.js"></script>
	    
	    <script type="text/javascript">
	        function go() {
	        	var texture="http://localhost:8080/traveller/api/planet/440/projection.jpg";
	        	//createSphere(document.getElementById("sphere"), texture);
	        	
	        	
	        }
	    </script>
	    
	    <style>
	       #sidebar {
	           float: left;
	           width: 10em;
	       }
	       
	       #systemBody {
	           margin-left: 12em;
	       }
	    </style>
	</head>
	
	
	<body>
	   <div id="header">
	       <h1 id="title">WorldGen</h1>
	   </div>

		<div class="container">
		    <div id="sidebar">
				<h2>Systems</h2>
	
				<ul id="systemList"></ul>
            </div>
            			
			<div id="systemBody">
			    <div id="systemData">
			    </div>
			    
			    <div id="systemStars">
			    </div>
			</div>
			
		</div>
	</body>
</html>
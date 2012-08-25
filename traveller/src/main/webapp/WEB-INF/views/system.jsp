<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page pageEncoding="UTF-8"%>
<%@ page session="false" %>
<html>
	<head>
		<title>WorldGen</title>
		<link rel="stylesheet" href="/traveller/css/default.css"/> 
		<script type="text/javascript" src="/traveller/scripts/jquery.js"></script>
	    <script type="text/javascript">
	       function displaySystemData(system) {
	    	   
	    	   $("#title").html(system.sectorName + " / " + system.name);
	    	   
	    	   $("#systemData").append("<b>Main world:</b> " + system.mainWorld.name + "<br/>");
               $("#systemData").append("<b>World type:</b> " + system.mainWorld.type + "<br/>");
               $("#systemData").append("<b>Distance:</b> " + system.mainWorld.distance + " Mkm<br/>");
               $("#systemData").append("<b>Radius:</b> " + system.mainWorld.radius + " km<br/>");
               $("#systemData").append("<b>Population:</b> " + system.mainWorld.population + "<br/>");
	    	   
	    	   for (var i = 0; i < system.stars.length; i++) {
	    		   $("#systemStars").html("<h2>" + system.stars[i].name + "</h2>");
	    		   $("#systemStars").append("<div id='star" + system.stars[i].id + "'></div>");	    		   
	    		   showPlanetsForStar(system, system.stars[i].id);
	    	   }
	    	   
	    	   //$("#sectorMap").html(minX+","+maxX+","+minY+","+maxY);
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
	    		   
	    		   $(pid).append("<p>" + planet.description + "</p>");
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
	    	   return planet.lawLevel;
	       }
	       
	       function getTechLevel(planet) {
	    	   return planet.techLevel;
	       }
	       
	       function getLifeLevel(planet) {
	    	   return formatEnum(planet.lifeLevel);
	       }
	       
	       function getStarPort(planet) {
	    	   return planet.starport;
	       }
	       
	       $(document).ready(function() {
	    	   
	    	   $.getJSON("/traveller/api/system/${systemId}", function(data) {
	               displaySystemData(data);
	    	   });
	    	   
               var canvas = document.getElementById("sphere");
               var context = canvas.getContext("2d");
               context.strokeStyle = "#000000";
               context.fillStyle = "#FFFF00";
               context.beginPath();
               context.arc(100,100,50,0,Math.PI*2,true);
               context.closePath();
               context.stroke();
               context.fill();

               var texture="http://localhost:8080/traveller/api/planet/445/projection.jpg";
               //var texture="http://localhost:8080/traveller/images/earth1024x1024.jpg";
               createSphere(document.getElementById("sphere"), texture);
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
			    <canvas style="border: 2px solid black" id="sphere" width="300" height="300">
			        Need canvas support.
			    </canvas>
			    <div id="systemData">
			    </div>
			    
			    <div id="systemStars">
			    </div>
			</div>
			
			<!-- 
			
			<h4>New Sector</h4>
			
			<table>
				<tr>
					<th></th>
					<c:set var="endX" value="${maxX - minX + 1}"/>
					<c:forEach var="xx" begin="0" end="5">
					    <c:set var="x" value="${minX + 1}"/>   
						<th>${x}</th>	
					</c:forEach>
				</tr>
				
				<c:forEach var="yy" begin="0" end="${maxY+1}" step="1">
				    <c:set var="y" value="${maxY - yy}"/>
					<tr>
						<th>${y}</th>
					</tr>
				</c:forEach>
			</table>			
			
			<form>
				Name: <input id="name"/><br/>
				X: <input id="x"/><br/>
				Y: <input id="y"/><br/>
				Allegiance: <input id="allegiance"/><br/>
				Codes: <input id="codes"/><br/>
			</form>
			
			-->
		</div>
	</body>
</html>
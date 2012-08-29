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
        <script type="text/javascript" src="/traveller/scripts/worldgen.js"></script>
        <script type="text/javascript" src="/traveller/scripts/star.js"></script>
        <script type="text/javascript" src="/traveller/scripts/planet.js"></script>
        <script type="text/javascript" src="/traveller/scripts/system.js"></script>
	    <script type="text/javascript">
	       var  _system = null;
	    
	       function displaySystemData(system) {
	    	   _system = system;
	    	   WG.system = new StarSystem(system);
	    	   
               $("#title").html(WG.system.getFullName());
	    	   
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
	    		   	    		   
	    		   listPlanetsForStar(WG.system.getPlanets(system.stars[i].id));
	    	   }
	    	   
	    	   $("#systemStars").append("<div id='planetData'></div>");
	    	   
	    	   showPlanet(system.mainWorld.id);
	    	   
	    	   //$("#sectorMap").html(minX+","+maxX+","+minY+","+maxY);
	       }

	       function listPlanetsForStar(list) {
               for (var i = 0; i < list.length; i++) {
                   var planet = list[i];
                   
                   var number = (planet.getName()+"").replace(/.* /g, "");
                   if (planet.isMainWorld()) {
                	   number += "*";
                   }
                   var html = "<span id='link" + planet.getId() + "' onclick='javascript: showPlanet(" + planet.getId() + ")'>";
                   html += number;
                   html += "</span>";
                   
                   $("#links").append(html);
               }
	       }
	       
	       function showPlanet(id) {
               var planet = WG.getPlanet(id);
               if (planet == null) {
            	   $("#planetData").html("No planet selected");
            	   return;
               }
               
               $(".selected").removeClass("selected");
               $("#link"+id).addClass("selected");
               
               var fullname = planet.getName() + " (" + planet.getType() + ")";
           	   fullname += "<span id='codes'>" + planet.getTradeIcons() + "</span>";
               $("#planetData").html("<h2>" + fullname + "</h2>");
               
               if (planet.getStarPort() != "X") {
            	    $("#planetData").append("<div id='starport'>" + planet.getStarPort() + "</div>");
               }
               
               var radiusLabel = "Radius";
               
               $("#planetData").append("<canvas id='globe' width='200px' height='200px'>Not supported</canvas>");
               var texture="/traveller/api/planet/" + planet.getId() + "/projection.jpg";
               // TODO: Need to cancel the previous animation. 
               if (!planet.isBelt()) {
            	    createSphere(document.getElementById("globe"), texture);
               } else {
            	   drawAsteroids(planet, document.getElementById("globe"));
            	   radiusLabel = "Thickness";
               }

               $("#planetData").append("<div id='statBlock'></div>");
               
               // It's a table. I can't think of a better way of laying this
               // out which doesn't involve a table.
               var para = "<table class='data'>";
               
               var labels = [ "Distance", radiusLabel, "Axial Tilt", "Day Length" ];
               var data = [ planet.getDistance(), planet.getRadius(), 
                            planet.getAxialTilt(), planet.getDayLength() ];
               
               para += mkTable(labels, data);
               
               var labels = [ "Temperature", "Atmosphere", "Hydrographics", "Life" ];
               var data = [ planet.getTemperature(), planet.getAtmosphere(), 
                            planet.getHydrographics(), planet.getLifeLevel() ];
               
               para += mkTable(labels, data);
               
               if (planet.isPopulated()) {
	               var labels = [ "Population", "Tech Level", "Government", "Law Level" ];
	               var data = [ planet.getPopulation(), planet.getTechLevel(), 
	                            planet.getGovernment(), planet.getLawLevel() ];
	               
	               para += mkTable(labels, data);
               }
               
               para += "</table>";
               $("#statBlock").append(para);
               
               $("#planetData").append("<p id='description'>" + planet.getDescription() + "</p>")
               
               $("#planetData").append("<p style='clear:both'/>");

               $("#planetData").append("<div id='resources'></div>");
               
               $.getJSON("/traveller/api/planet/" + planet.getId() + "/resources", function(data) {
                   displayPlanetResources(data);
               });
	       }

	       function drawAsteroids(planet, canvas) {
               var context = canvas.getContext("2d");               
               if (context == null) {
                   return;
               }
               
               var image = new Image();
               image.src = "/traveller/api/planet/"+planet.getId()+"/orbit";
               image.onload = function () {
            	    context.drawImage(image, 5, 5, 190, 190);
               };
	    	   
	       }
	       
	       function displayPlanetResources(list) {
	    	   $("#resources").html("<h3>Resources</h3>");
	    	   
	    	   $("#resources").append("<ul id='r' class='iconList'></ul>");
	    	   for (var i=0; i < list.length; i++) {
	    		   var c = list[i];
	    		   var image = "/traveller/images/trade/" + c.imagePath + ".png";
	    		   var name = c.name + " " + c.amount + "%";
	    		   var html = "<img src='"+image+"' width='64' height='64' title='"+name+"'/>";
	    		   html = html + c.amount + "%";
	    		   
	    		   $("#r").append("<li>" + html + "</li>");
	    		   
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
	</head>
	
	
	<body>
	   <div id="header">
	       <h1 id="title">WorldGen</h1>
	   </div>

		<div class="container">
		    <!-- 
		    <div id="sidebar">
				<h2>Systems</h2>
	
				<ul id="systemList"></ul>
            </div>
            -->		
			<div id="systemBody">
			    <div id="systemData">
			    </div>
			    
			    <div id="systemStars">
			    </div>
			</div>
			
		</div>
	</body>
</html>
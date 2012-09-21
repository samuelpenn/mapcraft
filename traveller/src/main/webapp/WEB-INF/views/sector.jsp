<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page pageEncoding="UTF-8"%>
<%@ page session="false" %>
<html>
	<head>
		<title>${sectorName}</title>
		<link rel="stylesheet" href="/traveller/css/default.css"/> 
        <link rel="stylesheet" href="/traveller/css/sector.css"/> 
		<script type="text/javascript" src="/traveller/scripts/jquery.js"></script>
        <script type="text/javascript" src="/traveller/scripts/worldgen.js"></script>
        <script type="text/javascript" src="/traveller/scripts/star.js"></script>
        <script type="text/javascript" src="/traveller/scripts/planet.js"></script>
        <script type="text/javascript" src="/traveller/scripts/system.js"></script>
        <script type="text/javascript" src="/traveller/scripts/sector.js"></script>
	    <script type="text/javascript">
	       function displaySectorData(sectorData) {
	    	   WG.sector = new Sector(sectorData);
	    	   
               $("#title").html("${sectorName} (${sectorX}, ${sectorY})");

               // Display subsector information.
               for (var i = 0; i < 16; i++) {
            	   $("#data").append("<h2>" + WG.sector.subSectors[i] + "</h2>");
            	   
            	   var mx = 1 + (i % 4) * 8;
            	   var my = 1 + (i / 4) * 10;
            	   
            	   $("#data").append("<ul id='ss_" + i + "'></ul>");
            	   var si = "#ss_" + i;
            	   for (var s = 0; s < WG.sector.systems.length; s++) {
            		   var sys = WG.sector.systems[s];
            		   if (sys.getX() < mx || sys.getX() > mx + 7) {
            			   continue;
            		   }
            		   if (sys.getY() < my || sys.getY() > my + 9) {
            			   continue;
            		   }
            		   var href = "/traveller/ui/system/" + sys.getId();
            		   
            		   $(si).append("<li>" + sys.getCoords() + " <a href='"+href+"'>" + sys.getName() + "</a></li>");
            	   }
               }
	    	   
	    	   
	    	   $("#systemStars").append("<div id='planetData'></div>");
	    	   
	    	   showPlanetAndMoons(system.mainWorld.id);
	    	   
	    	   //$("#sectorMap").html(minX+","+maxX+","+minY+","+maxY);
	       }

	       function listPlanetsForStar(list) {
               for (var i = 0; i < list.length; i++) {
                   var planet = list[i];
                   
                   var number = (planet.getName()+"").replace(/.* /g, "");
                   if (planet.isMainWorld()) {
                	   number += "*";
                   }
                   var html = "<span id='link" + planet.getId() + "' onclick='javascript: showPlanetAndMoons(" + planet.getId() + ")'>";
                   html += number;
                   html += "</span>";
                   
                   $("#links").append(html);
               }
	       }
	       
	       function showPlanetAndMoons(id) {
               var planet = WG.getPlanet(id);
               if (planet == null) {
            	   $("#planetData").html("No planet selected");
            	   return;
               }
               $("#planetData").html("<div class='planet' id='p" + id + "'></div>");
               
               $(".selected").removeClass("selected");
               $("#link"+id).addClass("selected");

               showPlanet(id, planet);
               
               WG.loadMoons(id, function (id) { 
            	   $("#planetData").append("<div class='planet' id='p" + id + "'></div>");
            	   showPlanet(id, WG.getPlanet(id));
               });               
	       }
	       
	       function showPlanet(id, planet) {
               var divId = "#p"+id;
               
               var fullname = planet.getName() + " (" + planet.getType() + ")";
           	   fullname += "<span id='codes'>" + planet.getTradeIcons() + "</span>";
               $(divId).html("<h2>" + fullname + "</h2>");

               var radiusLabel = "Radius";
               
               $(divId).append("<canvas class='globe' id='globe"+id+"' width='200px' height='200px'>Not supported</canvas>");
               var texture="/traveller/api/planet/" + planet.getId() + "/projection.jpg";
               // TODO: Need to cancel the previous animation. 
               if (planet.isMoon() == false) {
	               if (!planet.isBelt()) {
	            	    createSphere(document.getElementById("globe"+id), texture);
	            	    //var s = new Sphere(document.getElementById("globe"+id), texture);
	            	    //s.draw();
	               } else {
	            	   drawAsteroids(planet, document.getElementById("globe"+id));
	            	   radiusLabel = "Thickness";
	               }
               }
               $(divId).append("<div id='statBlock"+id+"'></div>");
               
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
               $("#statBlock"+id).append(para);
               
               $(divId).append("<p class='description'>" + planet.getDescription() + "</p>");
               
               $(divId).append("<p style='clear:both'/>");

               $(divId).append("<div id='resources"+id+"'></div>");
               
               $.getJSON("/traveller/api/planet/" + planet.getId() + "/resources", function(data) {
                   displayPlanetResources(id, data);
               });
               
               if (planet.planet.facilities.length > 0) {
                   $(divId).append("<div id='facilities"+id+"'</div>'");
                   displayPlanetFacilities(planet);
               }

               $(divId).append("<div id='inventory"+id+"'></div>");
               WG.loadInventory(id, displayPlanetInventory);
               
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
	       
	       function displayPlanetResources(id, list) {
	    	   $("#resources"+id).html("<h3>Resources</h3>");
	    	   
	    	   $("#resources"+id).append("<ul id='r"+id+"' class='iconList'></ul>");
	    	   for (var i=0; i < list.length; i++) {
	    		   var c = list[i];
	    		   var image = "/traveller/images/trade/" + c.imagePath + ".png";
	    		   var name = c.name + " " + c.amount + "%";
	    		   var html = "<img src='"+image+"' width='64' height='64' title='"+name+"'/>";
	    		   html = html + c.amount + "%";
	    		   
	    		   $("#r"+id).append("<li>" + html + "</li>");
	    		   
	    	   }
	       }
	       
	       function displayPlanetFacilities(planet) {
	    	   var id = planet.getId();
	    	   $("#facilities"+id).html("<h3>Facilities</h3>");
	    	   
               $("#facilities"+id).append("<ul id='f"+id+"'></ul>");
               for (var i=0; i < planet.planet.facilities.length; i++) {
            	   var f = planet.planet.facilities[i];
            	   $("#f"+id).append("<li>" + f.title + " - " + f.installation_size + "</li>");
               }
	    	   
	       }
	       
	       function displayPlanetInventory(planet) {
	    	   if (planet.planet.inventory == null ||
	    			   planet.planet.inventory.length == 0) {
	    		   return;
	    	   }
	    	   
	    	   $("#inventory"+planet.getId()).html("<h3>Inventory</h3>");
	    	   
	    	   var id = "tableInv" + planet.getId();
	    	   $("#inventory"+planet.getId()).append("<table class='inventory' id='"+id+"'></table>");
	    	   id = "#" + id;
	    	   
	    	   $(id).append("<tr></tr>");
	    	   $(id + " tr").append("<th>Name</th>");
               $(id + " tr").append("<th>Quantity</th>");
	    	   $(id + " tr").append("<th>Price</th>");
               $(id + " tr").append("<th>Weekly In</th>");
               $(id + " tr").append("<th>Weekly Out</th>");
               $(id + " tr").append("<th>Produced</th>");
               $(id + " tr").append("<th>Consumed</th>");
               $(id + " tr").append("<th>Bought</th>");
               $(id + " tr").append("<th>Sold</th>");
	    	   
	    	   for (var i=0; i < planet.planet.inventory.length; i++) {
	    		   var item = planet.planet.inventory[i];
	    		   var row = "";
	    		   row += "<td class='name'>" + item.commodity.name + "</td>";
                   row += "<td>" + WG.addCommas(item.amount) + "</td>";
                   row += "<td>" + WG.addCommas(item.price) + "</td>";
                   row += "<td>" + WG.addCommas(item.weeklyIn) + "</td>";
                   row += "<td>" + WG.addCommas(item.weeklyOut) + "</td>";
                   row += "<td>" + WG.addCommas(item.produced) + "</td>";
                   row += "<td>" + WG.addCommas(item.consumed) + "</td>";
                   row += "<td>" + WG.addCommas(item.bought) + "</td>";
                   row += "<td>" + WG.addCommas(item.sold) + "</td>";
	    		   
                   var shade="";
                   if (i%2 == 1) {
                       shade="class='shade'";                	   
                   }
	    		   $(id).append("<tr "+shade+">" + row + "</tr>");
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
	    	   
	    	   $.getJSON("/traveller/api/sector/${sectorId}/data", function(data) {
	               displaySectorData(data);
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
	</head>
	
	
	<body>
	   <div id="header">
	       <h1 id="title">Sector</h1>
	   </div>

		<div class="container">
			<div id="sectorBody">
                <div id="data">
                </div>
			    <canvas id="map">
			    </canvas>
			</div>			
		</div>
	</body>
</html>
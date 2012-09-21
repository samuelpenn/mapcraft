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
	    	   drawSectorMap();
	    	   
	       }
	       var COS30 = Math.sqrt(3.0)/2.0;
	       var COS60 = 0.5;
	       var SIN60 = Math.sqrt(3.0)/2.0;
	       var SIN30 = 0.5;
	       var ROOT_TWO = Math.sqrt(2.0);
	       
	       function drawHex(context, x, y, size) {
	    	   x = 1.0 * x;
	    	   y = 1.0 * y;
	    	   var    topLeft_x = x;
	    	   var    top_y = y;
	    	   var    topRight_x = x + size;
	    	   var    right_x = topRight_x + (size * COS60);
	    	   var    middle_y = y - (size * SIN60);
	    	   var    bottom_y = y - 2 * (size * SIN60);
	    	   var    left_x = x - (size * COS60);
	    	   
	    	   context.beginPath();
	    	   context.lineWidth = 1;
	    	   context.moveTo(topLeft_x, top_y);
	    	   context.lineTo(topRight_x, top_y);
	    	   context.lineTo(right_x, middle_y);
	    	   context.lineTo(topRight_x, bottom_y);
	    	   context.lineTo(topLeft_x, bottom_y);
	    	   context.lineTo(left_x, middle_y);
	    	   context.closePath();
	    	   context.stroke();
	       }
	       
	       var LEFT_MARGIN = 25;
	       var TOP_MARGIN = 48;
	       var SCALE = 20;
	       
	       function getX(x, y) {
               return LEFT_MARGIN + (x*(SCALE * 1.5));
	       }
	       
	       function getY(x, y) {
               return (TOP_MARGIN + (x%2)*(SCALE*SIN60) + y*(SIN60*2*SCALE));
	       }
	       
	       function drawSectorMap() {
               var canvas = document.getElementById("map");
               var context = canvas.getContext("2d");
               
               context.strokeStyle = "#000000";
               context.fillStyle = "#FFFFFF";
                              
               for (var y=0; y < 40; y++) {
            	   for (var x=0; x < 32; x++) {
            		   drawHex(context, getX(x,y), getY(x,y), SCALE);
            	   }
               }
               
               context.fillStyle = "#FF9900";
               for (var i = 0; i < WG.sector.systems.length; i++) {
            	   var sys = WG.sector.systems[i];
            	   var x = sys.getX() - 1;
            	   var y = sys.getY() - 1;
                   context.beginPath();
                   context.arc(getX(x,y) + SCALE/2, getY(x,y) + SCALE/1.2, SCALE/3, 0, 2*Math.PI);
                   context.closePath();
                   context.fill();
               }

	       }

	       $(document).ready(function() {
	    	   $.getJSON("/traveller/api/sector/${sectorId}/data", function(data) {
	               displaySectorData(data);
	    	   });
	       });
	    </script>	    
	</head>
	
	
	<body>
	   <div id="header">
	       <h1 id="title">Sector</h1>
	   </div>

		<div class="container">
			<div id="sectorBody">
                <canvas id="map" width="1000px" height="1440px">
                </canvas>
                <div id="data">
                </div>
			</div>			
		</div>
	</body>
</html>
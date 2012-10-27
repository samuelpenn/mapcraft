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
            	   var my = 1 + Math.floor(i / 4) * 10;
            	   
            	   //$("#data").append("<canvas id='map"+i+"' width='660px' height='940px'></canvas>");
                   $("#data").append("<canvas id='map"+i+"' width='1584px' height='2256px'></canvas>");
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
                   drawSubSectorMap(i);
               }
	    	   //drawSectorMap();
	    	   
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
	    	   context.lineWidth = 3;
	    	   context.moveTo(topLeft_x, top_y);
	    	   context.lineTo(topRight_x, top_y);
	    	   context.lineTo(right_x, middle_y);
	    	   context.lineTo(topRight_x, bottom_y);
	    	   context.lineTo(topLeft_x, bottom_y);
	    	   context.lineTo(left_x, middle_y);
	    	   context.closePath();
	    	   context.stroke();
	       }
	       
	       var LEFT_MARGIN = 1;
	       var TOP_MARGIN = 2;
	       var SCALE = 20;
	       
	       function getX(x, y, s) {
               return (LEFT_MARGIN * s) + (x*(s * 1.5));
	       }
	       
	       function getY(x, y, s) {
               return ((TOP_MARGIN * s) + (x%2)*(s*SIN60) + y*(SIN60*2*s));
	       }
	       
	       function drawSectorMap() {
               var canvas = document.getElementById("map");
               var context = canvas.getContext("2d");
               
               context.strokeStyle = "#909090";
               context.fillStyle = "#FFFFFF";
                              
               for (var y=0; y < 40; y++) {
            	   for (var x=0; x < 32; x++) {
            		   drawHex(context, getX(x,y,SCALE), getY(x,y,SCALE), SCALE);
            	   }
               }
               
               context.fillStyle = "#FF9900";
               for (var i = 0; i < WG.sector.systems.length; i++) {
            	   var sys = WG.sector.systems[i];
            	   var x = sys.getX()-1;
            	   var y = sys.getY()-2;
                   context.beginPath();
                   context.arc(getX(x,y,SCALE) + SCALE/2, getY(x,y,SCALE) + SCALE/1.2, SCALE/3, 0, 2*Math.PI);
                   context.closePath();
                   context.fill();
               }

	       }
	       
	       function drawSubSectorMap(i) {
               var canvas = document.getElementById("map"+i);
               var context = canvas.getContext("2d");
               
               
               var mx = 1 + (i % 4) * 8;
               var my = 1 + Math.floor(i / 4) * 10;

               var scale = 120;
               for (var y=0; y < 10; y++) {
                   for (var x=0; x < 8; x++) {
                       context.strokeStyle = "#909090";
                       context.fillStyle = "#FFFFFF";
                       drawHex(context, getX(x,y,scale), getY(x,y,scale), scale);
                       
                       var xx = mx + x;
                       var yy = my + y;
                       if (xx < 10) xx = "0" + xx;
                       if (yy < 10) yy = "0" + yy;
                       var coord = xx + "" + yy;
                       
                       context.strokeStyle = "#000000";
                       context.fillStyle = "#777777";
                       context.font = "20pt Arial";
                       var px = getX(x,y,scale) + scale * 0.25;
                       var py = getY(x,y,scale) - scale * 1.4;
                       context.fillText(coord, px, py);
                   }
               }
               

               for (var s = 0; s < WG.sector.systems.length; s++) {
                   var sys = WG.sector.systems[s];
                   if (sys.getX() < mx || sys.getX() > mx + 7) {
                       continue;
                   }
                   if (sys.getY() < my || sys.getY() > my + 9) {
                       continue;
                   }
                   var x = sys.getX() - mx;
                   var y = sys.getY() - my;
                   context.beginPath();
                   context.fillStyle = "#000000";
                   context.arc(getX(x,y,scale) + scale/2, getY(x,y,scale) - scale/1.2, scale/8, 0, 2*Math.PI);
                   context.closePath();
                   context.stroke();
                   context.fill();
                   
                   // Display system name.
                   context.fillStyle = "#000000";
                   context.font = "20pt Arial";
                   var textWidth = context.measureText(sys.getName()).width;
                   var px = getX(x,y,scale) + scale * 0.55 - textWidth/2;
                   var py = getY(x,y,scale) - scale * 0.45;
                   context.fillText(sys.getName(), px, py);

                   // Display starport and TL
                   var main = sys.getMainWorld();
                   var text = main.getStarPort() + " / " + main.getTechLevel();
                   var textWidth = context.measureText(text).width;
                   var px = getX(x,y,scale) + scale * 0.55 - textWidth/2;
                   var py = getY(x,y,scale) - scale * 1.1;
                   context.fillText(text, px, py);
                   
                   // Display social information.
                   // Consists of Law Level, Government and Population.
                   var text = main.getLawLevel() + "/";
                   text += main.getGovernment(true) + "/";
                   text += main.getPopulation(true);
                   var textWidth = context.measureText(text).width;
                   var px = getX(x,y,scale) + scale * 0.55 - textWidth/2;
                   var py = getY(x,y,scale) - scale * 0.15;
                   context.fillStyle = "#555555";
                   context.fillText(text, px, py);
                   
                   // Now do the icons.
                   var base = "/traveller/images/symbols/64x64/";
                   var px = px = getX(x,y,scale) + scale * 0.9;
                   var py = getY(x,y,scale) - scale * 1.4;
                   var life = main.getLifeLevel();
                   if (life == "ComplexOcean") {
                	   life = "life_water.png";
                   } else if (life == "SimpleLand" || life == "ComplexLand") {
                	   life = "life_land.png";
                   } else if (life == "Extensive") {
                	   life = "life_extensive.png";
                   } else {
                	   life = null;
                   }
                   if (life != null) {
	                   var image = new Image();
	                   image.src = base + life;
	                   image.onload = function () {
	                        context.drawImage(image, px, py, 40, 40);
	                   };
                   }
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
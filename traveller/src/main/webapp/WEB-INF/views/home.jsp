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
	       function drawSectorMap(sectors) {
	    	   var    minX = 999, minY = 999;
	    	   var    maxX = -999, maxY = -999;
	    	   
               $("#sectorList").html("");
	    	   for (var i=0; i < sectors.length; i++) {
	    		   minX = Math.min(minX, sectors[i].x);
	    		   maxX = Math.max(maxX, sectors[i].x);
	    		   minY = Math.min(minY, sectors[i].y);
	    		   maxY = Math.max(maxY, sectors[i].y);
	    		   $("#sectorList").append("<li>" + sectors[i].name + " (" + 
	    				   sectors[i].x + "," + sectors[i].y + ")</li>");
	    	   }
	    	   
	    	   
	    	   $("#sectorMap").html("<table id='sectorTable'><tr id='smHdr'><th></th></tr></table>");
	    	   
	    	   for (var x = minX; x <= maxX; x++) {
	    		   $("#smHdr").append("<th>"+x+"</th>");
	    	   }
	    	   
	    	   var    i = 0;
	    	   for (var y = minY; y <= maxY; y++) {
	    		   var rowId = "sm"+y;
	    		   $("#sectorTable").append("<tr id='"+rowId+"'><th>"+y+"</th></tr>");
		    	   for (var x = minX; x <= maxX; x++) {
		    		   var sector = sectors[i++];
		    		   $("#"+rowId).append("<td><img src='/traveller/api/sector/"+sector.name+"/image' title='"+sector.name+"'/></td>");
		    	   }
	    	   }
	    	   
	    	   //$("#sectorMap").html(minX+","+maxX+","+minY+","+maxY);
	       }
	       $(document).ready(function() {
	    	   
	    	   $.getJSON("/traveller/api/sector/", function(data) {
	               drawSectorMap(data);

	    	   });
	    	   
	       });
	    </script>
	    
	    <style>
	       #sidebar {
	           float: left;
	           width: 10em;
	       }
	       
	       #sectorMap {
	           margin-left: 12em;
	       }
	    </style>
	</head>
	
	
	<body>
	   <div id="header">
	       <h1>WorldGen</h1>
	   </div>

		<div class="container">
		    <div id="sidebar">
				<h2>Sectors</h2>
	
				<ul id="sectorList"></ul>
            </div>
            			
			<div id="sectorMap">
			 Not loaded.
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
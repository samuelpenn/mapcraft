<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<%@ page import="java.util.*" %>

<%@ page import="uk.org.glendale.rpg.traveller.sectors.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.systems.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<%
			Sector		sector = null;
			String		sectorName = request.getParameter("name");
			String		sectorId = request.getParameter("id");
			
			if (sectorId != null) {
				try {
					int		id = Integer.parseInt(sectorId);
					sector = new Sector(id);
				} catch (NumberFormatException e) {
					
				}
			}
			if (sector == null && sectorName != null) {
				sector = new Sector(sectorName);
			}
			
			if (sector == null) {
				// If everything else has failed, get the core sector.
				sector = new Sector(0, 0);
			}
		%>
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<title>Third Imperium Mapping Project</title>

		<style>
			body {
				margin: 0px;
			}
			
			h1 {
				color: #aaaaff;
				background-color: #000099;
				margin: 0px;
				padding-top: 5px;
				padding-bottom: 5px;
				padding-left: 10px;
				border-bottom: 3pt solid #aaaaff;
			}
			
			div#sector {
				height: 48pt;
				width: 99%;
				color: black;
				background-color: #aaaaff;
				margin: 0pt;
				padding: 2pt;
				padding-right: 0pt;
				border: 2pt solid black;
				border-bottom: none;
			}
			
			div#sector p {
				margin: 0pt;
				padding: 0pt;
			}
			
			img {
				padding: 0px;
				margin: 0px;
				border: 0px none white;
			}
		</style>
		
		<script type="text/javascript">
			var		sectorX = <%= sector.getX() %>;
			var		sectorY = <%= sector.getY() %>
		</script>

        <script type="text/javascript" src="scripts/ajax.js">
        </script>
        <script type="text/javascript" src="scripts/map.js">
        </script>
	</head>

	<body onload="loadedPage()">
		<h1>Third Imperium Mapping Project</h1>
		
		<img src="images/imperial-starburst.gif" width="120" height="120" style="position: absolute; top:0px; right:0px"/>
		
		<div style="margin-right: 140px">
			<p>
				This is a project to map the <e>Third Imperium</e> in its entirity,
				from the stars and sectors down to the planets and moons themselves.
			</p>

			<p>			
				Return to <a href="knownspace.jsp">main map</a>.
			</p>
			
			<form>
				<select onchange="changeScale(this)">
					<option value="32">Small</option>
					<option value="48" selected="true">Medium</option>
					<option value="64">Large</option>
				</select>
			</form>
		</div>

        <div id="sector">
        </div>
        
        <div id="map" style="position: relative; width:99%; height:70%; border: 2pt solid black; overflow: hidden">
        </div>
        
        <p style="font-style: italic">
        	Map data generated from <a href="http://111george.com/core/">GNI data</a>. 
        	Map viewer by Samuel Penn, available under the GPLv2.
        	Home page at <a href="http://www.glendale.org.uk">www.glendale.org.uk</a>.
        </p>
	</body>
</html>
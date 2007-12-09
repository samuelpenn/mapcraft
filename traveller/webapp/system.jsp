<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<%@ page import="uk.org.glendale.rpg.traveller.sectors.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.systems.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.database.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.map.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<%
			StarSystem		system = null;
			String			name = null;
			
			try {
				int		id = Integer.parseInt(request.getParameter("id"));
				system = new StarSystem(id);
				name = system.getName();
			} catch (Throwable e) {
				system = null;
				name = "Unknown";
			}
		%>
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<link rel="STYLESHEET" type="text/css" media="screen" href="css/system.css" />
		<title><%= name %></title>
	</head>
	
	<body>
		<h1><%= name %></h1>
		
		<%
			if (system == null) {
				out.println("<p><b>No star system was found.</b></p>");
				return;
			}
			Sector		sector = new Sector(system.getSectorId());
		%>
		
		<div class="header">
			<span>
				<a href="map.jsp?id=<%= sector.getId() %>">Sector Map</a>
			</span>
			
			<span>Actions:</span>
			
			<span>
				<a href="action?action=terraform&amp;type=system&amp;id=<%= system.getId() %>">Terraform</a>
			</span>
			<span>
				<a href="action?action=destroy&amp;type=system&amp;id=<%= system.getId() %>">Destroy</a>
			</span>
			<span>
				<a href="action?action=delete&amp;type=system&amp;id=<%= system.getId() %>">Delete</a>
			</span>
		</div>
		
		<table>
			<tr>
				<th>Sector</th>
				<td><%= sector.getName() %></td>
			</tr>
			
			<tr>
				<th>Coordinates</th>
				<td><%= system.getX() %>, <%= system.getY() %></td>
			</tr>
			
			<tr>
				<th>Allegiance</th>
				<td><%=system.getAllegianceCode()%></td>
			</tr>
			
			<tr>
				<th>Status</th>
				<td><%= system.getStatus() %></td>
			</tr>
		</table>
		
		<%
			for (Star star : system.getStars()) {
				if (system.getStars().size() > 1) {
					// Don't bother showing the star name if only one star.
					out.println("<h2>"+star.getName()+"</h2>");
				}
				
				int		count = 0;
				for (Planet planet : system.getPlanets()) {
					if (planet.getParentId() != star.getId()) {
						// Should only show planets orbiting this star.
						continue;
					}
					count++;
					out.println("<h3>"+planet.getName()+"</h3>");
		%>
					<table class="planetdata">
						<tr>
							<th>Planet type</th>
							<td><%= planet.getType() %></td>
						</tr>
						
						<tr>
							<th>Distance</th>
							<td><%= planet.getDistance() %> Mkm</td>
						</tr>
						
						<tr>
							<th>Radius</th>
							<td><%= planet.getRadius() %> km</td>
						</tr>
						
						<tr>
							<th>Atmosphere</th>
							<td><%= planet.getAtmospherePressure() %> <%= planet.getAtmosphereType() %></td>
						</tr>
						
						<tr>
							<th>Temperature</th>
							<td><%= planet.getTemperature() %></td>
						</tr>
						
						<tr>
							<th>Life</th>
							<td><%= planet.getLifeLevel() %></td>
						</tr>
					</table>
		<%					
				}
				if (count == 0) {
					out.println("<p>This star has no planets.</p>");
				}
				
			}
		%>		

	</body>
</html>
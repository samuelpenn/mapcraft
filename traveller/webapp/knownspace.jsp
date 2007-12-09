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
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<title>Known Space</title>
				
	</head>
	
	<body>
		<h1>Known Space</h1>
		
		<p>
			Some simple facts about known space.
		</p>
		
		<%
			ObjectFactory				fac = new ObjectFactory();
			Hashtable<String,Integer>	table = fac.getStatistics();
		%>
		
		<table>
			<tr>
				<th>Number of sectors</th>
				<td><%= table.get("sectors") %></td>
			</tr>
			<tr>
				<th>Number of systems</th>
				<td><%= table.get("systems") %></td>
			</tr>
			<tr>
				<th>Number of planets</th>
				<td><%= table.get("planets") %> (incl. <%= table.get("moons") %> moons)</td>
			</tr>
			<tr>
				<th>Life bearing worlds</th>
				<td><%= table.get("life") %></td>
			</tr>
		</table>
		
		<h2>Sectors of Known Space</h2>
		
		<table>
			<%
				String		root = request.getSession().getServletContext().getRealPath("/");
				int			minX = table.get("minx");
				int			minY = table.get("miny");
				int			maxX = table.get("maxx");
				int			maxY = table.get("maxy");
				
				out.println("<tr style=\"margin: 0pt; padding: 0pt;\">\n");
				for (int x=minX; x <= maxX; x++) {
					out.println("<th style=\"margin: 0pt; padding: 0pt; border: 1pt solid black; width: 0pt;\">"+x+"</th>");
				}
				out.println("</tr>\n");
				
				for (int y=minY; y <= maxY; y++) {
					out.println("<tr style=\"margin: 0pt; padding: 0pt;\">");
					out.println("<th>"+y+"</th>");
					for (int x=minX; x <= maxX; x++) {
						Sector		sector = null;
						try {
							sector = new Sector(fac, x, y);
						} catch (ObjectNotFoundException e) {
							out.println("<td> </td>");
							continue;
						}
						String		name = sector.getName().toLowerCase();
						File		file = new File(root+"/images/sectors/"+sector.getId()+".jpg");
						
						if (!file.exists()) {
							ImageMap		map = new ImageMap(sector.getName(), root+"/images");
							map.drawMap(2).save(file);
						}
						out.print("<td style=\"margin: 0pt; padding: 0pt; border: 1pt solid black;\">");
						out.print("<a href=\"map.jsp?id="+sector.getId()+"\" title=\""+sector.getName()+"\">");
						out.print("<img src=\"images/sectors/"+sector.getId()+".jpg\"/></a>");
						out.println("</td>");
					}
					out.println("</tr>");
				}
				fac.close();
			%>
		</table>
	</body>
</html>
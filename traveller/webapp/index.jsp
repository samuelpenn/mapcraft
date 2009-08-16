<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<%@ page import="uk.org.glendale.rpg.traveller.Config" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<%@ page import="uk.org.glendale.rpg.traveller.sectors.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.systems.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.database.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.map.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%
	String		pageTitle = Config.getTitle();
	String		pageDescription = Config.getDescription();
	String		baseUrl = Config.getBaseUrl();
%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<title><%= pageTitle %></title>
		<link rel="stylesheet" type="text/css" href="<%= baseUrl %>/css/default.css" title="Default styles"/>
	</head>
	
	<body>
		<div class="header">
			<h1><%= pageTitle %></h1>
			<p><%= pageDescription %></p>
		</div>
		
		<jsp:include page="includes/links.jsp" flush="false"/>
		
		<div class="body">
			<p>
				The <%= pageTitle %> provides both graphical and API interfaces
				onto data detailing a science fiction campaign universise. The
				code is <a href="http://mapcraft.glendale.org.uk/worldgen">WorldGen</a>,
				and is available for download from SourceForge. This site, and the
				data that it contains, is currently in a status of development
				so may change or break without warning.
			</p>
			
			<img src="images/knownspace_map.jpg" width="288" height="240" alt="Map of Known Space"/>
			
			<p>
				See the <a href="knownspace.jsp">map of known space</a> for a graphical
				view of the worlds that can be explored here. <e>Known Space</e> is
				divided into 2D <em>Sectors</em>, each sector 32x40 parsecs across. Since
				it is based on <strong>Traveller</strong>, the third dimension is ignored.
				However, every world in each star system has been detailed and potentially
				mapped.
			</p>

			<%
				ObjectFactory				fac = new ObjectFactory();
				Hashtable<String,Long>		table = fac.getStatistics();
				
				String	trillions = String.format("%.1f", table.get("population") / 1000000000000.0);
			%>
			
			<table class="statistics">
				<tr>
					<th>Total number of sectors:</th>
					<td><%= table.get("sectors") %></td>
				</tr>
				<tr>
					<th>Total number of star systems:</th>
					<td><%= table.get("systems") %></td>
				</tr>
				<tr>
					<th>Total number of planets:</th>
					<td><%= table.get("planets") %> (incl. <%= table.get("moons") %> moons)</td>
				</tr>
				<tr>
					<th>Number of Earth-like worlds:</th>
					<td><%= table.get("life") %></td>
				</tr>
				<tr>
					<th>Total population (trillions):</th>
					<td><%= trillions  %></td>
				</tr>
			</table>
			
			<p>
				Specific information about <em>Known Space</em> can be obtained by using
				the <a href="api.jsp">API</a> provided. Alternatively, just browse the
				detailed maps.
			</p>
			
			<p>
				Any comments and questions should be directed at 
				<a href="mailto:sam@glendale.org.uk">Samuel Penn</a>.
			</p>
		</div>
	</body>
</html>

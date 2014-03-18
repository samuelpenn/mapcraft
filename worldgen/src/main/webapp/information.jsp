<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<%@ page import="uk.org.glendale.rpg.traveller.Config" %>
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
				This is a live demo of the <a href="http://mapcraft.glendale.org.uk/worldgen">WorldGen</a>
				universe software, which aims to recreate a <em>Traveller</em>-like map of a fictional
				galaxy suitable for science fiction roleplaying games. It's aims are four fold:
			</p>
			
			<ul>
				<li>
					<img src="images/systems.jpg" width="160" align="left"/>
					<h3>Star System Maps</h3>
			
					<p>
						Provide a dynamic, AJAX-enabled map of all the star systems that can be hosted on
						a web server. This enables users to easily view the universe and obtain information
						on the stars and planets within it in an easily understandable form. This means
						dropping the use of <em>Traveller</em> style Universal World Profile (UWP) hex
						codes and replacing it with graphics and text descriptions.
					</p>
				</li>
				
				<li>
					<img src="images/globe.jpg" width="160" align="left"/>
					<h3>Rich Planetary Data</h3>
					<p>
						Provide a rich resource of planetary data, including surface maps, descriptive
						information about the geology, ecology and culture as well as extensive amounts
						of raw data. This information is extended to include all the planets and moons
						of each star system, not just to describe a single populated world.
					</p>
				</li>
				
				<li>			
					<img src="images/igoogle.jpg" width="160" align="left"/>
					<h3>Queryable Interface</h3>
					<p>
						Provide an easy to use Web Services API which provides RSS feeds, iGoogle
						gadgets and open interfaces which can be used to search for and retrieve
						information on anything in the universe.
					</p>
				
					<p>
						The API is based around REST, which means that a simple web URL is all
						that is needed to obtain information on systems and planets. For example,
						<a href="http://dev.glendale.org.uk/traveller/system/14053.html">
							http://dev.glendale.org.uk/traveller/system/14053.html
						</a> will return information on the <em>Byzantine</em> star system.
					</p>
				</li>
				
				<li>
					<h3>Trade Simulation</h3>
					<p>
						The resource and trade information generated for each world will be
						used in a 'live' economy simulation, where every starship is
						modelled individually.
					</p>
				</li>
			</ul>
		</div>
	</body>
</html>
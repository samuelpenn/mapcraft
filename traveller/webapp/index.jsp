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
		
		<div class="links">
			<a href="knownspace.jsp"><img src="images/knownspace.png" width="64" height="64" title="Map of Known Space"/></a>
			<a href="glossary/"><img src="images/glossary.png" width="64" height="64" title="Glossary"/></a>
		</div>
		
		<div class="body">
			<p>
				This is a live demo of the <a href="http://mapcraft.glendale.org.uk/worldgen">WorldGen</a>
				universe software, which aims to recreate a <em>Traveller</em>-like map of a fictional
				galaxy suitable for science fiction roleplaying games. It's aims are three fold:
			</p>
			
			<h2>Star System Maps</h2>
			
			<p>
				<img src="images/systems.jpg" width="160" align="left"/>
				Provide a dynamic, AJAX-enabled map of all the star systems that can be hosted on
				a web server. This enables users to easily view the universe and obtain information
				on the stars and planets within it in an easily understandable form. This means
				dropping the use of <em>Traveller</em> style Universal World Profile (UWP) hex
				codes and replacing it with graphics and text descriptions.
			</p>
			
			<h2>Rich Planetary Data</h2>
			
			<p>
				<img src="images/globe.jpg" width="160" align="left"/>
				Provide a rich resource of planetary data, including surface maps, descriptive
				information about the geology, ecology and culture as well as extensive amounts
				of raw data. This information is extended to include all the planets and moons
				of each star system, not just to describe a single populated world.
			</p>
			
			<h2>Queryable Interface</h2>
			
			<p>
				<img src="images/igoogle.jpg" width="160" align="left"/>
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
			
			<h2>Trade Simulation</h2>
			
			<p>
			</p>
		
				
			<h2>REST Interface</h2>
			
			<p>
				The map pages that make up the map, and the information it contains,
				are built on top of a web services layer which can be accessed via
				a REST interface. Currently, this is just read-only. The interface
				is quite simple, and best described with the following examples.
			</p>
			
			<p>
				<a href="http://dev.glendale.org.uk/traveller/system/14053.html">
					http://dev.glendale.org.uk/traveller/system/14053.html
				</a><br/>
				This gets information on system number 14053, returned in HTML format.
				To get the data as XML, request a .xml or .txt file instead (both
				return XML, the difference being the content type it defines).
			</p>
			
			<p>
				<a href="http://dev.glendale.org.uk/traveller/sector/103.html">
					http://dev.glendale.org.uk/traveller/sector/103.html
				</a><br/>
				Get information on sector number 103.
			</p>
			
			<p>
				<a href="http://dev.glendale.org.uk/traveller/sector/Verge Sector.html">
					http://dev.glendale.org.uk/traveller/sector/Verge Sector.html
				</a><br/>
				Get information on Verge Sector sector. This is the same as the
				above example, but references the sector by name. Note that systems
				and planets can also be referenced directly by name.
			</p>
			
			<p>
				Data returned as XML may be rendered by a stylesheet, depending
				on your browser, so fetch the Text version instead if you really
				want to see the raw XML.
			</p>
			
			<h2>Planet maps</h2>
			
			<p>
				Work is being done on generating a surface map for each individual
				planet and moon. Lots of code has been written, but data hasn't
				yet been generated on a large scale, so most worlds will be blank.
			</p>
	
			<p>
				<a href="http://www.glendale.org.uk/traveller/data/system/10373.html">
					http://www.glendale.org.uk/traveller/data/system/10373.html
				</a><br/>
				This system shows one set of planet maps. To reference individual
				maps, request planet data in jpg format, as so:
				<a href="http://dev.glendale.org.uk/traveller/planet/171977.jpg">
					http://dev.glendale.org.uk/traveller/planet/171977.jpg
				</a><br/>
				Or with the <tt>globe</tt> parameter for the globe image:<br/>
				<a href="http://dev.glendale.org.uk/traveller/planet/171977.jpg?globe">
					http://dev.glendale.org.uk/traveller/planet/171977.jpg?globe
				</a>
			</p>
		</div>
	</body>
</html>
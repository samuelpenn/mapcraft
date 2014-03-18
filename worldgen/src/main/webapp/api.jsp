<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<%@ page import="uk.org.glendale.rpg.traveller.Config" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%
    String      pageTitle = Config.getTitle();
	String		baseUrl = Config.getBaseUrl();
%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<title>WorldGen API</title>
		<link rel="stylesheet" type="text/css" href="<%= baseUrl %>/css/default.css" title="Default styles"/>
	</head>
	
	<body>
		<div class="header">
			<h1>WorldGen API</h1>
			<p>
			    <i>WorldGen</i> provides a REST API which gives access to the maps,
				data and resources which make up the <%= pageTitle %>.
			</p>
		</div>

		<jsp:include page="includes/links.jsp" flush="false"/>
				
		<div class="body">
			<p>
				As well as the graphical maps, a REST-style web service API
				is provided to enable read access to information in the
				database. REST is a very simple way of making calls to the
				web server, since you can simply type in the URL and get back
				the results as a nicely formatted HTML page. At its simplest,
				a call to REST is just an URL such as:
			</p>
			
			<p>
				<a href="<%= baseUrl %>system/Byzantine.html">
					<%= baseUrl %>system/Byzantine.html
				</a>
			</p>
			
			<p>
				This obtains information on the star system called
				<em>Byzantine</em>, returning it in HTML format. We could
				request the information as XML just by using .xml instead
				of .html, or reference the star system by its unique ID
				or its coordinates within the sector. Generally, the API
				follows the following guidelines:
			</p>

			<ul>
			    <li>
			    	Parameters are encoded into the URL path, rather than
					added as extra GET parameters. Parameters are only used to
					modify the results in some way.
				</li>
				<li>
					The format data is returned in is govered by the URL
					extension that is used, normally .html, .xml, .txt or .jpg.
					Text format is XML but with a text content type.
				</li>
				<li>
					Most items can be requested by name, ID or coordinates.
				</li>
			</ul>

			<h2>The REST Interface</h2>
			
			<p>
				The map pages that make up the map, and the information it contains,
				are built on top of a web services layer which can be accessed via
				a REST interface. Currently, this is just read-only. The interface
				is quite simple, and best described with the following examples.
			</p>
						
			<h3>Sectors</h3>
			
			<p>
				The <strong>Sector</strong> is the largest unit of information,
				and spans an area of space 32 parsecs wide by 40 parsecs tall.
				It contains 16 sub-sectors and potentially many star systems.
				The /sector API provides access to sector information, and
				also system information by coordinate.
			</p>
			
			<h4>Listing all sectors</h4>
			
			<p>
				<a href="<%= baseUrl %>sector/">
					<%= baseUrl %>sector/
				</a><br/>
				This will provide a list of all sectors currently known about.
				To obtain this list in XML format, reference /index.xml
				directly.
			</p>
			
			<h4>Listing a single sector</h4>
			
			<p>
				<a href="<%= baseUrl %>sector/1.html">
					<%= baseUrl %>sector/1.html
				</a><br/>
				This will provide information on a single sector.
			</p>
			
			<h2>Older Documentation</h2>
			
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

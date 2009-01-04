<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<title>Imperium Mapping Project</title>
	</head>
	
	<body>
		<h1>Star System Data</h1>
		
		<p>
			These pages contain some attempts at mapping the Imperium, which is part
			of the <strong>Traveller</strong> campaign setting. There are plenty of
			other attempts out there, though none, as far as I can tell, add to the
			detail of data available.
		</p>
		
		<p>
			Currently, the following services are available.
		</p>
		
		<ul>
			<li><a href="knownspace.jsp">Map of Known Space</a>.</li>
			<li><a href="glossary/">Glossary of terms</a>.</li>
		</ul>
		
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
	</body>
</html>
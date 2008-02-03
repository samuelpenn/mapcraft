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
			<b>http://dev.glendale.org.uk/traveller/system/5.html</b><br/>
			This gets information on system number 5, returned in HTML format.
			To get the data as XML, request a .xml or .txt file instead (both
			return XML, the difference being the content type it defines).
		</p>
		
		<p>
			<b>http://dev.glendale.org.uk/traveller/sector/1.html</b><br/>
			Get information on sector number 1.
		</p>
		
		<p>
			<b>http://dev.glendale.org.uk/traveller/sector/The Reft.html</b><br/>
			Get information on The Reft sector.
		</p>
		
		<p>
			Data returned as XML may be rendered by a stylesheet, depending
			on your browser, so fetch the Text version instead if you really
			want to see the raw XML.
		</p>
	</body>
</html>
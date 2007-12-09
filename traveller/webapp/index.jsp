<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<title>Imperium Mapping Project</title>
	</head>
	
	<body>
		<h1>Traveller Data</h1>
		
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
		
		<h2>API Interface</h2>
		
		<p>
			An API interface is available to access data on the universe:
		</p>
		
		<p>
			<b>http://www.glendale.org.uk/traveller/data/get?type=TYPE&amp;id=ID</b>
		</p>
		
		<p>
			Where TYPE can be one of <b>universe</b>, <b>sector</b> or <b>system</b>. The ID is the unique
			id of the sector or system. Alternatively, instead of id, you can use name or
			x and y (for sectors only). Core sector is treated as 0,0.
		</p>
		
		<p>
			Finally, you can assign a format of either <b>text</b> or <b>xml</b>. e.g, to
			get details on system <i>Harmony</i> in XML format, use:
		</p>
		
		<p>
			<a href="http://www.glendale.org.uk/traveller/data/get?type=system&name=Harmony&format=xml">
				http://www.glendale.org.uk/traveller/data/get?type=system&amp;name=Harmony&amp;format=xml
			</a>
		</p>
		
		<p>
			Note that a stylesheet may be applied to any XML returned, depending on the
			browser you are using.
		</p>
	</body>
</html>
<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<%@ page import="java.util.*" %>

<%@ page import="uk.org.glendale.rpg.traveller.Config" %>
<%@ page import="uk.org.glendale.rpg.traveller.sectors.*" %>
<%@ page import="uk.org.glendale.rpg.traveller.systems.*" %>

<%
	String		baseUrl = Config.getBaseUrl();
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
		<title>Commodities</title>
		<link rel="stylesheet" type="text/css" href="<%= baseUrl %>/css/default.css" title="Default styles"/>
		
		<script type="text/javascript" src="<%= baseUrl %>/scripts/prototype.js"></script>
		<script type="text/javascript" src="<%= baseUrl %>/scripts/commodities.js"></script>
	</head>
	
	<body onload="loaded()">
		<div class="header">
			<h1>Commodities Administration</h1>
			<p>View, edit, add and delete commodities available in the universe.</p>
		</div>
		
		<jsp:include page="/includes/links.jsp" flush="false"/>
		
		<div class="body">
			<table id="commodities">
				<tr>
					<th align="left" style="width:10em">Id</th>
					<th align="left">Icon</th>
					<th align="left">Name</th>
					<th align="left">Cost</th>
					<th align="left">Production</th>
					<th align="left">Consumption</th>
					<th align="left">Legality</th>
					<th align="left">TL</th>
					<th align="left">Codes</th>
				</tr>
			</table>
		</div>
	</body>
</html>
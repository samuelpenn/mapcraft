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
	</head>
	
	<body>
		<div class="header">
			<h1>Commodities Administration</h1>
			<p>View, edit, add and delete commodities available in the universe.</p>
		</div>
		
		<jsp:include page="includes/links.jsp" flush="false"/>
		
		<div class="body">
			<table id="commodities">
				<tr id="c0">
					<th>Id</th>
					<th>Icon</th>
					<th>Name</th>
				</tr>
			</table>
		</div>
	</body>
</html>
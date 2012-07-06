<%@page contentType="text/html;charset=UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page pageEncoding="UTF-8"%>
<%@ page session="false" %>
<html>
	<head>
		<title>WorldGen</title>
	</head>
	<body>

		<div class="container">
			<h1>Sectors</h1>

			<ul>
				<c:forEach var="sector" items="${sectors}">
					<li>
						<c:out value="${sector.name}"/>
						(${sector.x},${sector.y})
					</li>
				</c:forEach>
			</ul>
			
			<p>
			 System: ${id}, ${name}, ${stars}
			</p>
			
			<h4>New Sector</h4>
			
			<table>
				<tr>
					<th></th>
					<c:set var="endX" value="${maxX - minX + 1}"/>
					<c:forEach var="xx" begin="0" end="5">
					    <c:set var="x" value="${minX + 1}"/>   
						<th>${x}</th>	
					</c:forEach>
				</tr>
				
				<c:forEach var="yy" begin="0" end="${maxY+1}" step="1">
				    <c:set var="y" value="${maxY - yy}"/>
					<tr>
						<th>${y}</th>
					</tr>
				</c:forEach>
			</table>			
			
			<form>
				Name: <input id="name"/><br/>
				X: <input id="x"/><br/>
				Y: <input id="y"/><br/>
				Allegiance: <input id="allegiance"/><br/>
				Codes: <input id="codes"/><br/>
			</form>
		</div>
	</body>
</html>
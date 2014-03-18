<?xml version="1.0"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:yb="http://yagsbook.sourceforge.net/xml/"
               xmlns:t="http://yagsbook.sourceforge.net/xml/traveller"
               version="1.0">

	<xsl:template match="/t:sector">
		<html>
			<head>
				<title><xsl:value-of select="@name"/></title>
			</head>
			
			<body>
				<h1><xsl:value-of select="@name"/></h1>
				
				<h2>Subsectors</h2>
				
				<table>
					<tr>
						<th>Subsector</th>
						<th>X</th>
						<th>Y</th>
					</tr>
					
					<xsl:apply-templates select="t:subsector"/>
				</table>
				
				<h2>Systems</h2>
				
				<table>
					<tr>
						<th>System</th>
						<th>X</th>
						<th>Y</th>
					</tr>
					
					<xsl:apply-templates select="t:system"/>
				</table>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="t:subsector">
		<tr>
			<td><xsl:value-of select="@name"/></td>
			<td><xsl:value-of select="@x"/></td>
			<td><xsl:value-of select="@y"/></td>
		</tr>
	</xsl:template>
	
	<xsl:template match="t:system">
		<tr>
			<td>
				<xsl:variable name="id" select="@id"/>
				
				<a href="get?type=system&amp;id={$id}&amp;format=xml">
					<xsl:value-of select="@name"/>
				</a>
			</td>
			<td><xsl:value-of select="@x"/></td>
			<td><xsl:value-of select="@y"/></td>
		</tr>
	</xsl:template>
               
</xsl:transform>
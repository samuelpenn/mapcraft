<?xml version="1.0"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:yb="http://yagsbook.sourceforge.net/xml/"
               xmlns:t="http://yagsbook.sourceforge.net/xml/traveller"
               version="1.0">

	<xsl:template match="/t:system">
		<html>
			<head>
				<title>System <xsl:value-of select="@name"/></title>
				<link rel="STYLESHEET" type="text/css" media="screen" href="css/system.css" />
			</head>
			
			<body>
				<h1>System <xsl:value-of select="@name"/></h1>
	
				<table>
					<tr>
						<th>Sector</th>
						<td>
							<a href="get?type=sector&amp;id={@sector}&amp;format=xml">
								<xsl:value-of select="@sector"/>
							</a>
						</td>
					</tr>
					
					<tr>
						<th>X-Coordinate</th>
						<td><xsl:value-of select="@x"/></td>
					</tr>
					
					<tr>
						<th>Y-Coordinate</th>
						<td><xsl:value-of select="@y"/></td>
					</tr>
					
					<tr>
						<th>Stars</th>
						<td><xsl:value-of select="count(t:star)"/></td>
					</tr>
					
					<tr>
						<th>Planets</th>
						<td><xsl:value-of select="count(t:planet)"/></td>
					</tr>
					
					<tr>
						<th>Allegience</th>
						<td><xsl:value-of select="t:allegiance"/></td>
					</tr>
				</table>
							
				<xsl:apply-templates select="t:star"/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="t:star">
		<h2>
			<xsl:value-of select="@name"/> 
			(<xsl:value-of select="t:type"/> 
			<xsl:text> </xsl:text>
			<xsl:value-of select="t:class"/>)
		</h2>
		
		<p>
			<xsl:if test="t:parent">
				<xsl:variable name="parent" select="t:parent"/>
				<xsl:value-of select="format-number(t:distance, '###,###,###,###,###,###')"/> Mkm
				(<xsl:value-of select="../t:star[@id=$parent]/@name"/>)
			</xsl:if>			
		</p>
		
		<xsl:variable name="id" select="@id"/>
		<xsl:apply-templates select="../t:planet[t:parent=$id]"/>
	</xsl:template>
	
	<xsl:template match="t:planet">
		<div class="planet">
			<h3><xsl:value-of select="@name"/></h3>

			<table class="layout">
				<tr>
					<td style="align-text: left; vertical-align: top; width: 50%">
						<xsl:apply-templates select="." mode="physical"/>
					</td>		
			
					<xsl:if test="t:population">
						<td style="align-text: right; vertical-align: top">
							<xsl:apply-templates select="." mode="civilisation"/>
						</td>
					</xsl:if>
				</tr>
			</table>
		</div>
	</xsl:template>
	
	<xsl:template match="t:planet" mode="physical">
		<table class="physical">
			<tr>
				<th>Distance</th>
				<td><xsl:value-of select="format-number(t:distance, '###,###,###,###,###')"/> Mkm</td>
			</tr>
			
			<tr>
				<th>Type</th>
				<td><a href="glossary/pcl-{t:type}"><xsl:value-of select="t:type"/></a></td>
			</tr>
			
			<tr>
				<th>Radius</th>
				<td><xsl:value-of select="format-number(t:radius, '###,###,###')"/>km</td>
			</tr>
			
			<tr>
				<th>Gravity</th>
				<td><xsl:value-of select="format-number(t:gravity, '#0.###')"/> m/s/s</td>
			</tr>
				
			<tr>
				<th>Day length</th>
				<td><xsl:value-of select="t:day"/></td>
			</tr>

			<tr>
				<th>Temperature</th>
				<td><xsl:value-of select="t:temperature"/></td>
			</tr>
			
			<xsl:if test="not(t:pressure = 'None')">
				<tr>
					<th>Atmosphere</th>
					<td>
						<xsl:value-of select="t:pressure"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="t:atmosphere"/>
					</td>
				</tr>
			</xsl:if>
			
			<tr>
				<th>Hydrographics</th>
				<td><xsl:value-of select="t:hydrographics"/>%</td>
			</tr>
			
			<xsl:if test="not(t:life = 'None')">
				<tr>
					<th>Life level</th>
					<td><xsl:value-of select="t:life"/></td>
				</tr>
			</xsl:if>
		</table>
	</xsl:template>
	
	<xsl:template match="t:planet" mode="civilisation">
		<table class="civilisation">
			<tr>
				<th>Population</th>
				<td><xsl:value-of select="format-number(t:population, '###,###,###,###,###')"/></td>
			</tr>
			
			<tr>
				<th>Starport</th>
				<td><xsl:value-of select="t:starport"/></td>
			</tr>
			
			<tr>
				<th>Government</th>
				<td><xsl:value-of select="t:government"/></td>
			</tr>
			
			<tr>
				<th>Tech level</th>
				<td>
					<a href="glossary/tech-{t:tech}"><xsl:value-of select="t:tech"/></a>
					<xsl:choose>
						<xsl:when test="t:tech='0'"> (stone age)</xsl:when>
						<xsl:when test="t:tech='1'"> (bronze age)</xsl:when>
						<xsl:when test="t:tech='2'"> (iron age)</xsl:when>
						<xsl:when test="t:tech='3'"> (medieval)</xsl:when>
						<xsl:when test="t:tech='4'"> (age of sail)</xsl:when>
						<xsl:when test="t:tech='5'"> (industrial revolution)</xsl:when>
						<xsl:when test="t:tech='6'"> (mechanised)</xsl:when>
						<xsl:when test="t:tech='7'"> (nuclear)</xsl:when>
						<xsl:when test="t:tech='8'"> (digital)</xsl:when>
						<xsl:when test="t:tech='9'"> (interplanetary)</xsl:when>
						<xsl:when test="t:tech='10'"> (interstellar)</xsl:when>
						<xsl:when test="t:tech='11'"> (low imperial)</xsl:when>
						<xsl:when test="t:tech='12'"> (standard imperial)</xsl:when>
						<xsl:when test="t:tech='13'"> (advanced)</xsl:when>
						<xsl:when test="t:tech='14'"> (highly advanced)</xsl:when>
						<xsl:otherwise> (magic)</xsl:otherwise>
					</xsl:choose>
				</td>
			</tr>
			
			<tr>
				<th>Law level</th>
				<td>
					<a href="glossary/law-{t:law}"><xsl:value-of select="t:law"/></a>
					<xsl:choose>
						<xsl:when test="t:law='0'"> (anarchy)</xsl:when>
						<xsl:when test="t:law='1'"> (very free)</xsl:when>
						<xsl:when test="t:law='2'"> (free)</xsl:when>
						<xsl:when test="t:law='3'"> (moderated)</xsl:when>
						<xsl:when test="t:law='4'"> (controlled)</xsl:when>
						<xsl:when test="t:law='5'"> (repressive)</xsl:when>
						<xsl:when test="t:law='6'"> (total control)</xsl:when>
					</xsl:choose>
				</td>
			</tr>
			
			<xsl:if test="t:base">
				<tr>
					<th>Base</th>
					<td>
						<xsl:choose>
							<xsl:when test="t:base='N'">Naval Base</xsl:when>
							<xsl:when test="t:base='S'">Scout Base</xsl:when>
							<xsl:otherwise><xsl:value-of select="t:base"/></xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
			</xsl:if>
			
			<xsl:for-each select="t:trade">
				<tr>
					<th>Trade Codes</th>
					<td>
						<a href="glossary/trade-{@code}"><xsl:value-of select="."/></a>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	               
</xsl:transform>
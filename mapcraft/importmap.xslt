<?xml version="1.0"?>
<!--Namespaces are global if you set them in the stylesheet element-->
<xsl:transform
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:my-ext="uk.co.demon.bifrost.rpg.encyclopedia.Extensions"
    extension-element-prefixes="my-ext">

    <xalan:component prefix="ext1">
        <xalan:script lang="javaclass"
                      src="xalan://uk.co.demon.bifrost.rpg.encyclopedia.Extensions"/>
    </xalan:component>

    <xsl:template match="//import-map">
        <my-ext:importMap href="/home/sam/rpg/habisfern/encyclopedia/src/maps/euressa.map"
                          scale="1" style="full">
            <crop x="14" y="26" width="12" height="8"/>
            <site name="Some site"/>
        </my-ext:importMap>
    </xsl:template>


</xsl:transform>

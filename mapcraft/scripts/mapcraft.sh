#!/bin/bash
#
# Run script for Mapcraft
# Sets up CLASSPATH and calls the main class in the jar file.
#
# Version: $Revision$

XALAN_HOME=/usr/share/java/xalan
MAPCRAFT_JAR=mapcraft.jar
export XALAN_HOME MAPCRAFT_JAR

CLASSPATH=$XALAN_HOME/xalan.jar:$XALAN_HOME/xercesImpl.jar:$MAPCRAFT_JAR
export CLASSPATH

java -Xmx1024m net.sourceforge.mapcraft.MapCraft $*


#!/bin/bash
#
# Run script for Mapcraft
# Sets up CLASSPATH and calls the main class in the jar file.
#
# Version: $Revision$

MAPCRAFT_JAR=mapcraft.jar
export MAPCRAFT_JAR

CLASSPATH=$MAPCRAFT_JAR
export CLASSPATH

java -Xmx1024m net.sourceforge.mapcraft.MapCraft $*


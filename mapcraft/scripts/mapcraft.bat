rem Startup script for MS Windows

rem Tested briefly and seems to work
rem Remember to set the XALAN_HOME variable before running


rem Version: $Revision$
rem Date:    $Date$

rem Author:  Samuel Penn


set MAPCRAFT_JAR=mapcraft.jar
set CLASSPATH=%MAPCRAFT_JAR%

java -Xmx1024m net.sourceforge.mapcraft.MapCraft %*


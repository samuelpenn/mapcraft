rem Startup script for MS Windows

rem Tested briefly and seems to work
rem Remember to set the XALAN_HOME variable before running


rem Version: $Revision$
rem Date:    $Date$

rem Author:  Samuel Penn


set XALAN_HOME=C:\share\libs

set MAPCRAFT_JAR=mapcraft.jar


set CLASSPATH=%XALAN_HOME%\xalan.jar;%XALAN_HOME%\xercesImpl.jar;%MAPCRAFT_JAR%



java -Xmx1024m uk.co.demon.bifrost.rpg.mapcraft.MapCraft %*


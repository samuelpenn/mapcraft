rem Startup script for MS Windows
rem Totally untested since I don't have an MS Windows box to test it on.

rem Version: $Revision$
rem Date:    $Date$
rem Author:  Samuel Penn

set XALAN_HOME=C:\share\xalan
set MAPCRAFT_JAR=mapcraft.jar

set CLASSPATH=%XALAN_HOME%\xalan.jar;%XALAN_HOME%\xercesImpl.jar;%MAPCRAFT_JAR%

java -Xmx1024m uk.co.demon.bifrost.rpg.mapcraft.MapCraft $*


@echo off
REM Verify if JORAM_HOME is well defined
if not exist "%JORAM_HOME%\samples\bin\clean.bat" goto nokHome
REM Verify if JAVA_HOME is well defined
if not exist "%JAVA_HOME%\bin\java.exe" goto nokJava

set CONFIG_DIR=%JORAM_HOME%\samples\config
set JORAM_BIN=%JORAM_HOME%\ship\bin
set RUN_DIR=%JORAM_HOME%\samples\run
set SERVER_RUN_DIR=%RUN_DIR%\server0

REM  Building the Classpath
set CLASSPATH=%JORAM_BIN%\felix.jar

mkdir %RUN_DIR%
mkdir %SERVER_RUN_DIR%
copy %CONFIG_DIR%\a3config.dtd %SERVER_RUN_DIR%\a3config.dtd
copy %CONFIG_DIR%\a3debug.cfg %SERVER_RUN_DIR%\a3debug.cfg
copy %CONFIG_DIR%\centralized_a3servers.xml %SERVER_RUN_DIR%\a3servers.xml
copy %CONFIG_DIR%\config.properties %SERVER_RUN_DIR%\config.properties

set PATH=%JAVA_HOME%\bin;%PATH%

echo == Launching a non persistent server#0 ==
start /D %SERVER_RUN_DIR% java -Dfelix.config.properties=file:config.properties -Dfr.dyade.aaa.agent.AgentServer.id=0 -Dcom.sun.management.jmxremote -classpath %CLASSPATH% org.apache.felix.main.Main
goto end
:nokHome
echo The JORAM_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:nokJava
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:end

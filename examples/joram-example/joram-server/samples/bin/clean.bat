@echo off
REM Verify if JORAM_HOME is well defined
if not exist "%JORAM_HOME%\samples\bin\clean.bat" goto nokHome

set RUN_DIR=%JORAM_HOME%\samples\run

echo == Cleaning the persistence directories and configuration settings ==
rmdir /s /q %RUN_DIR%
goto end
:nokHome
echo The JORAM_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:end

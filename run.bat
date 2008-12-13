@echo off
set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_11
rem echo %PATH%
rem echo %JAVA_HOME%
rem call ant run
call java -jar "ophelia.jar" ophelia.main.Main
pause
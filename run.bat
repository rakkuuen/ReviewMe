@echo off
setlocal

:: Define paths
set BIN_PATH=bin
set LIB_PATH=lib

:: Add the sqlite-jdbc.jar file to the classpath
set CLASSPATH=%LIB_PATH%\sqlite-jdbc-3.45.2.0.jar;%LIB_PATH%\slf4j-api-2.0.16.jar;%LIB_PATH%\slf4j-simple-2.0.16.jar

:: Run the program
java -cp %BIN_PATH%;%CLASSPATH% Controller.Main

:: Print success message
echo Program executed successfully.

endlocal
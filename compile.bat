@echo off
setlocal enabledelayedexpansion

:: Define paths
set SRC_PATH=src
set BIN_PATH=bin
set LIB_PATH=lib

:: Add the sqlite-jdbc.jar file to the classpath
set CLASSPATH=%LIB_PATH%\sqlite-jdbc-3.45.2.0.jar;%LIB_PATH%\slf4j-api-2.0.16.jar;%LIB_PATH%\slf4j-simple-2.0.16.jar

:: Recursively find all .java files in the src directory and store in a file, one
:: quoted path per line. Paths use forward slashes (javac accepts them fine on
:: Windows) since javac's @sources.txt parsing treats backslash as an escape
:: character and would otherwise mangle every path.
if exist sources.txt del sources.txt
for /R %SRC_PATH% %%f in (*.java) do (
    set "p=%%f"
    set "p=!p:\=/!"
    echo "!p!" >> sources.txt
)

:: Compile all .java files
javac -d %BIN_PATH% -cp "lib/*" @sources.txt

:: Clean up temporary files
del sources.txt

:: Indicate success
echo Compilation completed.

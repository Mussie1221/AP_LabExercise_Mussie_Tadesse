@echo off
rem =====================================================
rem Simple script to compile and run the PokerGame project
rem -----------------------------------------------------
rem Assumes you are in the project root that contains the src folder
rem -----------------------------------------------------
setlocal

rem ----- Clean previous build folder (optional) -----
if exist out rmdir /s /q out

rem ----- Compile all Java source files -----
javac -d out src/*.java
if %errorlevel% neq 0 (
    echo Compilation failed. Check the error messages above.
    exit /b %errorlevel%
)

rem ----- Run the application -----
java -cp out PokerGame

endlocal

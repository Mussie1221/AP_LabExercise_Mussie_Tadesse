REM Compile all Java source files into the out directory
mkdir out
javac -d out src/*.java

REM Run the PokerGame GUI
java -cp out PokerGame
pause

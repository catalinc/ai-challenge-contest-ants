@echo off
rem clean
del *.log
del *.class
del ..\tools\MyBot.jar
rem compile
javac MyBot.java
rem package
jar cvfm MyBot.jar Manifest.txt *.class 
move MyBot.jar ..\tools
rem clean
del *.class

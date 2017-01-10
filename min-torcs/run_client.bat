@echo off
type header.txt > output.csv
java -jar TorcsClientFcl-1.0-jar-with-dependencies.jar coyote.fcl gearPreferences:gears.csv >> output.csv
timeout 1 >nul
@echo on
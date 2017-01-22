@echo off
type header.txt > output.csv
java -jar TorcsClientGamepad.jar coyote.fcl gearPreferences:gears.csv >> output.csv
timeout 1 >nul
@echo on
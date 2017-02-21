@echo off
type header.txt > output_pad.csv
java -jar TorcsClientGamepad.jar coyote.fcl verbose:on gearPreferences:gears.csv >> output_pad.csv
timeout 1 >nul
@echo on
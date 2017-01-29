@echo off
type header.txt > output_fcl.csv
java -jar TorcsClientFcl.jar coyote.fcl gearPreferences:gears.csv >> output_fcl.csv
timeout 1 >nul
@echo on
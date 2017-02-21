@echo off
type header.txt > output_fcl2.csv
java -jar TorcsClientFcl.jar coyote2.fcl gearPreferences:gears.csv >> output_fcl2.csv
timeout 1 >nul
@echo on
@echo off
type header.txt > output_fcl_forza.csv
java -jar TorcsClientFcl.jar ForzaDriver.fcl gearPreferences:gears.csv >> output_fcl_forza.csv
timeout 1 >nul
@echo on
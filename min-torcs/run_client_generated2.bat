@echo off
type header.txt > output_generated2.csv
java -jar normalizedTorcs.jar generated2.fcl gearPreferences:gears.csv >> output_generated2.csv
timeout 1 >nul
@echo on
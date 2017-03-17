@echo off
type header.txt > output_generated.csv
java -jar normalizedTorcs.jar generated.fcl gearPreferences:gears.csv >> output_generated.csv
timeout 1 >nul
@echo on
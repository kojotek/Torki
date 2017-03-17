@echo off
type header.txt > output_genetics.csv
java -jar genetorcs.jar coyote.fcl gearPreferences:gears.csv >> output_genetics.csv
timeout 1 >nul
@echo on
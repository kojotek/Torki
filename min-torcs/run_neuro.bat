@echo off
type header.txt > output_fcl.csv
java -jar neuro.jar coyote.fcl gearPreferences:gears.csv >> output_fcl_neuro.csv
timeout 1 >nul
@echo on
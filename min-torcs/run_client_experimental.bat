@echo off
type header.txt > output_expermiental.csv
java -jar TorcsClientFcl.jar experimental.fcl gearPreferences:gears.csv >> output_expermiental.csv
timeout 1 >nul
@echo on
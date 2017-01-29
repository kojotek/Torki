@echo off
type header.txt > output_optimized.csv
java -jar TorcsClientFcl.jar fcl_optimized/fcl_optimized.fcl gearPreferences:gears.csv >> output_optimized.csv
timeout 1 >nul
@echo on
@echo off
type header.txt > output_optimized_2.csv
java -jar TorcsClientFcl.jar fcl_optimized_2/fcl_optimized_2.fcl gearPreferences:gears.csv >> output_optimized_2.csv
timeout 1 >nul
@echo on
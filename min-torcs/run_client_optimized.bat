@echo off
java -jar TorcsClientFcl-1.0-jar-with-dependencies.jar fcl_optimized/fcl_optimized.fcl gearPreferences:gears.csv
timeout 1 >nul
@echo on
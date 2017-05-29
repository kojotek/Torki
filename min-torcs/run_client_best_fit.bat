@echo off
type header.txt > output_fcl_best_fit.csv
java -jar TorcsClientFcl.jar %1 gearPreferences:gears.csv >> output_fcl_best_fit.csv
timeout 1 >nul
@echo on
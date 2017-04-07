@echo off
type header.txt > output_fcl_best_fit.csv
java -jar TorcsClientFcl.jar fcl\robot_best_fit.fcl gearPreferences:gears.csv >> output_fcl_best_fit.csv
timeout 1 >nul
@echo on
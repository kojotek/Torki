@echo off
java -jar optimization2.jar to_optimize.fcl gearPreferences:gears.csv
timeout 1 >nul
@echo on
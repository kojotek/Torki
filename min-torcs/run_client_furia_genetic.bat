@echo off
java -jar FuriaGenetic.jar furia_steer.fcl furia_accel.fcl furia_gears.fcl verbose:on gearPreferences:gears.csv racingLine:%1 trackLength:%2
timeout 1 >nul
@echo on
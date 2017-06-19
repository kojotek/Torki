@echo off
java -jar TorcsClientGamepad.jar furia_steer.fcl furia_accel.fcl furia_gears.fcl verbose:on gearPreferences:gears.csv racingLine:eroad_racing_line2.csv > output_pad.csv
timeout 1 >nul
@echo on
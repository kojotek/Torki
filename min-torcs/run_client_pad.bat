@echo off
java -jar TorcsClientGamepad.jar coyote.fcl verbose:on gearPreferences:gears.csv racingLine:eroad_racing_line.csv > output_pad.csv
timeout 1 >nul
@echo on
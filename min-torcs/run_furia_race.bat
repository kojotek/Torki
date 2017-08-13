@echo off
java -jar furiarace.jar %1 visual:on gearPreferences:gears.csv racingLine:%2 trackLength:%3 > nul
timeout 1 >nul
@echo on
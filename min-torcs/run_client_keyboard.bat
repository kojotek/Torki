@echo off
java -jar TorcsClientKeyboard.jar coyote.fcl verbose:on gearPreferences:gears.csv >> output_keyboard.csv
timeout 1 >nul
@echo on
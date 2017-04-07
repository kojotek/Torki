@echo off
if "%1"=="" goto :blank
if "%2"=="" goto :blank
cd torcs
START /B wtorcs.exe -r ../%1 -t 100000 -nofuel -nodamage > ../%1.out
cd ..
timeout 1 >nul
type %1.out
java -jar genetorcs.jar %2 gearPreferences:gears.csv >nul
timeout 1 >nul
type %1.out 
goto :done

:blank
echo blad
:done
@echo on
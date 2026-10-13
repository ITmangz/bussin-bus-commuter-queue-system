@echo off
setlocal
cd /d "%~dp0"
set "DEMO_JAVA=java"
if exist "C:\Program Files\Java\bin\java.exe" set "DEMO_JAVA=C:\Program Files\Java\bin\java.exe"
if not exist "target\classes\StudentIdCameraDemo.class" (
  echo Build the sample first using the instructions in README.md.
  pause
  exit /b 1
)
"%DEMO_JAVA%" --add-opens java.base/java.lang=ALL-UNNAMED -cp "target\classes;target\lib\*" StudentIdCameraDemo %*
if errorlevel 1 pause

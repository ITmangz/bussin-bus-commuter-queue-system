@echo off
setlocal
cd /d "%~dp0"
set "DEMO_PYTHON=%USERPROFILE%\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"
if not exist "%DEMO_PYTHON%" set "DEMO_PYTHON=python"
"%DEMO_PYTHON%" camera_app.py %*
if errorlevel 1 (
  echo.
  echo Could not run the sample. See README.md for Python setup instructions.
  pause
)

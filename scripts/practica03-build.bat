@echo off
setlocal
cd /d "%~dp0.."
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0practica03-build.ps1"
if errorlevel 1 (
  echo.
  echo La construccion no termino correctamente. Revise el mensaje anterior.
  exit /b 1
)
echo.
echo Practica 03 ejecutada correctamente.
endlocal

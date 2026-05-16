@echo off
REM ============================================================
REM  SurakshaSetu — Run Script
REM  Run compile.bat first!
REM ============================================================

set PROJECT_DIR=%~dp0
set OUT_DIR=%PROJECT_DIR%out
set LIB_DIR=%PROJECT_DIR%lib
set JAR=%LIB_DIR%\*
set MAIN_CLASS=com.suraksha.setu.SurakshasetuApp

if not exist "%OUT_DIR%" (
    echo [ERROR] Output directory not found. Run compile.bat first.
    pause
    exit /b 1
)

echo Starting SurakshaSetu...
java -cp "%OUT_DIR%;%JAR%" %MAIN_CLASS%

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Application crashed. Check console output.
    pause
)

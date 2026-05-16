@echo off
REM ============================================================
REM  SurakshaSetu — Compile Script
REM  Requires: JDK 11+, mysql-connector-j-9.0.0.jar in lib/
REM ============================================================

set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src
set OUT_DIR=%PROJECT_DIR%out
set LIB_DIR=%PROJECT_DIR%lib
set JAR=%LIB_DIR%\*

echo [1/3] Creating output directory...
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo [2/3] Finding source files...
dir /s /b "%SRC_DIR%\*.java" > "%PROJECT_DIR%sources.txt"

echo [3/3] Compiling...
javac -encoding UTF-8 -cp "%JAR%" -d "%OUT_DIR%" @"%PROJECT_DIR%sources.txt"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Build successful! Run run.bat to launch.
) else (
    echo.
    echo [ERROR] Build failed. Check error messages above.
    echo        Make sure JDK 11+ is installed and on PATH.
    echo        Make sure lib\mysql-connector-j-9.0.0.jar exists.
)

del "%PROJECT_DIR%sources.txt" 2>nul
pause

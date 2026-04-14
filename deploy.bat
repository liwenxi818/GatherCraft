@echo off
cd /d "%~dp0"

set JAR=build\libs\gathercraft-1.0.0.jar

if not exist "%JAR%" (
    echo [ERROR] JAR not found: %JAR%
    echo Run: gradlew build
    pause
    exit /b 1
)

if "%1"=="" (
    echo Usage: deploy.bat [mods_folder_path]
    echo Example: deploy.bat C:\server\mods
    pause
    exit /b 1
)

set DEST=%~1

if not exist "%DEST%" (
    echo [ERROR] Destination not found: %DEST%
    pause
    exit /b 1
)

echo Copying gathercraft-1.0.0.jar to %DEST%...
copy /y "%JAR%" "%DEST%\gathercraft-1.0.0.jar" >nul

echo Done.
pause
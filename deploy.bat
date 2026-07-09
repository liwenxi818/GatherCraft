@echo off
cd /d "%~dp0"

set JAR=
for %%f in (build\libs\gathercraft-*.jar) do set JAR=%%f

if "%JAR%"=="" (
    echo [ERROR] JAR not found in build\libs
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

echo Copying %JAR% to %DEST%...
copy /y "%JAR%" "%DEST%\" >nul

echo Done.
pause
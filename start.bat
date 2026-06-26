@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title DataCleanPro - Auto Setup

set "JAVA_MIN_VERSION=17"
set "MAVEN_VERSION=3.9.6"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/!MAVEN_VERSION!/binaries/apache-maven-!MAVEN_VERSION!-bin.zip"
set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-!MAVEN_VERSION!-bin\3311e1d4\apache-maven-!MAVEN_VERSION!"
set "VERSION=1.0.0"

if /i "%~1"=="--help" goto show_help
if /i "%~1"=="--stop" goto stop_services
if /i "%~1"=="--uninstall" goto uninstall
if /i "%~1"=="--version" (echo v%VERSION% & exit /b 0)

echo.
echo  ========================================================
echo     DataCleanPro - Auto Setup and Launch v%VERSION%
echo  ========================================================
echo.

echo [0/6] Checking network...
set "NETWORK_OK=0"
ping -n 1 -w 1000 baidu.com >nul 2>&1 && set "NETWORK_OK=1"
if "%NETWORK_OK%"=="1" (echo [OK] Network connected) else (echo [!] Offline mode)
echo.

echo [1/6] Checking Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%g
    set JAVA_VER=!JAVA_VER:"=!
    for /f "tokens=1 delims=." %%v in ("!JAVA_VER!") do set JAVA_MAJOR=%%v
    if !JAVA_MAJOR! geq %JAVA_MIN_VERSION% (
        echo [OK] Java !JAVA_VER!
        goto check_maven
    )
)

if "%NETWORK_OK%"=="0" (echo [X] Need JDK 17+ & pause & exit /b 1)
echo [!] Installing JDK 17...
where winget >nul 2>&1 && winget install EclipseAdoptium.Temurin.17.JDK --silent --accept-package-agreements --accept-source-agreements
if %errorlevel% equ 0 (echo [OK] Java installed & goto check_maven)
echo [X] Install Java manually: https://adoptium.net/
pause
exit /b 1

:check_maven
echo.
echo [2/6] Checking Maven...
set "MVN=!MAVEN_DIR!\bin\mvn.cmd"
if exist "!MVN!" (echo [OK] Local Maven found & goto check_mysql)
where mvn >nul 2>&1 && (set "MVN=mvn" & echo [OK] System Maven found & goto check_mysql)

if "%NETWORK_OK%"=="0" (echo [X] Need Maven & pause & exit /b 1)
echo [!] Downloading Maven...
if not exist "!MAVEN_DIR!" mkdir "!MAVEN_DIR!" 2>nul
powershell -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -Uri '!MAVEN_URL!' -OutFile '%TEMP%\maven.zip'"
if not exist "%TEMP%\maven.zip" (echo [X] Download failed & pause & exit /b 1)
powershell -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\apache-maven-!MAVEN_VERSION!-bin\3311e1d4' -Force"
del "%TEMP%\maven.zip" >nul 2>&1
if exist "!MVN!" (echo [OK] Maven installed) else (echo [X] Install failed & pause & exit /b 1)

:check_mysql
echo.
echo [3/6] Checking MySQL...
set "MYSQL_FOUND=0"
sc query MySQL80 >nul 2>&1 && set "MYSQL_FOUND=1"
sc query MySQL >nul 2>&1 && set "MYSQL_FOUND=1"
if "%MYSQL_FOUND%"=="0" (echo [!] MySQL not found - skipping & goto start_app)
echo [OK] MySQL found

:config_db
echo.
echo [4/6] Testing database connection...

set "DB_PASS="
for /f "tokens=2 delims==" %%a in ('findstr /i "db.password=" src\main\resources\application.properties 2^>nul') do set "DB_PASS=%%a"

:: Try only the configured password
mysql -u root -p!DB_PASS! -e "SELECT 1" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Database connected
    mysql -u root -p!DB_PASS! -e "CREATE DATABASE IF NOT EXISTS datacleanpro" >nul 2>&1
    echo [OK] Database ready
    mysql -u root -p!DB_PASS! datacleanpro < "src\main\resources\db\schema.sql" >nul 2>&1
    echo [OK] Schema ready
) else (
    echo [!] Database connection failed - skipping
    echo     Update db.password in application.properties
)

:start_app
echo.
echo [5/6] Building...

if not exist "src\main\java\com\datacleanpro\App.java" (echo [X] Project not found & pause & exit /b 1)

if not exist "target\classes\com\datacleanpro\App.class" (
    call "!MVN!" compile -q
    if !errorlevel! neq 0 (echo [X] Compile failed & pause & exit /b 1)
    echo [OK] Build successful
) else (
    echo [OK] Already compiled
)

taskkill /f /im java.exe >nul 2>&1

echo.
echo [6/6] Starting DataCleanPro...
echo ========================================
echo.

call "!MVN!" exec:java -Dexec.mainClass="com.datacleanpro.App"

echo.
echo [OK] Application closed
pause
exit /b 0

:show_help
echo Usage: start.bat [--stop] [--uninstall] [--version] [--help]
exit /b 0

:stop_services
taskkill /f /im java.exe >nul 2>&1
echo [OK] Stopped
pause
exit /b 0

:uninstall
if exist "!MAVEN_DIR!" rmdir /s /q "!MAVEN_DIR!" >nul 2>&1
if exist "target" rmdir /s /q "target" >nul 2>&1
echo [OK] Done
pause
exit /b 0

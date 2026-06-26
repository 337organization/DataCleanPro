@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title DataCleanPro - Auto Setup

echo.
echo  ========================================
echo    DataCleanPro - Auto Setup and Launch
echo  ========================================
echo.

:: =============================================
:: Step 1: Check/Install Java
:: =============================================
echo [1/5] Checking Java...
echo --------------------------------------------

java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Java is installed
    goto :check_maven
)

echo [!] Java not found. Installing JDK 17...
echo.

:: Try winget
where winget >nul 2>&1
if %errorlevel% equ 0 (
    echo Using winget to install Java...
    winget install EclipseAdoptium.Temurin.17.JDK --silent --accept-package-agreements --accept-source-agreements
    if !errorlevel! equ 0 (
        echo [OK] Java installed successfully
        goto :check_maven
    )
)

echo [X] Cannot install Java automatically.
echo.
echo Please install Java manually:
echo 1. Visit: https://adoptium.net/
echo 2. Download Temurin JDK 17
echo 3. Run installer, check "Add to PATH"
echo 4. Restart this script
echo.
start https://adoptium.net/
pause
exit /b 1

:check_maven
echo.

:: =============================================
:: Step 2: Check/Install Maven
:: =============================================
echo [2/5] Checking Maven...
echo --------------------------------------------

:: Check local Maven
set "MVN=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6\bin\mvn.cmd"
if exist "%MVN%" (
    echo [OK] Local Maven found
    goto :check_mysql
)

:: Check system Maven
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set "MVN=mvn"
    echo [OK] System Maven found
    goto :check_mysql
)

:: Download Maven
echo [!] Maven not found. Downloading...
echo.

set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6"
if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%" 2>nul

echo Downloading Maven 3.9.6...
powershell -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\maven.zip'"

if not exist "%TEMP%\maven.zip" (
    echo [X] Download failed!
    echo.
    echo Please download Maven manually:
    echo 1. Visit: https://maven.apache.org/download.cgi
    echo 2. Download apache-maven-3.9.6-bin.zip
    echo 3. Extract to: %MAVEN_DIR%
    echo.
    pause
    exit /b 1
)

echo Extracting Maven...
powershell -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4' -Force"

if exist "%MVN%" (
    echo [OK] Maven installed successfully
    del "%TEMP%\maven.zip" >nul 2>&1
) else (
    echo [X] Maven installation failed
    pause
    exit /b 1
)

:check_mysql
echo.

:: =============================================
:: Step 3: Check MySQL
:: =============================================
echo [3/5] Checking MySQL...
echo --------------------------------------------

set "MYSQL_FOUND=0"
sc query MySQL80 >nul 2>&1 && set "MYSQL_FOUND=1"
sc query MySQL >nul 2>&1 && set "MYSQL_FOUND=1"

if "!MYSQL_FOUND!"=="1" (
    echo [OK] MySQL service found
    goto :config_db
)

:: Try winget
where winget >nul 2>&1
if %errorlevel% equ 0 (
    echo [!] MySQL not found. Installing via winget...
    winget install Oracle.MySQL --silent --accept-package-agreements --accept-source-agreements
    if !errorlevel! equ 0 (
        echo.
        echo [OK] MySQL installed!
        echo.
        echo IMPORTANT: Please restart your computer, then run this script again.
        echo.
        pause
        exit /b 0
    )
)

echo [!] MySQL not found.
echo.
echo The app can run without MySQL (limited functionality).
echo.
echo To install MySQL manually:
echo Visit: https://dev.mysql.com/downloads/installer/
echo.
set /p CONTINUE="Continue without MySQL? (Y/N): "
if /i not "!CONTINUE!"=="Y" (
    start https://dev.mysql.com/downloads/installer/
    pause
    exit /b 0
)
goto :start_app

:config_db
echo.

:: =============================================
:: Step 4: Configure Database
:: =============================================
echo [4/5] Configuring database...
echo --------------------------------------------

:: Read current password from config
set "DB_PASS="
for /f "tokens=2 delims==" %%a in ('findstr /i "db.password" src\main\resources\application.properties 2^>nul') do (
    set "DB_PASS=%%a"
)

:: Try current password first
if defined DB_PASS (
    mysql -u root -p!DB_PASS! -e "USE datacleanpro" >nul 2>&1
    if !errorlevel! equ 0 (
        echo [OK] Database 'datacleanpro' exists
        goto :start_app
    )
)

:: Try common passwords to find the right one
set "FOUND_PASS="
for %%p in ("!DB_PASS!" "" "root" "123456" "password" "admin" "mysql") do (
    if not defined FOUND_PASS (
        mysql -u root -p%%~p -e "SELECT 1" >nul 2>&1
        if !errorlevel! equ 0 (
            set "FOUND_PASS=%%~p"
            echo [OK] MySQL connection successful
        )
    )
)

if not defined FOUND_PASS (
    echo [!] Cannot connect to MySQL.
    echo.
    echo Please update the database password in:
    echo src\main\resources\application.properties
    echo.
    echo Set: db.password=YOUR_MYSQL_PASSWORD
    echo.
    set /p CONTINUE="Continue anyway? (Y/N): "
    if /i not "!CONTINUE!"=="Y" exit /b 1
    goto :start_app
)

:: Create database if not exists
mysql -u root -p!FOUND_PASS! -e "CREATE DATABASE IF NOT EXISTS datacleanpro" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] Database 'datacleanpro' ready
)

:: Update password in config
powershell -ExecutionPolicy Bypass -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=!FOUND_PASS!' | Set-Content 'src\main\resources\application.properties'"
echo [OK] Database password configured

:: Import schema
mysql -u root -p!FOUND_PASS! datacleanpro < "src\main\resources\db\schema.sql" >nul 2>&1
if !errorlevel! equ 0 (
    echo [OK] Database schema imported
) else (
    echo [!] Schema import skipped (may already exist)
)

:start_app
echo.

:: =============================================
:: Step 5: Build and Launch
:: =============================================
echo [5/5] Building and launching...
echo --------------------------------------------

if not exist "src\main\java\com\datacleanpro\App.java" (
    echo [X] Error: Project files not found!
    echo Please run this script from the DataCleanPro directory.
    pause
    exit /b 1
)

echo Compiling project...
call "%MVN%" compile -q
if %errorlevel% neq 0 (
    echo.
    echo [X] Compilation failed!
    echo Please check Java version (requires JDK 17+)
    pause
    exit /b 1
)
echo [OK] Build successful

echo.
echo  ========================================
echo    Starting DataCleanPro...
echo  ========================================
echo.

call "%MVN%" exec:java -Dexec.mainClass="com.datacleanpro.App"

echo.
echo Application closed.
pause

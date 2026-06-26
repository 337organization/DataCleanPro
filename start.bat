@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title DataCleanPro - Auto Setup

:: =============================================
:: Configuration
:: =============================================
set "JAVA_MIN_VERSION=17"
set "MAVEN_VERSION=3.9.6"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin\3311e1d4\apache-maven-%MAVEN_VERSION%"
set "LOG_FILE=startup.log"
set "MAX_RETRIES=3"

:: Initialize log
echo [%date% %time%] DataCleanPro Startup Script > "%LOG_FILE%"

:: =============================================
:: Header
:: =============================================
echo.
echo  ========================================
echo    DataCleanPro - Auto Setup and Launch
echo  ========================================
echo.

:: =============================================
:: Check Administrator Rights
:: =============================================
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Running without administrator privileges
    echo [%date% %time%] Running as standard user >> "%LOG_FILE%"
) else (
    echo [INFO] Running with administrator privileges
    echo [%date% %time%] Running as administrator >> "%LOG_FILE%"
)
echo.

:: =============================================
:: Check Network Connection
:: =============================================
echo [0/5] Checking network...
echo --------------------------------------------

ping -n 1 google.com >nul 2>&1
if %errorlevel% neq 0 (
    ping -n 1 baidu.com >nul 2>&1
    if !errorlevel! neq 0 (
        echo [!] No network connection detected
        echo     Some features may not work properly
        echo [%date% %time%] Network check failed >> "%LOG_FILE%"
    ) else (
        echo [OK] Network connected
    )
) else (
    echo [OK] Network connected
)

echo.

:: =============================================
:: Step 1: Check/Install Java
:: =============================================
echo [1/5] Checking Java...
echo --------------------------------------------

:: Check if Java exists
java -version >nul 2>&1
if %errorlevel% equ 0 (
    :: Get Java version
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VER=%%g
    )
    set JAVA_VER=!JAVA_VER:"=!
    
    :: Extract major version
    for /f "tokens=1 delims=." %%v in ("!JAVA_VER!") do (
        set JAVA_MAJOR=%%v
    )
    
    if !JAVA_MAJOR! geq %JAVA_MIN_VERSION% (
        echo [OK] Java !JAVA_VER! installed
        echo [%date% %time%] Java !JAVA_VER! found >> "%LOG_FILE%"
        goto :check_maven
    ) else (
        echo [!] Java !JAVA_VER! found but version %JAVA_MIN_VERSION%+ required
        echo [%date% %time%] Java version too old: !JAVA_VER! >> "%LOG_FILE%"
    )
)

echo [!] Installing JDK %JAVA_MIN_VERSION%...
echo.

:: Try winget
where winget >nul 2>&1
if %errorlevel% equ 0 (
    echo Using winget to install Java...
    winget install EclipseAdoptium.Temurin.17.JDK --silent --accept-package-agreements --accept-source-agreements
    if !errorlevel! equ 0 (
        echo [OK] Java installed successfully
        echo [%date% %time%] Java installed via winget >> "%LOG_FILE%"
        
        :: Refresh PATH for current session
        for /f "tokens=2*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do (
            set "PATH=%%b"
        )
        goto :check_maven
    )
)

echo [X] Cannot install Java automatically
echo     Please visit https://adoptium.net/ to install JDK %JAVA_MIN_VERSION%
echo [%date% %time%] Java installation failed >> "%LOG_FILE%"
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
set "MVN=%MAVEN_DIR%\bin\mvn.cmd"
if exist "%MVN%" (
    echo [OK] Local Maven found
    echo [%date% %time%] Local Maven found >> "%LOG_FILE%"
    goto :check_mysql
)

:: Check system Maven
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set "MVN=mvn"
    echo [OK] System Maven found
    echo [%date% %time%] System Maven found >> "%LOG_FILE%"
    goto :check_mysql
)

:: Download Maven
echo [!] Maven not found. Downloading...
echo.

if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%" 2>nul

:: Download with retry
set "RETRY=0"
:download_maven
set /a RETRY+=1
echo Downloading Maven %MAVEN_VERSION% (attempt !RETRY!/%MAX_RETRIES%)...
echo [%date% %time%] Maven download attempt !RETRY! >> "%LOG_FILE%"

powershell -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%TEMP%\maven.zip' -ErrorAction Stop } catch { exit 1 }"

if not exist "%TEMP%\maven.zip" (
    if !RETRY! lss %MAX_RETRIES% (
        echo Download failed, retrying...
        timeout /t 2 /nobreak >nul
        goto :download_maven
    )
    echo [X] Maven download failed after %MAX_RETRIES% attempts
    echo     Please download manually from https://maven.apache.org/download.cgi
    echo [%date% %time%] Maven download failed >> "%LOG_FILE%"
    pause
    exit /b 1
)

echo Extracting Maven...
powershell -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin\3311e1d4' -Force"

if exist "%MVN%" (
    echo [OK] Maven installed successfully
    echo [%date% %time%] Maven installed successfully >> "%LOG_FILE%"
    del "%TEMP%\maven.zip" >nul 2>&1
) else (
    echo [X] Maven installation failed
    echo [%date% %time%] Maven extraction failed >> "%LOG_FILE%"
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
where mysql >nul 2>&1 && set "MYSQL_FOUND=1"

if "!MYSQL_FOUND!"=="0" (
    echo [!] MySQL not found - skipping database setup
    echo     (Database features will be unavailable)
    echo [%date% %time%] MySQL not found, skipping >> "%LOG_FILE%"
    goto :start_app
)

echo [OK] MySQL found
echo [%date% %time%] MySQL found >> "%LOG_FILE%"

:config_db
echo.

:: =============================================
:: Step 4: Configure Database
:: =============================================
echo [4/5] Configuring database...
echo --------------------------------------------

:: Read current password
set "DB_PASS="
for /f "tokens=2 delims==" %%a in ('findstr /i "db.password=" src\main\resources\application.properties 2^>nul') do (
    set "DB_PASS=%%a"
)

:: Try connection
set "FOUND_PASS="
for %%p in ("!DB_PASS!" "" "root" "123456" "password" "admin") do (
    if not defined FOUND_PASS (
        mysql -u root -p%%~p -e "SELECT 1" >nul 2>&1
        if !errorlevel! equ 0 (
            set "FOUND_PASS=%%~p"
        )
    )
)

if not defined FOUND_PASS (
    echo [!] Cannot connect to MySQL - skipping database setup
    echo     (Update db.password in application.properties to enable)
    echo [%date% %time%] MySQL connection failed >> "%LOG_FILE%"
    goto :start_app
)

echo [OK] MySQL connected
echo [%date% %time%] MySQL connected successfully >> "%LOG_FILE%"

:: Create database
mysql -u root -p!FOUND_PASS! -e "CREATE DATABASE IF NOT EXISTS datacleanpro" >nul 2>&1
echo [OK] Database 'datacleanpro' ready

:: Update config
powershell -ExecutionPolicy Bypass -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=!FOUND_PASS!' | Set-Content 'src\main\resources\application.properties'"
echo [OK] Database password configured

:: Import schema (suppress duplicate key errors)
mysql -u root -p!FOUND_PASS! datacleanpro < "src\main\resources\db\schema.sql" >nul 2>&1
echo [OK] Database schema ready

:start_app
echo.

:: =============================================
:: Step 5: Build and Launch
:: =============================================
echo [5/5] Building and launching...
echo --------------------------------------------

if not exist "src\main\java\com\datacleanpro\App.java" (
    echo [X] Error: Project files not found!
    echo     Please run this script from the DataCleanPro directory.
    echo [%date% %time%] Project files not found >> "%LOG_FILE%"
    pause
    exit /b 1
)

:: Check if already compiled
set "NEED_COMPILE=0"
if not exist "target\classes\com\datacleanpro\App.class" set "NEED_COMPILE=1"

:: Check if source is newer than class
for %%f in (src\main\java\com\datacleanpro\App.java) do (
    if exist "target\classes\com\datacleanpro\App.class" (
        for %%c in (target\classes\com\datacleanpro\App.class) do (
            if %%~tf gtr %%~tc set "NEED_COMPILE=1"
        )
    )
)

if "!NEED_COMPILE!"=="1" (
    echo Compiling project...
    call "%MVN%" compile -q
    if !errorlevel! neq 0 (
        echo [X] Compilation failed!
        echo     Please check Java version (requires JDK %JAVA_MIN_VERSION%+)
        echo [%date% %time%] Compilation failed >> "%LOG_FILE%"
        pause
        exit /b 1
    )
    echo [OK] Build successful
    echo [%date% %time%] Compilation successful >> "%LOG_FILE%"
) else (
    echo [OK] Already compiled, skipping
)

echo.
echo  ========================================
echo    Starting DataCleanPro...
echo  ========================================
echo.
echo [%date% %time%] Application started >> "%LOG_FILE%"

call "%MVN%" exec:java -Dexec.mainClass="com.datacleanpro.App"

echo.
echo [%date% %time%] Application closed >> "%LOG_FILE%"
echo Application closed.
echo Log saved to: %LOG_FILE%
pause

@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title DataCleanPro - Auto Setup

echo.
echo  ========================================
echo    DataCleanPro - Auto Setup & Launch
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

echo [!] Java not found. Downloading JDK 17...
echo.

:: Download JDK using winget (Windows Package Manager)
where winget >nul 2>&1
if %errorlevel% equ 0 (
    echo Using winget to install Java...
    winget install EclipseAdoptium.Temurin.17.JDK --silent --accept-package-agreements --accept-source-agreements
    if %errorlevel% equ 0 (
        echo [OK] Java installed successfully
        :: Refresh PATH
        set "PATH=%PATH%;%ProgramFiles%\Eclipse Adoptium\jdk-17*\bin"
        goto :check_maven
    )
)

:: Fallback: download manually
echo winget not available. Opening download page...
echo.
echo Please install Java manually:
echo 1. Download JDK 17 from: https://adoptium.net/
echo 2. Run installer, check "Add to PATH"
echo 3. Restart this script after installation
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

:: Download Maven automatically
echo [!] Maven not found. Downloading...
echo.

set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6"
if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%" 2>nul

echo Downloading Maven 3.9.6...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\maven.zip'"

if not exist "%TEMP%\maven.zip" (
    echo [X] Download failed. Please install Maven manually.
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Extracting Maven...
powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4' -Force"

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
:: Step 3: Check/Install MySQL
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

:: Try to install MySQL via winget
where winget >nul 2>&1
if %errorlevel% equ 0 (
    echo [!] MySQL not found. Installing via winget...
    winget install Oracle.MySQL --silent --accept-package-agreements --accept-source-agreements
    if %errorlevel% equ 0 (
        echo [OK] MySQL installed. Please restart your computer.
        echo After restart, run this script again.
        pause
        exit /b 0
    )
)

echo [!] MySQL not found.
echo.
echo The app can run without MySQL (limited functionality).
echo To install MySQL manually: https://dev.mysql.com/downloads/installer/
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

:: Check if database exists
mysql -u root -e "USE datacleanpro" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Database 'datacleanpro' exists
    goto :start_app
)

:: Try to create database with common passwords
set "DB_CREATED=0"
for %%p in ("" "root" "123456" "password" "admin") do (
    if "!DB_CREATED!"=="0" (
        mysql -u root -p%%p -e "CREATE DATABASE IF NOT EXISTS datacleanpro" >nul 2>&1
        if !errorlevel! equ 0 (
            echo [OK] Database created with password: %%p
            
            :: Update application.properties
            if "%%p"=="" (
                powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=' | Set-Content 'src\main\resources\application.properties'"
            ) else (
                powershell -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=%%p' | Set-Content 'src\main\resources\application.properties'"
            )
            
            :: Import schema
            mysql -u root -p%%p datacleanpro < "src\main\resources\db\schema.sql" >nul 2>&1
            echo [OK] Database schema imported
            set "DB_CREATED=1"
        )
    )
)

if "!DB_CREATED!"=="0" (
    echo [!] Could not auto-configure database.
    echo.
    echo Please manually:
    echo 1. Open MySQL command line
    echo 2. Run: CREATE DATABASE datacleanpro;
    echo 3. Run: USE datacleanpro;
    echo 4. Run: SOURCE src/main/resources/db/schema.sql;
    echo 5. Update db.password in src/main/resources/application.properties
    echo.
    set /p CONTINUE="Continue anyway? (Y/N): "
    if /i not "!CONTINUE!"=="Y" exit /b 1
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
call "%MVN%" compile -q 2>nul
if %errorlevel% neq 0 (
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


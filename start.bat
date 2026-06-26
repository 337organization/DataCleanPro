@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title DataCleanPro - Auto Setup

:: =============================================
:: ANSI Color Codes
:: =============================================
set "ESC="
set "RED=%ESC%[91m"
set "GREEN=%ESC%[92m"
set "YELLOW=%ESC%[93m"
set "BLUE=%ESC%[94m"
set "MAGENTA=%ESC%[95m"
set "CYAN=%ESC%[96m"
set "WHITE=%ESC%[97m"
set "RESET=%ESC%[0m"
set "BOLD=%ESC%[1m"

:: =============================================
:: Configuration
:: =============================================
set "JAVA_MIN_VERSION=17"
set "MAVEN_VERSION=3.9.6"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin\3311e1d4\apache-maven-%MAVEN_VERSION%"
set "LOG_FILE=startup.log"
set "MAX_RETRIES=3"
set "CONFIG_FILE=startup.cfg"
set "GITHUB_REPO=337organization/DataCleanPro"
set "VERSION=1.0.0"
set "VERBOSE=0"
set "DRY_RUN=0"

:: Parse command line arguments
:parse_args
if "%~1"=="" goto :args_done
if /i "%~1"=="--verbose" set "VERBOSE=1"
if /i "%~1"=="--dry-run" set "DRY_RUN=1"
if /i "%~1"=="--help" goto :show_help
if /i "%~1"=="-h" goto :show_help
if /i "%~1"=="--uninstall" goto :uninstall
if /i "%~1"=="--stop" goto :stop_services
if /i "%~1"=="--version" (
    echo DataCleanPro Startup Script v%VERSION%
    exit /b 0
)
shift
goto :parse_args
:args_done

:: Load config file if exists
if exist "%CONFIG_FILE%" (
    for /f "usebackq tokens=1,* delims==" %%a in ("%CONFIG_FILE%") do (
        set "%%a=%%b"
    )
    if "%VERBOSE%"=="1" echo [DEBUG] Loaded config from %CONFIG_FILE%
)

:: Initialize log
echo [%date% %time%] DataCleanPro Startup Script v%VERSION% > "%LOG_FILE%"
echo [%date% %time%] Arguments: verbose=%VERBOSE% dry-run=%DRY_RUN% >> "%LOG_FILE%"

:: =============================================
:: ASCII Art Header
:: =============================================
echo.
echo %CYAN%  ╔═══════════════════════════════════════════════════════════════╗%RESET%
echo %CYAN%  ║%RESET%  %BOLD%%WHITE%  ____        _       ____ _                            %RESET%%CYAN%  ║%RESET%
echo %CYAN%  ║%RESET%  %BOLD%%WHITE% ^|  _ \  __ _^(_^) ___ / ___^| ^| ___   _ _ __           %RESET%%CYAN%  ║%RESET%
echo %CYAN%  ║%RESET%  %BOLD%%WHITE% ^| ^| ^| ^|/ _` ^| ^|/ __^| ^|   ^| ^|/ _ \ ^| ^| '_ \          %RESET%%CYAN%  ║%RESET%
echo %CYAN%  ║%RESET%  %BOLD%%WHITE% ^| ^|_^| ^| (_^| ^| ^| (__^| ^|___^| ^| (_) ^|^| ^| ^| ^| ^|         %RESET%%CYAN%  ║%RESET%
echo %CYAN%  ║%RESET%  %BOLD%%WHITE% ^|____/ \__,_^|_^|\___^|\____^|_^|\___/ ^|_^|_^| ^|_^|         %RESET%%CYAN%  ║%RESET%
echo %CYAN%  ║%RESET%  %BOLD%%WHITE%                                                           %RESET%%CYAN%  ║%RESET%
echo %CYAN%  ║%RESET%      %YELLOW%Auto Setup and Launch Script v%VERSION%%RESET%                  %CYAN%║%RESET%
echo %CYAN%  ╚═══════════════════════════════════════════════════════════════╝%RESET%
echo.

:: =============================================
:: Check Prerequisites
:: =============================================

:: Check Administrator Rights
net session >nul 2>&1
if %errorlevel% equ 0 (
    set "IS_ADMIN=1"
    if "%VERBOSE%"=="1" echo %GREEN%[INFO]%RESET% Running with administrator privileges
) else (
    set "IS_ADMIN=0"
    if "%VERBOSE%"=="1" echo %YELLOW%[INFO]%RESET% Running without administrator privileges
)

:: Check Disk Space (need at least 500MB)
for /f "tokens=3" %%a in ('dir /-c "%USERPROFILE%" 2^>nul ^| findstr /i "bytes free"') do (
    set "FREE_SPACE=%%a"
)
if defined FREE_SPACE (
    if !FREE_SPACE! lss 524288000 (
        echo %RED%[WARNING]%RESET% Low disk space! Less than 500MB free
        echo [%date% %time%] Low disk space warning >> "%LOG_FILE%"
    )
)

echo.

:: =============================================
:: Step 0: Check Network
:: =============================================
echo %BOLD%%WHITE%[0/6] Checking network...%RESET%
echo %BLUE%--------------------------------------------%RESET%

set "NETWORK_OK=0"
ping -n 1 -w 1000 google.com >nul 2>&1 && set "NETWORK_OK=1"
ping -n 1 -w 1000 baidu.com >nul 2>&1 && set "NETWORK_OK=1"

if "%NETWORK_OK%"=="0" (
    echo %YELLOW%[!]%RESET% No network connection detected
    echo     Offline mode - skipping downloads
    echo [%date% %time%] Network check failed, offline mode >> "%LOG_FILE%"
) else (
    echo %GREEN%[OK]%RESET% Network connected
    echo [%date% %time%] Network connected >> "%LOG_FILE%"
)

echo.

:: =============================================
:: Step 1: Check/Install Java
:: =============================================
echo %BOLD%%WHITE%[1/6] Checking Java...%RESET%
echo %BLUE%--------------------------------------------%RESET%

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
        echo %GREEN%[OK]%RESET% Java !JAVA_VER! installed
        echo [%date% %time%] Java !JAVA_VER! found >> "%LOG_FILE%"
        goto :check_maven
    ) else (
        echo %YELLOW%[!]%RESET% Java !JAVA_VER! found but version %JAVA_MIN_VERSION%+ required
    )
)

if "%DRY_RUN%"=="1" (
    echo %YELLOW%[DRY-RUN]%RESET% Would install JDK %JAVA_MIN_VERSION%
    goto :check_maven
)

if "%NETWORK_OK%"=="0" (
    echo %RED%[X]%RESET% Cannot install Java without network
    echo     Please install JDK %JAVA_MIN_VERSION% manually: https://adoptium.net/
    pause
    exit /b 1
)

echo %YELLOW%[!]%RESET% Installing JDK %JAVA_MIN_VERSION%...

:: Try winget
where winget >nul 2>&1
if %errorlevel% equ 0 (
    echo Using winget to install Java...
    winget install EclipseAdoptium.Temurin.17.JDK --silent --accept-package-agreements --accept-source-agreements
    if !errorlevel! equ 0 (
        echo %GREEN%[OK]%RESET% Java installed successfully
        echo [%date% %time%] Java installed via winget >> "%LOG_FILE%"
        
        :: Refresh PATH
        for /f "tokens=2*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do (
            set "PATH=%%b"
        )
        goto :check_maven
    )
)

echo %RED%[X]%RESET% Cannot install Java automatically
echo     Please visit: https://adoptium.net/
echo [%date% %time%] Java installation failed >> "%LOG_FILE%"
start https://adoptium.net/
pause
exit /b 1

:check_maven
echo.

:: =============================================
:: Step 2: Check/Install Maven
:: =============================================
echo %BOLD%%WHITE%[2/6] Checking Maven...%RESET%
echo %BLUE%--------------------------------------------%RESET%

:: Check local Maven
set "MVN=%MAVEN_DIR%\bin\mvn.cmd"
if exist "%MVN%" (
    echo %GREEN%[OK]%RESET% Local Maven found
    echo [%date% %time%] Local Maven found >> "%LOG_FILE%"
    goto :check_mysql
)

:: Check system Maven
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set "MVN=mvn"
    echo %GREEN%[OK]%RESET% System Maven found
    echo [%date% %time%] System Maven found >> "%LOG_FILE%"
    goto :check_mysql
)

if "%DRY_RUN%"=="1" (
    echo %YELLOW%[DRY-RUN]%RESET% Would download Maven %MAVEN_VERSION%
    goto :check_mysql
)

if "%NETWORK_OK%"=="0" (
    echo %RED%[X]%RESET% Cannot download Maven without network
    echo     Please install Maven manually: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

:: Download Maven
echo %YELLOW%[!]%RESET% Maven not found. Downloading...

if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%" 2>nul

:: Download with retry
set "RETRY=0"
:download_maven
set /a RETRY+=1
echo Downloading Maven %MAVEN_VERSION% (attempt !RETRY!/%MAX_RETRIES%)...
echo [%date% %time%] Maven download attempt !RETRY! >> "%LOG_FILE%"

powershell -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; try { $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%TEMP%\maven.zip' -ErrorAction Stop; Write-Host 'Download complete' } catch { Write-Host $_.Exception.Message; exit 1 }"

if not exist "%TEMP%\maven.zip" (
    if !RETRY! lss %MAX_RETRIES% (
        echo %YELLOW%[!]%RESET% Download failed, retrying in 3 seconds...
        timeout /t 3 /nobreak >nul
        goto :download_maven
    )
    echo %RED%[X]%RESET% Maven download failed after %MAX_RETRIES% attempts
    echo [%date% %time%] Maven download failed >> "%LOG_FILE%"
    pause
    exit /b 1
)

echo Extracting Maven...
powershell -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin\3311e1d4' -Force"

if exist "%MVN%" (
    echo %GREEN%[OK]%RESET% Maven installed successfully
    echo [%date% %time%] Maven installed successfully >> "%LOG_FILE%"
    del "%TEMP%\maven.zip" >nul 2>&1
) else (
    echo %RED%[X]%RESET% Maven installation failed
    echo [%date% %time%] Maven extraction failed >> "%LOG_FILE%"
    pause
    exit /b 1
)

:check_mysql
echo.

:: =============================================
:: Step 3: Check MySQL
:: =============================================
echo %BOLD%%WHITE%[3/6] Checking MySQL...%RESET%
echo %BLUE%--------------------------------------------%RESET%

set "MYSQL_FOUND=0"
sc query MySQL80 >nul 2>&1 && set "MYSQL_FOUND=1"
sc query MySQL >nul 2>&1 && set "MYSQL_FOUND=1"
where mysql >nul 2>&1 && set "MYSQL_FOUND=1"

if "%MYSQL_FOUND%"=="0" (
    echo %YELLOW%[!]%RESET% MySQL not found - skipping database setup
    echo     Database features will be unavailable
    echo [%date% %time%] MySQL not found, skipping >> "%LOG_FILE%"
    goto :check_update
)

echo %GREEN%[OK]%RESET% MySQL found
echo [%date% %time%] MySQL found >> "%LOG_FILE%"

:config_db
echo.

:: =============================================
:: Step 4: Configure Database
:: =============================================
echo %BOLD%%WHITE%[4/6] Configuring database...%RESET%
echo %BLUE%--------------------------------------------%RESET%

if "%DRY_RUN%"=="1" (
    echo %YELLOW%[DRY-RUN]%RESET% Would configure database
    goto :check_update
)

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
    echo %YELLOW%[!]%RESET% Cannot connect to MySQL - skipping database setup
    echo     Update db.password in application.properties to enable
    echo [%date% %time%] MySQL connection failed >> "%LOG_FILE%"
    goto :check_update
)

echo %GREEN%[OK]%RESET% MySQL connected
echo [%date% %time%] MySQL connected successfully >> "%LOG_FILE%"

:: Create database
mysql -u root -p!FOUND_PASS! -e "CREATE DATABASE IF NOT EXISTS datacleanpro" >nul 2>&1
echo %GREEN%[OK]%RESET% Database 'datacleanpro' ready

:: Update config
powershell -ExecutionPolicy Bypass -Command "(Get-Content 'src\main\resources\application.properties') -replace 'db.password=.*', 'db.password=!FOUND_PASS!' | Set-Content 'src\main\resources\application.properties'"
echo %GREEN%[OK]%RESET% Database password configured

:: Import schema
mysql -u root -p!FOUND_PASS! datacleanpro < "src\main\resources\db\schema.sql" >nul 2>&1
echo %GREEN%[OK]%RESET% Database schema ready

:check_update
echo.

:: =============================================
:: Step 5: Check for Updates
:: =============================================
echo %BOLD%%WHITE%[5/6] Checking for updates...%RESET%
echo %BLUE%--------------------------------------------%RESET%

if "%NETWORK_OK%"=="0" (
    echo %YELLOW%[!]%RESET% Skip update check (no network)
    goto :start_app
)

:: Check GitHub for latest release
powershell -ExecutionPolicy Bypass -Command "try { $latest = (Invoke-RestMethod -Uri 'https://api.github.com/repos/%GITHUB_REPO%/releases/latest' -ErrorAction Stop).tag_name; Write-Host $latest } catch { Write-Host 'check_failed' }" > "%TEMP%\latest_ver.txt" 2>nul

set /p LATEST_VER=<"%TEMP%\latest_ver.txt"
del "%TEMP%\latest_ver.txt" >nul 2>&1

if "!LATEST_VER!"=="check_failed" (
    echo %YELLOW%[!]%RESET% Cannot check for updates
) else if "!LATEST_VER!"=="" (
    echo %GREEN%[OK]%RESET% Running latest version
) else (
    echo %GREEN%[OK]%RESET% Latest version: !LATEST_VER!
    echo     Run 'git pull' to update
)

echo [%date% %time%] Update check completed >> "%LOG_FILE%"

:start_app
echo.

:: =============================================
:: Step 6: Build and Launch
:: =============================================
echo %BOLD%%WHITE%[6/6] Building and launching...%RESET%
echo %BLUE%--------------------------------------------%RESET%

if not exist "src\main\java\com\datacleanpro\App.java" (
    echo %RED%[X]%RESET% Error: Project files not found!
    echo     Please run this script from the DataCleanPro directory.
    echo [%date% %time%] Project files not found >> "%LOG_FILE%"
    pause
    exit /b 1
)

if "%DRY_RUN%"=="1" (
    echo %YELLOW%[DRY-RUN]%RESET% Would compile and launch application
    goto :done
)

:: Check if already compiled (incremental build)
set "NEED_COMPILE=0"
if not exist "target\classes\com\datacleanpro\App.class" (
    set "NEED_COMPILE=1"
) else (
    :: Check if any source file is newer
    for /r "src\main\java" %%f in (*.java) do (
        for %%c in (target\classes\%%~nf.class) do (
            if %%~tf gtr %%~tc set "NEED_COMPILE=1"
        )
    )
)

if "!NEED_COMPILE!"=="1" (
    echo Compiling project...
    call "%MVN%" compile -q
    if !errorlevel! neq 0 (
        echo %RED%[X]%RESET% Compilation failed!
        echo     Please check Java version (requires JDK %JAVA_MIN_VERSION%+)
        echo [%date% %time%] Compilation failed >> "%LOG_FILE%"
        pause
        exit /b 1
    )
    echo %GREEN%[OK]%RESET% Build successful
    echo [%date% %time%] Compilation successful >> "%LOG_FILE%"
) else (
    echo %GREEN%[OK]%RESET% Already compiled, skipping
)

:: Stop any existing instance
tasklist /fi "imagename eq java.exe" 2>nul | findstr /i "java" >nul
if !errorlevel! equ 0 (
    echo %YELLOW%[!]%RESET% Stopping existing instance...
    taskkill /f /im java.exe >nul 2>&1
    timeout /t 1 /nobreak >nul
)

echo.
echo %CYAN%  ========================================%RESET%
echo %BOLD%%WHITE%    Starting DataCleanPro...%RESET%
echo %CYAN%  ========================================%RESET%
echo.
echo [%date% %time%] Application started >> "%LOG_FILE%"

call "%MVN%" exec:java -Dexec.mainClass="com.datacleanpro.App"

:done
echo.
echo [%date% %time%] Application closed >> "%LOG_FILE%"
echo %GREEN%[OK]%RESET% Application closed
echo %CYAN%[INFO]%RESET% Log saved to: %LOG_FILE%
pause
exit /b 0

:: =============================================
:: Show Help
:: =============================================
:show_help
echo.
echo %BOLD%%WHITE%DataCleanPro Startup Script v%VERSION%%RESET%
echo.
echo %CYAN%Usage:%RESET%
echo   start.bat [options]
echo.
echo %CYAN%Options:%RESET%
echo   --verbose    Show detailed output
echo   --dry-run    Check only, don't execute
echo   --stop       Stop running instance
echo   --uninstall  Remove installed dependencies
echo   --version    Show version
echo   --help, -h   Show this help
echo.
echo %CYAN%Examples:%RESET%
echo   start.bat              Normal startup
echo   start.bat --verbose    Startup with debug info
echo   start.bat --dry-run    Check environment only
echo   start.bat --stop       Stop the application
echo.
echo %CYAN%Configuration:%RESET%
echo   Create startup.cfg to override defaults:
echo   JAVA_MIN_VERSION=17
echo   MAVEN_VERSION=3.9.6
echo.
exit /b 0

:: =============================================
:: Stop Services
:: =============================================
:stop_services
echo.
echo %BOLD%%WHITE%Stopping DataCleanPro...%RESET%
echo %BLUE%--------------------------------------------%RESET%

:: Stop Java processes
tasklist /fi "imagename eq java.exe" 2>nul | findstr /i "java" >nul
if %errorlevel% equ 0 (
    echo %YELLOW%[!]%RESET% Stopping Java processes...
    taskkill /f /im java.exe >nul 2>&1
    echo %GREEN%[OK]%RESET% Stopped
) else (
    echo %GREEN%[OK]%RESET% No running instance found
)

:: Stop Maven processes
tasklist /fi "imagename eq mvn.cmd" 2>nul | findstr /i "mvn" >nul
if %errorlevel% equ 0 (
    taskkill /f /im mvn.cmd >nul 2>&1
)

echo.
pause
exit /b 0

:: =============================================
:: Uninstall
:: =============================================
:uninstall
echo.
echo %BOLD%%WHITE%Uninstalling DataCleanPro dependencies...%RESET%
echo %BLUE%--------------------------------------------%RESET%
echo.
echo %YELLOW%[WARNING]%RESET% This will remove:
echo   - Local Maven installation
echo   - Compiled classes (target/)
echo   - Log files
echo.
set /p CONFIRM="Are you sure? (Y/N): "
if /i not "!CONFIRM!"=="Y" (
    echo Cancelled.
    exit /b 0
)

:: Remove Maven
if exist "%MAVEN_DIR%" (
    echo Removing Maven...
    rmdir /s /q "%MAVEN_DIR%" >nul 2>&1
    echo %GREEN%[OK]%RESET% Maven removed
)

:: Remove compiled files
if exist "target" (
    echo Removing compiled files...
    rmdir /s /q "target" >nul 2>&1
    echo %GREEN%[OK]%RESET% Compiled files removed
)

:: Remove logs
if exist "startup.log" del "startup.log" >nul 2>&1
if exist "storage\logs" rmdir /s /q "storage\logs" >nul 2>&1

echo.
echo %GREEN%[OK]%RESET% Uninstall complete
echo.
pause
exit /b 0

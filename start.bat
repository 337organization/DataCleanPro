@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion
title DataCleanPro Launcher

cd /d "%~dp0"

set "JAVA_MIN_VERSION=17"
set "MAVEN_VERSION=3.9.6"
set "MAVEN_BASE=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin\3311e1d4"
set "MAVEN_DIR=%MAVEN_BASE%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"
set "APP_MAIN=com.datacleanpro.App"
set "VERSION=1.2.2"

if /i "%~1"=="--help" goto show_help
if /i "%~1"=="--stop" goto stop_services
if /i "%~1"=="--uninstall" goto uninstall
if /i "%~1"=="--version" goto show_version
if /i "%~1"=="--check-java" goto check_java_only

call :write_log "DataCleanPro Startup Script v%VERSION%"

echo.
echo  ========================================================
echo     DataCleanPro - Check, Build and Launch v%VERSION%
echo  ========================================================
echo.

echo [0/6] Checking network...
set "NETWORK_OK=0"
ping -n 1 -w 1000 archive.apache.org >nul 2>&1
if "%errorlevel%"=="0" set "NETWORK_OK=1"
if "%NETWORK_OK%"=="1" echo [OK] Network available
if not "%NETWORK_OK%"=="1" echo [WARN] Offline mode
echo.

echo [1/6] Checking Java...
call :detect_java
if not defined JAVA_EXE goto install_java
if not defined JAVA_MAJOR goto install_java
if !JAVA_MAJOR! GEQ %JAVA_MIN_VERSION% goto java_ok
echo [WARN] Found Java !JAVA_MAJOR!, but Java %JAVA_MIN_VERSION%+ is required.
goto install_java

:java_ok
echo [OK] Java !JAVA_MAJOR! found: !JAVA_EXE!
if defined JAVA_HOME (
    set "PATH=!JAVA_HOME!\bin;!PATH!"
)
goto check_maven

:install_java
if "%NETWORK_OK%"=="0" goto java_missing_offline
echo [WARN] Java %JAVA_MIN_VERSION%+ was not found. Trying winget install...
where winget >nul 2>&1
if not "%errorlevel%"=="0" goto java_install_failed
winget install EclipseAdoptium.Temurin.17.JDK --silent --accept-package-agreements --accept-source-agreements
if "%errorlevel%"=="0" goto java_after_install
goto java_install_failed

:java_missing_offline
echo [X] JDK %JAVA_MIN_VERSION%+ is required. Offline mode cannot install it.
pause
exit /b 1

:java_installed
echo [OK] Java installed and detected.
goto check_maven

:java_after_install
call :detect_java
if not defined JAVA_EXE goto java_install_needs_restart
if not defined JAVA_MAJOR goto java_install_needs_restart
if !JAVA_MAJOR! GEQ %JAVA_MIN_VERSION% goto java_installed

:java_install_needs_restart
echo [WARN] Java is installed, but java.exe is not available in this terminal yet.
echo        Reopen the terminal or restart Windows, then run start.bat again.
pause
exit /b 0

:java_install_failed
echo [X] Java auto install failed. Install JDK 17+ manually: https://adoptium.net/
pause
exit /b 1

:check_maven
echo.
echo [2/6] Checking Maven...
set "MVN=%MAVEN_DIR%\bin\mvn.cmd"
if exist "%MVN%" goto maven_ok

where mvn >nul 2>&1
if "%errorlevel%"=="0" (
    set "MVN=mvn"
    goto maven_ok
)

if "%NETWORK_OK%"=="0" goto maven_missing_offline
goto download_maven

:maven_ok
echo [OK] Maven available
goto check_mysql

:maven_missing_offline
echo [X] Maven is required. Offline mode cannot download it.
pause
exit /b 1

:download_maven
echo [WARN] Downloading Maven %MAVEN_VERSION%...
if not exist "%MAVEN_BASE%" mkdir "%MAVEN_BASE%" >nul 2>&1
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%TEMP%\maven.zip'"
if not exist "%TEMP%\maven.zip" goto maven_download_failed
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%MAVEN_BASE%' -Force"
del "%TEMP%\maven.zip" >nul 2>&1
if exist "%MVN%" goto maven_download_ok
goto maven_download_failed

:maven_download_ok
echo [OK] Maven installed
goto check_mysql

:maven_download_failed
echo [X] Maven download or unzip failed.
pause
exit /b 1

:check_mysql
echo.
echo [3/6] Checking MySQL...
where mysql >nul 2>&1
if not "%errorlevel%"=="0" goto mysql_client_missing
echo [OK] mysql command available
goto config_db

:mysql_client_missing
echo [WARN] mysql command was not found. Database initialization is skipped.
goto start_app

:config_db
echo.
echo [4/6] Testing database connection...
set "DB_PASS="
if exist "src\main\resources\application.properties" for /f "tokens=1,* delims==" %%a in ('findstr /b /i "db.password=" "src\main\resources\application.properties" 2^>nul') do set "DB_PASS=%%b"

mysql -u root -p!DB_PASS! -e "SELECT 1" >nul 2>&1
if not "%errorlevel%"=="0" goto db_failed
echo [OK] Database connected
mysql -u root -p!DB_PASS! -e "CREATE DATABASE IF NOT EXISTS datacleanpro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci" >nul 2>&1
if not "%errorlevel%"=="0" goto db_schema_failed

set "SCHEMA_EXISTS=0"
mysql -u root -p!DB_PASS! -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='datacleanpro' AND table_name='data_file'" > "%TEMP%\datacleanpro_schema_check.txt" 2>nul
if "%errorlevel%"=="0" for /f "usebackq" %%v in ("%TEMP%\datacleanpro_schema_check.txt") do set "SCHEMA_EXISTS=%%v"
del "%TEMP%\datacleanpro_schema_check.txt" >nul 2>&1

if "%SCHEMA_EXISTS%"=="0" goto import_schema
echo [OK] Database schema already exists
goto start_app

:import_schema
if not exist "src\main\resources\db\schema.sql" goto db_schema_missing
mysql -u root -p!DB_PASS! datacleanpro < "src\main\resources\db\schema.sql" >nul 2>&1
if "%errorlevel%"=="0" echo [OK] Database schema imported
if not "%errorlevel%"=="0" goto db_schema_failed
goto start_app

:db_schema_missing
echo [WARN] schema.sql was not found. The app will still start.
goto start_app

:db_schema_failed
echo [WARN] Database schema initialization failed. The app will still start.
goto start_app

:db_failed
echo [WARN] Database connection failed. Database initialization is skipped.
echo     Check db.password in src\main\resources\application.properties.
goto start_app

:start_app
echo.
echo [5/6] Compiling project...

if not exist "pom.xml" goto project_missing
if not exist "src\main\java\com\datacleanpro\App.java" goto app_missing

call "%MVN%" compile -q
if not "%errorlevel%"=="0" goto compile_failed
echo [OK] Compile successful

echo.
echo [6/6] Starting DataCleanPro...
echo ========================================
echo.

call "%MVN%" org.codehaus.mojo:exec-maven-plugin:3.3.0:java -Dexec.mainClass="%APP_MAIN%"
set "APP_EXIT_CODE=%errorlevel%"

echo.
if "%APP_EXIT_CODE%"=="0" echo [OK] Application closed normally
if not "%APP_EXIT_CODE%"=="0" echo [X] Application exited with code %APP_EXIT_CODE%
pause
exit /b %APP_EXIT_CODE%

:project_missing
echo [X] pom.xml was not found. Make sure start.bat is in the project root.
pause
exit /b 1

:app_missing
echo [X] App.java was not found.
pause
exit /b 1

:compile_failed
echo [X] Compile failed. Check Maven output above.
pause
exit /b 1

:show_help
echo Usage: start.bat [--stop] [--uninstall] [--version] [--help]
echo.
echo   --stop       Stop only DataCleanPro Java processes
echo   --uninstall  Remove downloaded Maven and target directory
echo   --version    Show script version
echo   --help       Show help
exit /b 0

:show_version
echo v%VERSION%
exit /b 0

:stop_services
echo Stopping DataCleanPro...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$procs = Get-CimInstance Win32_Process | Where-Object { ($_.Name -eq 'java.exe' -or $_.Name -eq 'javaw.exe') -and ($_.CommandLine -like '*exec.mainClass=%APP_MAIN%*' -or $_.CommandLine -like '*%APP_MAIN%*') }; if ($procs) { $procs | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }; Write-Host '[OK] DataCleanPro process stopped.' } else { Write-Host '[WARN] No DataCleanPro process found.' }"
pause
exit /b 0

:uninstall
if exist "%MAVEN_DIR%" rmdir /s /q "%MAVEN_DIR%" >nul 2>&1
if exist "target" rmdir /s /q "target" >nul 2>&1
echo [OK] Local Maven cache and target directory removed.
pause
exit /b 0

:write_log
echo [%date% %time%] %~1>> startup.log
exit /b 0

:detect_java
set "ORIGINAL_JAVA_HOME=%JAVA_HOME%"
set "JAVA_EXE="
set "JAVA_MAJOR="
set "JAVA_HOME="

for /f "tokens=*" %%j in ('where java.exe 2^>nul') do call :check_java_candidate "%%j"
if defined JAVA_EXE exit /b 0

if not defined ORIGINAL_JAVA_HOME goto detect_java_program_files
if exist "%ORIGINAL_JAVA_HOME%\bin\java.exe" call :check_java_candidate "%ORIGINAL_JAVA_HOME%\bin\java.exe"
if defined JAVA_EXE exit /b 0

:detect_java_program_files
for /d %%j in ("%ProgramFiles%\Eclipse Adoptium\jdk-*") do if exist "%%~fj\bin\java.exe" call :check_java_candidate "%%~fj\bin\java.exe"
if defined JAVA_EXE exit /b 0

for /d %%j in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~fj\bin\java.exe" call :check_java_candidate "%%~fj\bin\java.exe"
if defined JAVA_EXE exit /b 0

for /d %%j in ("%ProgramFiles%\Microsoft\jdk-*") do if exist "%%~fj\bin\java.exe" call :check_java_candidate "%%~fj\bin\java.exe"
exit /b 0

:check_java_candidate
if defined JAVA_EXE exit /b 0
set "CANDIDATE_JAVA=%~1"
if not exist "!CANDIDATE_JAVA!" exit /b 0
set "CANDIDATE_MAJOR="
set "CANDIDATE_VERSION="
set "JAVA_VERSION_FILE=%TEMP%\datacleanpro_java_version.txt"
del "!JAVA_VERSION_FILE!" >nul 2>&1
"!CANDIDATE_JAVA!" -version > "!JAVA_VERSION_FILE!" 2>&1
if errorlevel 1 exit /b 0
for /f "tokens=3" %%v in ('findstr /i "version" "!JAVA_VERSION_FILE!" 2^>nul') do if not defined CANDIDATE_VERSION set "CANDIDATE_VERSION=%%v"
del "!JAVA_VERSION_FILE!" >nul 2>&1
if not defined CANDIDATE_VERSION exit /b 0
set "CANDIDATE_VERSION=!CANDIDATE_VERSION:"=!"
for /f "tokens=1,2 delims=." %%a in ("!CANDIDATE_VERSION!") do (
    set "CANDIDATE_MAJOR=%%a"
    if "%%a"=="1" set "CANDIDATE_MAJOR=%%b"
)
if not defined CANDIDATE_MAJOR exit /b 0
if !CANDIDATE_MAJOR! LSS %JAVA_MIN_VERSION% exit /b 0
set "JAVA_EXE=!CANDIDATE_JAVA!"
set "JAVA_MAJOR=!CANDIDATE_MAJOR!"
for %%b in ("!CANDIDATE_JAVA!") do set "JAVA_BIN=%%~dpb"
for %%h in ("!JAVA_BIN!..") do set "JAVA_HOME=%%~fh"
exit /b 0

:check_java_only
call :detect_java
if not defined JAVA_EXE goto check_java_missing
echo [OK] Java !JAVA_MAJOR! found: !JAVA_EXE!
echo JAVA_HOME=!JAVA_HOME!
exit /b 0

:check_java_missing
echo [X] Java %JAVA_MIN_VERSION%+ was not found.
exit /b 1

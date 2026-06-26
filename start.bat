@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title DataCleanPro - ????????????

echo.
echo  ================================================================
echo            DataCleanPro - ????????????
echo  ================================================================
echo.

:: ============================================
:: ??????Java??
:: ============================================
echo [1/4] ??Java??...
echo ----------------------------------------------------------------

java -version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VER=%%g
    )
    set JAVA_VER=!JAVA_VER:"=!
    echo       [OK] Java???: !JAVA_VER!
    goto :check_maven
)

echo       [X] ????Java??
echo.
echo       ????????Java:
echo.
echo       ??1 - ??:
echo       1. ?? https://adoptium.net/
echo       2. ?? Temurin JDK 17 (LTS)
echo       3. ????????? "Add to PATH"
echo       4. ????????????
echo.
echo       ??2:
echo       ?? https://www.oracle.com/java/technologies/downloads/
echo.
set /p INSTALL_JAVA="??????????? (Y/N): "
if /i "!INSTALL_JAVA!"=="Y" (
    start https://adoptium.net/
    echo.
    echo       ?????????????
)
pause
exit /b 1

:check_maven
echo.

:: ============================================
:: ??????Maven??
:: ============================================
echo [2/4] ??Maven??...
echo ----------------------------------------------------------------

:: ????Maven
set "LOCAL_MAVEN=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6"
if exist "%LOCAL_MAVEN%\bin\mvn.cmd" (
    set "MVN_CMD=%LOCAL_MAVEN%\bin\mvn.cmd"
    echo       [OK] ????Maven 3.9.6
    goto :check_mysql
)

:: ????Maven
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set "MVN_CMD=mvn"
    echo       [OK] ??Maven???
    goto :check_mysql
)

echo       [!] ????Maven???????...
echo.

:: ????
set "MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6"
if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%"

:: ??Maven
set "MAVEN_URL=https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
set "MAVEN_ZIP=%TEMP%\maven.zip"

echo       ????Maven 3.9.6...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%'" 2>nul

if not exist "%MAVEN_ZIP%" (
    echo       [X] ??????
    echo.
    echo       ?????Maven:
    echo       1. ?? https://maven.apache.org/download.cgi
    echo       2. ?? apache-maven-3.9.6-bin.zip
    echo       3. ????bin???????PATH
    echo.
    pause
    exit /b 1
)

:: ??
echo       ????Maven...
powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4' -Force" 2>nul

if exist "%MAVEN_DIR%\bin\mvn.cmd" (
    set "MVN_CMD=%MAVEN_DIR%\bin\mvn.cmd"
    echo       [OK] Maven????
    del "%MAVEN_ZIP%" >nul 2>&1
) else (
    echo       [X] Maven????
    pause
    exit /b 1
)

:check_mysql
echo.

:: ============================================
:: ??????MySQL??
:: ============================================
echo [3/4] ??MySQL??...
echo ----------------------------------------------------------------

set "MYSQL_FOUND=0"

:: ??MySQL??
sc query MySQL80 >nul 2>&1
if %errorlevel% equ 0 (
    set "MYSQL_FOUND=1"
)

sc query MySQL >nul 2>&1
if %errorlevel% equ 0 (
    set "MYSQL_FOUND=1"
)

where mysql >nul 2>&1
if %errorlevel% equ 0 (
    set "MYSQL_FOUND=1"
)

if "!MYSQL_FOUND!"=="1" (
    echo       [OK] MySQL???
) else (
    echo       [!] ????MySQL
    echo.
    echo       ??: ???????MySQL 8.0+
    echo       ????????????/????????
    echo.
    echo       ????MySQL:
    echo       1. ?? https://dev.mysql.com/downloads/installer/
    echo       2. ?? MySQL Installer
    echo       3. ????? "Server only"
    echo       4. ??root?????
    echo.
    set /p CONTINUE="??????? (Y/N): "
    if /i not "!CONTINUE!"=="Y" (
        exit /b 0
    )
)

echo.

:: ============================================
:: ??????????
:: ============================================
echo [4/4] ??????...
echo ----------------------------------------------------------------
echo.

:: ??????
if not exist "src\main\java\com\datacleanpro\App.java" (
    echo  [X] ??: ???????
    echo  ????DataCleanPro??????????
    pause
    exit /b 1
)

:: ??
echo  ?????????????1-2???...
call "%MVN_CMD%" compile -q 2>nul
if %errorlevel% neq 0 (
    echo.
    echo  [X] ????????Java?????17+
    pause
    exit /b 1
)
echo  [OK] ????
echo.
echo  ????DataCleanPro...
echo.
echo  ================================================================
echo   ??: ???????????????application.properties???MySQL??
echo  ================================================================
echo.

:: ????
call "%MVN_CMD%" exec:java -Dexec.mainClass="com.datacleanpro.App"

echo.
echo  ???????
pause

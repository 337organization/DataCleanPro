@echo off
chcp 65001 >nul
title DataCleanPro

echo.
echo  ========================================
echo    DataCleanPro - Data Processing System
echo  ========================================
echo.

echo [1/3] Checking Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Java not found, please install JDK 17 from https://adoptium.net/
    pause
    exit /b 1
)
echo Java OK

echo.
echo [2/3] Checking Maven...
set "MVN=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6\bin\mvn.cmd"
if not exist "%MVN%" (
    set "MVN=mvn"
    where mvn >nul 2>&1
    if %errorlevel% neq 0 (
        echo Maven not found, downloading...
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\maven.zip'"
        powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4' -Force"
        set "MVN=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6\bin\mvn.cmd"
    )
)
echo Maven OK

echo.
echo [3/3] Starting DataCleanPro...
echo.
call "%MVN%" exec:java -Dexec.mainClass="com.datacleanpro.App"
echo.
echo Application closed
pause
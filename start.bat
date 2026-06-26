@echo off
chcp 65001 >nul
title DataCleanPro ???...

echo.
echo  ========================================
echo    DataCleanPro - ?????????
echo  ========================================
echo.

:: ??Java
echo [1/3] ??Java??...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [??] ???Java????JDK 17+
    pause
    exit /b 1
)
echo       Java????

:: Maven??
echo [2/3] ??Maven...
set "MVN_HOME=C:\Users\28394\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6"
if not exist "%MVN_HOME%\bin\mvn.cmd" (
    echo [??] ???Maven
    pause
    exit /b 1
)
echo       Maven??

:: ????
echo [3/3] ??????...
echo.
echo  ????????...
echo.

:: ??Maven exec????
"%MVN_HOME%\bin\mvn.cmd" exec:java -Dexec.mainClass="com.datacleanpro.App" -q

pause

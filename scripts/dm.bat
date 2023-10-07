@echo off
REM Check for Java 17+
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER=%%g
)
set JAVA_VER=%JAVA_VER:~0,2%
IF %JAVA_VER% LSS 17 (
    echo You need Java version 17 or newer.
    exit /b
)

REM Collect inputs from the user
set /p PROJECT_NAME="Enter project name (e.g., todo, my-todo-app): "
set /p PACKAGE_NAME="Enter package name (e.g., org.ionatomics, org.acme, com.ed.ian): "
set /p TEMPLATE_NAME="Enter name of the example (rest, entity, todo, etc.): "
set /p DB_TYPE="Enter Database Type (mysql, mariadb, etc.) or press Enter to skip: "

REM Execute the Java CLI
java -jar dm.jar -n %PROJECT_NAME% -p %PACKAGE_NAME% -t %TEMPLATE_NAME% -d %DB_TYPE%

REM End of the script

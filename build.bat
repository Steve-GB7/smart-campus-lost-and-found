@echo off
echo ========================================================
echo   Compiling Unified Smart Campus Helpdesk (CEC)
echo ========================================================

if not exist "out" mkdir out
if not exist "lib\mysql-connector-j-8.3.0.jar" (
    echo [WARNING] MySQL JDBC jar not found in lib\
)

javac -encoding UTF-8 -cp "lib/*" -d out ^
    src\com\cec\helpdesk\model\enums\*.java ^
    src\com\cec\helpdesk\model\*.java ^
    src\com\cec\helpdesk\dao\*.java ^
    src\com\cec\helpdesk\service\*.java ^
    src\com\cec\helpdesk\service\impl\*.java ^
    src\com\cec\helpdesk\adapter\*.java ^
    src\com\cec\helpdesk\controller\*.java ^
    src\com\cec\helpdesk\view\*.java ^
    src\com\cec\helpdesk\App.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation finished with 0 errors!
) else (
    echo [ERROR] Compilation failed.
)
pause

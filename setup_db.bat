@echo off
echo ========================================================
echo   Setting up CEC Helpdesk MySQL Database
echo ========================================================
echo Importing schema.sql into MySQL localhost...
echo.
set /p MYSQL_USER="Enter MySQL Username [default: root]: " || set MYSQL_USER=root
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo Running: mysql -u %MYSQL_USER% -p < schema.sql
mysql -u %MYSQL_USER% -p < schema.sql

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Database and tables created successfully!
) else (
    echo [ERROR] Database setup encountered an error. Check MySQL service status and credentials.
)
pause

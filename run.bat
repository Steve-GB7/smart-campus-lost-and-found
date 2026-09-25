@echo off
echo ========================================================
echo   Launching Unified Smart Campus Helpdesk (CEC)
echo ========================================================

java -cp "out;lib/*" com.cec.helpdesk.App %*
pause

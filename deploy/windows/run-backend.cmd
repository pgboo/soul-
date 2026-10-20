@echo off
REM =====================================================================
REM  Soul 社交 - 后端守护进程（由 Windows 计划任务调用）
REM  崩了自动重启，脱离任何终端窗口 / AI 会话独立运行。
REM  路径全部用 %~dp0 推导，不写死中文路径，避免编码问题。
REM =====================================================================
setlocal
for %%I in ("%~dp0..\..") do set ROOT=%%~fI
set JAVA_BIN=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\java.exe
set JAR=%ROOT%\backend\target\soul-social-backend-1.0.0.jar
set LOGDIR=%ROOT%\logs
set LOG=%LOGDIR%\service-backend.log
set PORT=8090
set PROFILE=test

if not exist "%LOGDIR%" mkdir "%LOGDIR%"

:loop
echo. >> "%LOG%"
echo ===== [%DATE% %TIME%] starting backend ===== >> "%LOG%"

netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul
if not errorlevel 1 (
  echo     port %PORT% already listening, skip this round >> "%LOG%"
  timeout /t 30 /nobreak >nul
  goto loop
)

cd /d "%ROOT%\backend"
"%JAVA_BIN%" -jar "%JAR%" --spring.profiles.active=%PROFILE% --server.port=%PORT% >> "%LOG%" 2>&1

echo ===== [%DATE% %TIME%] backend exited, restart in 10s ===== >> "%LOG%"
timeout /t 10 /nobreak >nul
goto loop

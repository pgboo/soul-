@echo off
REM =====================================================================
REM  Soul 社交 - Cloudflare Named Tunnel 守护进程（由计划任务调用）
REM  固定域名，断了自动重连。配置见 deploy\cloudflare\config.yml
REM =====================================================================
setlocal
for %%I in ("%~dp0..\..") do set ROOT=%%~fI
set CF_BIN=D:\cloudflared-windows-amd64.exe
set CONF=%ROOT%\deploy\cloudflare\config.yml
set LOGDIR=%ROOT%\logs
set LOG=%LOGDIR%\service-tunnel.log

if not exist "%LOGDIR%" mkdir "%LOGDIR%"

if not exist "%CONF%" (
  echo [%DATE% %TIME%] ERROR: config not found: %CONF% >> "%LOG%"
  echo   请先执行 bash deploy/cloudflare/setup-tunnel.sh ^<你的域名^>
  exit /b 1
)

:loop
echo. >> "%LOG%"
echo ===== [%DATE% %TIME%] starting tunnel ===== >> "%LOG%"
"%CF_BIN%" tunnel --config "%CONF%" --no-autoupdate --protocol http2 --edge-ip-version 4 >> "%LOG%" 2>&1
echo ===== [%DATE% %TIME%] tunnel exited, restart in 10s ===== >> "%LOG%"
timeout /t 10 /nobreak >nul
goto loop

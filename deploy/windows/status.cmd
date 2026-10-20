@echo off
REM 查看服务状态
setlocal
for %%I in ("%~dp0..\..") do set ROOT=%%~fI
set CONF=%ROOT%\deploy\cloudflare\config.yml

echo =============== 计划任务 ===============
schtasks /query /tn "SoulSocial-Backend" /fo LIST 2>nul | findstr /i "TaskName Status"
schtasks /query /tn "SoulSocial-Tunnel" /fo LIST 2>nul | findstr /i "TaskName Status"

echo.
echo =============== 端口 ===============
netstat -ano | findstr ":8090 " | findstr LISTENING

echo.
echo =============== 进程 ===============
tasklist /fi "imagename eq java.exe" 2>nul | findstr /i java.exe
tasklist /fi "imagename eq cloudflared-windows-amd64.exe" 2>nul | findstr /i cloudflared

echo.
echo =============== 公网地址 ===============
if exist "%CONF%" (
  findstr /i "hostname" "%CONF%"
) else (
  echo   尚未配置（config.yml 不存在）
)

echo.
echo =============== 本机自检 ===============
curl -s -o nul -w "  http://localhost:8090/  -^> HTTP %%{http_code}\n" --max-time 8 http://localhost:8090/

echo.
pause

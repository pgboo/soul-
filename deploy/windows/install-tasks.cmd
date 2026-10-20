@echo off
REM =====================================================================
REM  安装两个开机自启的 Windows 计划任务：
REM    SoulSocial-Backend  -> 后端 (8090)
REM    SoulSocial-Tunnel   -> Cloudflare 隧道
REM  两者都是无限循环 + 崩溃自动重启，且隐藏窗口运行。
REM  需要【以管理员身份】运行本脚本。
REM =====================================================================
setlocal
for %%I in ("%~dp0..\..") do set ROOT=%%~fI
set WSCRIPT=%SystemRoot%\System32\wscript.exe
set B=%ROOT%\deploy\windows\hidden.vbs "%ROOT%\deploy\windows\run-backend.cmd"
set T=%ROOT%\deploy\windows\hidden.vbs "%ROOT%\deploy\windows\run-tunnel.cmd"

echo ==^> 项目根目录: %ROOT%
echo.

net session >nul 2>&1
if errorlevel 1 (
  echo !! 需要管理员权限。请右键「以管理员身份运行」本脚本。
  pause
  exit /b 1
)

echo ==^> 清理旧任务（如存在）
schtasks /end /tn "SoulSocial-Backend" >nul 2>&1
schtasks /end /tn "SoulSocial-Tunnel" >nul 2>&1
schtasks /delete /tn "SoulSocial-Backend" /f >nul 2>&1
schtasks /delete /tn "SoulSocial-Tunnel" /f >nul 2>&1

echo ==^> 注册后端任务
schtasks /create /tn "SoulSocial-Backend" /tr "\"%WSCRIPT%\" %B%" /sc onlogon /rl highest /f
if errorlevel 1 goto fail

echo ==^> 注册隧道任务
schtasks /create /tn "SoulSocial-Tunnel" /tr "\"%WSCRIPT%\" %T%" /sc onlogon /rl highest /f
if errorlevel 1 goto fail

echo.
echo ==^> 立即启动
schtasks /run /tn "SoulSocial-Backend" >nul
timeout /t 3 /nobreak >nul
schtasks /run /tn "SoulSocial-Tunnel" >nul

echo.
echo ============================================================
echo   安装完成。两个服务已后台启动，开机自动运行。
echo   查看状态：deploy\windows\status.cmd
echo   卸载：    deploy\windows\uninstall-tasks.cmd
echo ============================================================
pause
exit /b 0

:fail
echo !! 任务创建失败，请确认以管理员身份运行。
pause
exit /b 1

@echo off
REM 卸载开机自启的两个计划任务
setlocal
net session >nul 2>&1
if errorlevel 1 (
  echo !! 需要管理员权限。
  pause
  exit /b 1
)
schtasks /end /tn "SoulSocial-Backend" >nul 2>&1
schtasks /end /tn "SoulSocial-Tunnel" >nul 2>&1
schtasks /delete /tn "SoulSocial-Backend" /f
schtasks /delete /tn "SoulSocial-Tunnel" /f
echo.
echo ==^> 已卸载。进程可能仍在运行，可用 status.cmd 查看 PID 后手动结束。
pause

' 隐藏窗口启动器：wscript hidden.vbs "要执行的脚本"
' 计划任务直接跑 .cmd 会弹一个黑窗口，用它包一层就没有窗口了。
Set sh = CreateObject("WScript.Shell")
If WScript.Arguments.Count < 1 Then WScript.Quit 1
sh.Run """" & WScript.Arguments(0) & """", 0, False

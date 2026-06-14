# VinScanner — 模拟器前置互斥检查
# 用法： powershell -ExecutionPolicy Bypass -File .\tooling\android_emulator_precheck.ps1
# 退出码： 0=空闲可使用， 1=已被占用， 2=错误

$ErrorActionPreference = "Stop"
$Adb = "E:\Program Files\Android\Sdk\platform-tools\adb.exe"

Write-Host "== PRECHECK adb devices =="
$out = (& $Adb devices) -join "`n"
Write-Host $out

Write-Host "== PRECHECK process table =="
$procs = Get-Process | Where-Object { $_.ProcessName -match 'emulator|qemu' }
if ($procs -and $procs.Count -gt 0) {
  $procs | ForEach-Object { Write-Host ("  pid=" + $_.Id + " name=" + $_.ProcessName) }
  Write-Host "RESULT: IN_USE — emulator process(es) running."
  exit 1
}

if ($out -match 'emulator-\d+\s+device') {
  Write-Host "RESULT: IN_USE — adb devices shows emulator."
  exit 1
}

Write-Host "RESULT: FREE — emulator available."
exit 0

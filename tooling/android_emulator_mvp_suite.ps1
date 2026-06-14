# VinScanner — Android Emulator MVP Suite
# 遵循全局规则：e:\AI\tooling\ANDROID_EMULATOR_RULES.md
#
# 使用前必读：
#   1. 先在 Android Studio 中打开 e:\AI\VinScanner，让它同步并生成 gradle wrapper
#   2. 再运行本脚本：powershell -ExecutionPolicy Bypass -File .\tooling\android_emulator_mvp_suite.ps1

param(
  [string]$AvdName = "quitsmoke_pixel",
  [string]$Serial = "emulator-5554",
  [string]$ApkPath = "app\build\outputs\apk\debug\app-debug.apk",
  [string]$PackageName = "com.vinscanner.app",
  [string]$LaunchActivity = "com.vinscanner.app.MainActivity",
  [int]$BootTimeoutSeconds = 240
)

$ErrorActionPreference = "Stop"

# ====== 路径（与本机实际环境对齐） ======
$AndroidHome = "E:\Program Files\Android\Sdk"
$Emulator    = Join-Path $AndroidHome "emulator\emulator.exe"
$Adb         = Join-Path $AndroidHome "platform-tools\adb.exe"
$GradleExe   = Join-Path (Resolve-Path ".").Path "gradlew.bat"

function Invoke-Adb {
  param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
  & $Adb -s $Serial @Args
}

# ====== 【强制】互斥检查：adb devices + 进程表 ======
Write-Host "== MUTEX_CHECK: adb devices =="
$devicesOut = (& $Adb devices) -join "`n"
Write-Host $devicesOut

Write-Host "== MUTEX_CHECK: process =="
$procs = Get-Process | Where-Object { $_.ProcessName -match 'emulator|qemu' }
if ($procs -and $procs.Count -gt 0) {
  $procs | ForEach-Object { Write-Host ("  pid=" + $_.Id + " name=" + $_.ProcessName) }
  throw "MUTEX_VIOLATION: emulator/qemu 进程已存在，当前时刻仅允许一个 Agent 使用模拟器。"
}
if ($devicesOut -match 'emulator-\d+') {
  throw "MUTEX_VIOLATION: adb devices 中已存在模拟器设备，当前时刻仅允许一个 Agent 使用。"
}
Write-Host "MUTEX_CHECK_OK"

# ====== 1. 构建 APK ======
if (-not (Test-Path $GradleExe)) {
  throw "gradlew.bat 不存在。请先在 Android Studio 中打开项目并执行一次 Sync，让 Android Studio 生成 Gradle Wrapper。"
}
if (-not (Test-Path $ApkPath)) {
  Write-Host "BUILD_START: gradlew.bat assembleDebug"
  & $GradleExe assembleDebug
  if ($LASTEXITCODE -ne 0) { throw "BUILD_FAILED: gradlew assembleDebug exit=$LASTEXITCODE" }
  Write-Host "BUILD_OK"
}

# ====== 2. 启动模拟器 ======
& $Adb start-server | Out-Null
$emulatorArgs = @(
  "-avd", $AvdName,
  "-no-window", "-no-audio", "-no-boot-anim",
  "-no-snapshot", "-no-metrics",
  "-gpu", "swiftshader_indirect",
  "-memory", "1536"
)
$emulatorProcess = [System.Diagnostics.Process]::Start($Emulator, $emulatorArgs)
Write-Host ("EMULATOR_STARTED pid=" + $emulatorProcess.Id)

try {
  # ====== 3. 等待启动 ======
  $booted = $false
  $deadline = (Get-Date).AddSeconds($BootTimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    Start-Sleep -Seconds 10
    $out = (& $Adb devices) -join "`n"
    Write-Host $out
    if ($out -match "$Serial\s+device") {
      $boot = ((Invoke-Adb shell getprop sys.boot_completed 2>$null) -join "").Trim()
      Write-Host "boot_completed=$boot"
      if ($boot -eq "1") { $booted = $true; break }
    }
  }
  if (-not $booted) { throw "EMULATOR_BOOT_TIMEOUT" }

  # ====== 4. 安装 + 启动 + 冒烟测试 ======
  Invoke-Adb install -r $ApkPath | Out-Host

  $launchCmd = "am start -n $PackageName/$LaunchActivity"
  Write-Host "LAUNCH: $launchCmd"
  Invoke-Adb shell $launchCmd | Out-Host

  Start-Sleep -Seconds 6
  $focused = ((Invoke-Adb shell dumpsys window windows) -join "`n")
  if ($focused -notmatch [regex]::Escape($PackageName)) {
    throw "APP_NOT_IN_FOCUS after launch"
  }
  Write-Host "SMOKE_OK package=$PackageName"

  # 5. 权限审查（相机权限属于业务必要项）
  $dumpsys = (Invoke-Adb shell dumpsys package $PackageName) -join "`n"
  if ($dumpsys -notmatch 'android.permission.CAMERA') {
    Write-Host "PRIVACY_NOTE: android.permission.CAMERA not present — 符合扫码场景将授权"
  } else {
    Write-Host "PRIVACY_OK: android.permission.CAMERA present (扫码场景需此权限)"
  }

  Write-Host "VINSCANNER_MVP_SUITE_PASSED"
}
finally {
  # ====== 【强制】清理：务必关闭模拟器 + kill 进程 ======
  try { & $Adb -s $Serial emu kill 2>$null | Out-Host } catch {}
  Start-Sleep -Seconds 3
  Get-Process | Where-Object { $_.ProcessName -match 'emulator|qemu' } | Stop-Process -Force -ErrorAction SilentlyContinue
  & $Adb kill-server 2>$null | Out-Null

  # 最终校验
  $after = (& $Adb devices) -join "`n"
  $afterProcs = Get-Process | Where-Object { $_.ProcessName -match 'emulator|qemu' }
  Write-Host ("CLEANUP_VERIFY devices=`n" + $after)
  Write-Host ("CLEANUP_VERIFY procs=" + ($afterProcs.Count))
}

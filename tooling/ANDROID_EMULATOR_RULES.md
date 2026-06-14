# Android Emulator — 本项目规则（遵循 e:\AI\tooling\ANDROID_EMULATOR_RULES.md 全局规则）

- AVD: `quitsmoke_pixel`
- 互斥：使用前 `adb devices` + `Get-Process | ? ProcessName -match 'emulator|qemu'` 双重检查
- 测试套件：`tooling\android_emulator_mvp_suite.ps1`
- 清理：每次会话末必须 `adb -s emulator-5554 emu kill` 并终止所有 emulator/qemu 进程

# AGENTS.md — VinScanner 项目记忆

> 本文件供 AI 编码助手（ZCode / Codex / Claude 等）在每次会话自动加载，作为项目背景与约定。
> 如有架构或约定变更，请同步更新此处。

## 项目简介

**VinScanner** 是一个 Android 原生应用，用于通过相机扫码或手动输入采集车辆 VIN 码，
本地保存列表，并可将采集到的 VIN 列表通过邮件发送。

## 基本信息速查

| 项目 | 内容 |
|------|------|
| 类型 | Android 原生应用 |
| 语言 | Kotlin |
| 构建系统 | Gradle Kotlin DSL |
| 应用包名 | `com.vinscanner.app` |
| 版本 | `1.0.4` (versionCode=4) |
| SDK | minSdk 24 / target & compile SDK 34 |
| JDK | 17 |
| SDK 路径 | `E:\Program Files\Android\Sdk`（见 `local.properties`） |

## 核心功能

1. 📷 **扫码采集** — ZXing 相机连续扫码识别 VIN
2. ⌨️ **手动输入** — 对话框输入 VIN
3. ✅ **校验规范化** — 17 位格式校验，过滤 I/O/Q 非法字符，去重
4. 📋 **列表管理** — 复制 / 删除 / 清空 / 计数，点击编辑，长按删除
5. 📧 **邮件发送** — 通过 ACTION_SENDTO 调起邮件应用发送 VIN 列表
6. ⚙️ **设置** — 配置收件邮箱与邮件主题（SharedPreferences `vin_scanner_settings`）
7. 🛡️ **崩溃上报** — 全局 UncaughtExceptionHandler，下次启动弹窗提示上次崩溃（SharedPreferences `vin_scanner_crash`）

## 代码架构（MVVM）

```
app/src/main/java/com/vinscanner/app/
├── MainActivity.kt                 主界面（入口，列表 + FAB 扫码 + 操作按钮）
├── VinScannerApp.kt                Application + 崩溃捕获
│
├── data/                           数据层
│   ├── VinRecord.kt                数据模型（vin / timestamp / source）
│   └── VinRepository.kt            持久化：SharedPreferences + Gson
│                                   （PREFS: vin_scanner_prefs, KEY: vin_records）
│
├── viewmodel/                      视图模型层
│   ├── VinViewModel.kt             业务逻辑 + LiveData<List<VinRecord>>
│   └── VinViewModelFactory.kt      ViewModel 工厂
│
├── ui/                             视图层
│   ├── scan/ScanActivity.kt        扫码页（ZXing DecoratedBarcodeView）
│   ├── input/InputDialogFragment.kt  手动输入弹窗
│   ├── list/VinListAdapter.kt      VIN 列表适配器（DiffUtil）
│   ├── edit/VinEditActivity.kt     编辑 VIN 页
│   └── settings/SettingsActivity.kt 设置页（PreferenceFragmentCompat）
│
└── util/                           工具层
    ├── VinValidator.kt             VIN 校验（17位 / 过滤 I O Q / normalize）
    └── EmailSender.kt              邮件 Intent 与正文构造
```

### 关键数据流

- 扫码：`ScanActivity` → `EXTRA_SCAN_RESULT` → `MainActivity.addVin()` → `VinViewModel.add()` → `VinRepository.add()` → `refresh()`
- 输入：`InputDialogFragment` 校验通过 → `listener.onVinInput()` → 同上
- 编辑：`MainActivity` 点击项 → `VinEditActivity`（传 index + value）→ `viewModel.updateVinAt()`
- 邮件：`MainActivity.sendEmail()` → 读设置 Prefs → `EmailSender.buildEmailIntent()` → `startActivity()`

## 测试

- **单元测试**（`app/src/test/`）：
  - `VinValidatorTest`、`EmailSenderTest`、`VinRepositoryTest`、`VinViewModelTest`
- **插桩测试**（`app/src/androidTest/`）：
  - `MainActivitySmokeTest`
- 状态：`testDebugUnitTest` 已通过 ✅

## 关键依赖

- AndroidX：AppCompat 1.6.1、Material 1.11.0、RecyclerView 1.3.2、ConstraintLayout 2.1.4
- Lifecycle：ViewModel / LiveData / Runtime 2.7.0
- Fragment 1.6.2、Activity 1.8.2、Preference 1.2.1
- **ZXing** `com.journeyapps:zxing-android-embedded:4.3.0`（扫码核心）
- **Gson** `com.google.code.gson:gson:2.10.1`（序列化）
- 测试：JUnit 4.13.2、Mockito 5.10.0、Espresso 3.5.1

## 常用命令（Windows PowerShell / cmd）

使用项目本地 Gradle 缓存（重要，避免污染全局）：

```powershell
# PowerShell
$env:GRADLE_USER_HOME='E:\AI\VinScanner\.gradle'
.\gradlew.bat testDebugUnitTest      # 运行单元测试
.\gradlew.bat assembleDebug          # 构建 debug APK
.\gradlew.bat installDebug          # 安装到已连接设备/模拟器
.\gradlew.bat lint                   # 代码检查
```

```cmd
:: cmd.exe
set GRADLE_USER_HOME=E:\AI\VinScanner\.gradle
.\gradlew.bat assembleDebug
```

调试 APK 产物：`app/build/outputs/apk/debug/app-debug.apk`

## 开发约定与注意事项

1. **新增功能前**：参考现有 MVVM 分层，数据走 `VinRepository` → `VinViewModel` → Activity/Fragment。
2. **持久化**：当前用 SharedPreferences + Gson，无 Room/数据库。如需大量数据请评估迁移。
3. **VIN 校验**：`VinValidator` 只做格式校验（17位、合法字符），**未实现第 9 位校验位算法**，如需要请扩展。
4. **去重**：`add` / `updateVinAt` / `containsExceptIndex` 已处理，新增入口注意复用。
5. **构建脚本**：根目录存在大量临时 PowerShell 脚本（`run_build*.ps1`、`run_debug.ps1`、`check_tasks.ps1` 等）及 `tmp/`、`.trae/` 目录，均为历史调试产物，**非项目正式组成部分**，可酌情清理。
6. **未跟踪文件**：`README.md`、`AGENTS.md`、各 `run_build*.ps1`、`.trae/` 当前未纳入 git。
7. **Gradle 缓存本地化**：务必设置 `GRADLE_USER_HOME` 指向项目内 `.gradle`，否则首次构建会下载大量依赖。
8. **提交规范**：历史提交为英文短句（如 "Add VIN edit flow and release 1.0.4"），请保持一致风格；仅在用户明确要求时才 commit/push。

## 资源结构

```
app/src/main/res/
├── layout/   activity_main / activity_scan / activity_vin_edit /
│             custom_barcode_scanner / dialog_input_vin / item_vin
├── values/   colors / strings / themes
├── menu/     menu_main
├── drawable/ 图标与矢量资源
└── mipmap-*/ 各密度启动图标（ic_launcher）
```

## AndroidManifest 要点

- 权限：`CAMERA`、`FLASHLIGHT`
- `uses-feature`：`android.hardware.camera` (required=true)
- Activity：`MainActivity`(exported, launcher) / `ScanActivity`(portrait) / `SettingsActivity` / `VinEditActivity`
- Application：`.VinScannerApp`（注册崩溃处理器）

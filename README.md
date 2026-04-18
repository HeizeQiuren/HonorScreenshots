# 王者截图 - Android应用

专为王者荣耀玩家设计的游戏截屏工具。

## 功能特性

### 1. 悬浮球系统
- 🎯 **快速截屏**: 点击悬浮球即可截取当前画面
- 📍 **自由拖拽**: 悬浮球可在屏幕任意位置移动
- 🔒 **边缘隐藏**: 拖动到屏幕边缘自动吸附隐藏，不影响游戏
- 🎨 **美观设计**: 电竞风格渐变背景

### 2. 权限管理
- 👁️ **悬浮窗权限**: 用于显示悬浮球
- 📁 **存储权限**: 用于保存截图到相册
- 📸 **截屏权限**: 用于截取游戏画面
- ✅ **直观展示**: 实时显示各权限授权状态

### 3. 文件管理
- 📂 **截图保存**: 自动保存到 `Pictures/HonorScreenshots` 目录
- 🖼️ **快速访问**: 一键打开截图文件夹
- 📊 **统计展示**: 显示已截取图片数量

## 项目结构

```
HonorScreenshots/
├── app/
│   ├── src/main/
│   │   ├── java/com/honorshots/screenshot/
│   │   │   ├── HonorScreenshotsApp.kt      # Application类
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt          # 主Activity
│   │   │   │   ├── MainScreen.kt           # 主界面UI
│   │   │   │   ├── MainViewModel.kt        # ViewModel
│   │   │   │   ├── PermissionState.kt      # 权限状态模型
│   │   │   │   └── theme/                  # Compose主题
│   │   │   └── service/
│   │   │       └── FloatBallService.kt     # 悬浮球服务
│   │   ├── res/
│   │   │   ├── drawable/                   # 矢量图标
│   │   │   ├── values/                     # 资源文件
│   │   │   └── mipmap-*/                   # 应用图标
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew
```

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose + Material 3
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **目标SDK**: 34 (Android 14)
- **最低SDK**: 26 (Android 8.0)

## 在Android Studio中打开项目

### 方法一：直接打开
1. 打开 Android Studio
2. 选择 `File` → `Open`
3. 选择项目根目录 `HonorScreenshots`
4. 等待 Gradle 同步完成

### 方法二：从命令行
```bash
cd HonorScreenshots
./gradlew assembleDebug
```

### 编译输出
编译成功后，APK文件位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

## 首次使用步骤

1. **安装应用**: 将APK传输到手机并安装
2. **授予权限**: 
   - 悬浮窗权限（必须）
   - 存储权限（必须）
   - 截屏权限（必须）
3. **开启悬浮球**: 在主界面打开悬浮球开关
4. **开始截屏**: 进入游戏，点击悬浮球即可截屏

## 权限说明

| 权限 | 用途 | 必要性 |
|------|------|--------|
| SYSTEM_ALERT_WINDOW | 显示悬浮球 | 必须 |
| READ/WRITE_EXTERNAL_STORAGE | 保存截图 | 必须 |
| MEDIA_PROJECTION | 截取屏幕 | 必须 |

## 截图保存位置

- **路径**: `内部存储/Pictures/HonorScreenshots/`
- **命名格式**: `王者截图_YYYYMMDD_HHmmss.png`
- **格式**: PNG无损压缩

## 常见问题

### Q: 悬浮球无法显示？
A: 请检查是否授予了悬浮窗权限

### Q: 点击悬浮球没有反应？
A: 请检查是否授予了截屏权限

### Q: 截图保存失败？
A: 请检查存储权限是否授予

### Q: 如何在游戏中隐藏悬浮球？
A: 将悬浮球拖动到屏幕左边缘或右边缘，会自动吸附隐藏

## 开发说明

### 添加新功能
1. 在 `service/FloatBallService.kt` 中添加服务逻辑
2. 在 `ui/MainScreen.kt` 中添加UI组件
3. 在 `ui/MainViewModel.kt` 中添加业务逻辑

### 注意事项
- 悬浮球使用 `TYPE_APPLICATION_OVERLAY` 窗口类型
- 截屏使用 `MediaProjection` API
- 需要处理 Android 6.0+ 运行时权限
- Android 10+ 使用 Scoped Storage

## 许可证

MIT License

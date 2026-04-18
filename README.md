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

### 4. 视频功能
- 📹 **视频文件夹设置**: 用户可选择视频保存位置
- 🎬 **视频文件夹管理**: 快速访问已设置的视频文件夹

### 5. AI与扩展功能
- 🤖 **AI分析**: 预留接口，支持后续扩展AI分析功能
- 📚 **搜索教程视频**: 一键跳转腾讯元宝AI对话页面
- 🎮 **赛前分析**: 阵容实时分析，支持识别英雄、评估优劣

### 6. 悬浮球高级功能
- 📊 **英雄讲解**: 显示英雄连招、出装、技巧
- 📈 **赛前分析**: 分析双方阵容的坦度、控制、开团、输出等
- 💡 **打法思路**: 提供全局打法和团战思路建议

## API接口文档

详细API接口说明请参阅 [README2.md](README2.md)

### 快速参考

#### ViewModel核心方法

| 方法 | 功能 | 位置 |
|------|------|------|
| `toggleFloatBall()` | 切换悬浮球开关 | MainViewModel.kt |
| `openScreenshotFolder()` | 打开截图文件夹 | MainViewModel.kt |
| `openVideoFolder()` | 打开视频文件夹 | MainViewModel.kt |
| `saveVideoFolderPath(path)` | 保存视频文件夹路径 | MainViewModel.kt |
| `performAIAnalysis()` | AI分析（预留） | MainViewModel.kt |
| `performPreMatchAnalysis()` | 赛前分析（预留） | MainViewModel.kt |
| `getFeatureAnalysisUrl()` | 获取教程视频URL | MainViewModel.kt |
| `getPreMatchAnalysisUrl()` | 获取赛前分析URL | MainViewModel.kt |

#### 侧边菜单功能栏

| 按钮名称 | 功能 |
|----------|------|
| 打开截图文件夹 | 打开截图保存目录 |
| 保存文件夹设置 | 修改截图保存路径 |
| 保存视频文件路径 | 选择视频文件夹 |
| 打开视频文件夹 | 打开已设置的视频文件夹 |
| 进行AI分析 | 预留AI分析功能 |
| 搜索教程视频 | 跳转腾讯元宝AI对话 |
| 赛前分析 | 预留赛前分析功能 |

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
│   │   │   │   ├── MainViewModel.kt        # ViewModel (API核心)
│   │   │   │   ├── PermissionState.kt      # 权限状态模型
│   │   │   │   └── theme/                  # Compose主题
│   │   │   ├── service/
│   │   │   │   ├── FloatBallService.kt     # 悬浮球服务
│   │   │   │   └── MatchAnalysisService.kt # 阵容分析服务
│   │   │   └── data/
│   │   │       ├── HeroData.kt            # 数据模型
│   │   │       └── HeroDatabase.kt        # 英雄数据库
│   │   ├── res/
│   │   │   ├── drawable/                   # 矢量图标
│   │   │   ├── layout/                     # 布局文件
│   │   │   ├── values/                     # 资源文件
│   │   │   └── mipmap-*/                   # 应用图标
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md                              # 主文档
├── README2.md                             # API接口文档
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
| POST_NOTIFICATIONS (Android 13+) | 发送通知 | 可选 |

## 截图/视频保存位置

- **截图路径**: `内部存储/Pictures/HonorScreenshots/`
- **截图命名**: `王者截图_YYYYMMDD_HHmmss.png`
- **截图格式**: PNG无损压缩
- **视频路径**: 用户自定义（通过"保存视频文件路径"设置）

## 数据持久化

应用使用 `SharedPreferences` 存储以下数据：

| 文件名 | Key | 用途 |
|--------|-----|------|
| `video_prefs` | `video_folder_path` | 视频文件夹路径 |

## 常见问题

### Q: 悬浮球无法显示？
A: 请检查是否授予了悬浮窗权限

### Q: 点击悬浮球没有反应？
A: 请检查是否授予了截屏权限

### Q: 截图保存失败？
A: 请检查存储权限是否授予

### Q: 如何在游戏中隐藏悬浮球？
A: 将悬浮球拖动到屏幕左边缘或右边缘，会自动吸附隐藏

### Q: 如何使用视频文件夹功能？
A: 在侧边菜单中点击"保存视频文件路径"，选择视频所在文件夹即可

### Q: 赛前分析功能如何使用？
A: 该功能为预留接口，点击后会跳转至腾讯元宝AI对话页面

## 开发说明

### 添加新功能

#### 1. 在 MainViewModel.kt 中添加业务逻辑

```kotlin
// MainViewModel.kt
fun myNewFunction(param: String) {
    viewModelScope.launch {
        // 业务逻辑
        showToast("功能正在开发中...")
    }
}
```

#### 2. 在 MainScreen.kt 中添加UI

```kotlin
// 添加回调参数
@Composable
fun DrawerContent(
    // ... 其他参数
    onMyNewFunction: () -> Unit
) {
    DrawerFunctionItem(
        title = "新功能",
        onClick = {
            onMyNewFunction()
            onCloseDrawer()
        }
    )
}
```

### 注意事项
- 悬浮球使用 `TYPE_APPLICATION_OVERLAY` 窗口类型
- 截屏使用 `MediaProjection` API
- 需要处理 Android 6.0+ 运行时权限
- Android 10+ 使用 Scoped Storage

### API扩展示例

```kotlin
// 1. 添加新方法到 MainViewModel
fun performPreMatchAnalysis() {
    viewModelScope.launch {
        // TODO: 实现赛前分析功能
        // - 读取用户视频文件
        // - 分析历史战绩
        // - 提供阵容建议
        showToast("赛前分析功能正在开发中...")
    }
}

// 2. 在 MainScreen 中添加回调
onPreMatchAnalysis = { viewModel.performPreMatchAnalysis() }

// 3. 在侧边菜单中添加入口
DrawerFunctionItem(
    icon = Icons.Default.Assessment,
    title = "赛前分析",
    onClick = {
        onPreMatchAnalysis()
        onCloseDrawer()
    }
)
```

## 许可证

MIT License

---

## 更新日志

### v2.0 (2026-04-18)
- ✨ 新增"赛前分析"功能按钮及API接口
- ✨ 新增视频文件夹管理功能
- ✨ 新增AI分析预留接口
- 🎨 优化UI界面，添加侧边菜单栏
- 🔗 更新功能分析跳转链接（腾讯元宝AI）

### v1.0 (初始版本)
- ✅ 基础截图功能
- ✅ 悬浮球系统
- ✅ 权限管理

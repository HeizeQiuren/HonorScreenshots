# 王者截图 - API接口文档 (v2.0)

本文档详细描述了"王者截图"应用的所有API接口，包括新增的"赛前分析"接口。

---

## 目录

1. [概述](#概述)
2. [ViewModel API 接口](#viewmodel-api-接口)
   - [截图相关](#截图相关)
   - [文件管理](#文件管理)
   - [权限管理](#权限管理)
   - [视频相关](#视频相关)
   - [AI功能](#ai功能)
   - [教程与赛前分析](#教程与赛前分析)
3. [状态接口](#状态接口)
4. [UI回调接口](#ui回调接口)
5. [扩展指南](#扩展指南)

---

## 概述

本应用采用 **MVVM 架构**，核心业务逻辑集中在 `MainViewModel.kt` 中。

### 文件位置

```
app/src/main/java/com/honorshots/screenshot/ui/
├── MainViewModel.kt      # 核心业务逻辑
├── MainScreen.kt         # UI界面
├── PermissionState.kt    # 状态模型
└── MainActivity.kt       # Activity
```

---

## ViewModel API 接口

所有接口均位于 `MainViewModel` 类中，可通过 `viewModel` 实例调用。

### 截图相关

#### `toggleFloatBall()`

切换悬浮球服务的开启/关闭状态。

```kotlin
viewModel.toggleFloatBall()
```

**功能说明**：
- 如果服务未运行，则启动 `FloatBallService`
- 如果服务已运行，则停止服务

**使用场景**：主界面悬浮球开关按钮

---

#### `updateScreenshotCount()`

更新截图数量统计。

```kotlin
viewModel.updateScreenshotCount()
```

**功能说明**：
- 扫描 `Pictures/HonorScreenshots` 目录
- 统计 PNG 文件数量
- 更新 UI 状态显示

**使用场景**：
- 应用启动时
- 截图成功后回调

---

#### `onScreenshotTaken(success: Boolean)`

截图完成后的回调处理。

```kotlin
viewModel.onScreenshotTaken(success = true)
```

**参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| `success` | `Boolean` | 截图是否成功 |

**功能说明**：
- 成功时：更新截图数量，显示成功提示
- 失败时：显示失败提示

---

### 文件管理

#### `openScreenshotFolder()`

打开截图保存文件夹。

```kotlin
viewModel.openScreenshotFolder()
```

**功能说明**：
- 使用系统文件管理器打开 `Pictures/HonorScreenshots` 目录
- 如果目录不存在，会提示用户

**使用场景**：侧边菜单"打开截图文件夹"按钮

---

#### `openSaveFolderSettings()`

打开保存文件夹设置页面。

```kotlin
viewModel.openSaveFolderSettings()
```

**功能说明**：
- 打开系统设置中的默认存储位置设置页面
- 允许用户修改截图保存路径

**使用场景**：侧边菜单"保存文件夹设置"按钮

---

#### `openVideoFolder()`

打开视频文件夹。

```kotlin
viewModel.openVideoFolder()
```

**功能说明**：
- 读取 SharedPreferences 中保存的视频文件夹路径
- 使用系统文件管理器打开该目录
- 如果未设置路径，显示提示

**前置条件**：需要先调用 `saveVideoFolderPath()` 保存路径

**使用场景**：侧边菜单"打开视频文件夹"按钮

---

### 权限管理

#### `checkPermissions()`

检查所有必需权限的状态。

```kotlin
viewModel.checkPermissions()
```

**检查的权限**：
| 权限 | 检查内容 |
|------|----------|
| 悬浮窗权限 | `Settings.canDrawOverlays()` |
| 存储权限 | `ContextCompat.checkSelfPermission()` |
| 截屏权限 | `ProjectionPermissionManager.hasValidPermission()` |

**使用场景**：
- 应用启动时
- 权限授予/撤销后

---

#### `hasAllRequiredPermissions(): Boolean`

检查是否拥有所有必需权限。

```kotlin
val hasAll = viewModel.hasAllRequiredPermissions()
```

**返回值**：`Boolean` - 是否所有权限都已授予

**使用场景**：决定是否允许启动悬浮球服务

---

### 视频相关

#### `saveVideoFolderPath(path: String)`

保存视频文件夹路径。

```kotlin
viewModel.saveVideoFolderPath("/storage/emulated/0/DCIM/视频")
```

**参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| `path` | `String` | 文件夹绝对路径 |

**功能说明**：
- 将路径保存到 `SharedPreferences`
- Key: `video_folder_path`
- File: `video_prefs`
- 更新 UI 状态显示

**使用场景**：侧边菜单"保存视频文件路径"按钮

---

#### `getVideoFolderPath(): String?`

获取已保存的视频文件夹路径。

```kotlin
val path = viewModel.getVideoFolderPath()
if (path != null) {
    // 使用路径
}
```

**返回值**：`String?` - 保存的路径，未设置则返回 `null`

**使用场景**：
- 读取已保存的设置
- 检查路径是否存在

---

### AI功能

#### `performAIAnalysis()`

执行AI分析功能（预留接口）。

```kotlin
viewModel.performAIAnalysis()
```

**功能说明**：
- 显示"AI分析功能正在开发中..."提示
- 预留接口，方便后续扩展

**可扩展功能**：
```kotlin
// TODO: 实现AI分析功能
// - 读取视频文件
// - 调用AI模型分析
// - 返回分析结果
```

**使用场景**：侧边菜单"进行AI分析"按钮

---

### 教程与赛前分析

#### `getFeatureAnalysisUrl(): String`

获取功能分析（搜索教程视频）的跳转URL。

```kotlin
val url = viewModel.getFeatureAnalysisUrl()
```

**返回值**：`String` - 跳转网址

**当前值**：
```
https://yuanqi.tencent.com/webim/#/chat/SbIwIL?appid=2043231099212303552&experience=true
```

**使用场景**：侧边菜单"搜索教程视频"按钮

---

#### `performPreMatchAnalysis()`

执行赛前分析功能（预留接口）。

```kotlin
viewModel.performPreMatchAnalysis()
```

**功能说明**：
- 显示"赛前分析功能正在开发中..."提示
- 预留接口，方便后续扩展

**可扩展功能**：
```kotlin
// TODO: 实现赛前分析功能
// - 读取用户视频文件
// - 分析历史战绩
// - 提供阵容建议
```

**使用场景**：侧边菜单"赛前分析"按钮

---

#### `getPreMatchAnalysisUrl(): String`

获取赛前分析的跳转URL。

```kotlin
val url = viewModel.getPreMatchAnalysisUrl()
```

**返回值**：`String` - 跳转网址

**当前值**：
```
https://yuanqi.tencent.com/webim/#/chat/SbIwIL?appid=2043231099212303552&experience=true
```

> ⚠️ TODO: 替换为实际的赛前分析网站地址

---

## 状态接口

### PermissionState

权限状态数据类，用于 UI 状态展示。

```kotlin
data class PermissionState(
    val hasOverlayPermission: Boolean = false,      // 悬浮窗权限
    val hasStoragePermission: Boolean = false,       // 存储权限
    val hasProjectionPermission: Boolean = false,    // 截屏权限
    val isServiceRunning: Boolean = false,           // 服务运行状态
    val screenshotCount: Int = 0,                    // 截图数量
    val toastMessage: String? = null,                 // 提示消息
    val videoFolderPath: String? = null               // 视频文件夹路径
)
```

---

## UI回调接口

### MainScreen.kt 回调列表

| 回调名称 | 参数 | 说明 |
|----------|------|------|
| `onOpenScreenshotFolder` | `() -> Unit` | 打开截图文件夹 |
| `onOpenSaveFolderSettings` | `() -> Unit` | 打开文件夹设置 |
| `onSelectVideoFolder` | `() -> Unit` | 选择视频文件夹 |
| `onOpenVideoFolder` | `() -> Unit` | 打开视频文件夹 |
| `onPerformAIAnalysis` | `() -> Unit` | 执行AI分析 |
| `onFeatureAnalysis` | `() -> Unit` | 搜索教程视频 |
| `onPreMatchAnalysis` | `() -> Unit` | 赛前分析 |

---

## 扩展指南

### 添加新功能示例

#### 1. 在 ViewModel 中添加方法

```kotlin
// MainViewModel.kt
fun myNewFunction(param: String) {
    viewModelScope.launch {
        // 业务逻辑
        showToast("功能正在开发中...")
    }
}
```

#### 2. 在 MainScreen 中添加回调

```kotlin
// 在 MainScreenWithViewModel 中添加
onMyNewFunction = { viewModel.myNewFunction("参数") }

// 在 DrawerContent 中添加参数
@Composable
fun DrawerContent(
    // ... 其他参数
    onMyNewFunction: () -> Unit
) {
    // 使用回调
    DrawerFunctionItem(
        title = "新功能",
        onClick = {
            onMyNewFunction()
            onCloseDrawer()
        }
    )
}
```

#### 3. 在主页面添加入口（可选）

```kotlin
// 在主页面 Column 中添加按钮
FunctionButton(
    icon = Icons.Default.New,
    text = "新功能",
    onClick = { onMyNewFunction() }
)
```

---

### 数据持久化

应用使用 `SharedPreferences` 存储数据：

| 文件名 | 用途 |
|--------|------|
| `video_prefs` | 保存视频文件夹路径 |

```kotlin
// 保存数据
val prefs = context.getSharedPreferences("video_prefs", Context.MODE_PRIVATE)
prefs.edit().putString("video_folder_path", path).apply()

// 读取数据
val path = prefs.getString("video_folder_path", null)
```

---

## 更新日志

### v2.0 (2026-04-18)
- 新增"赛前分析"功能按钮及API接口
- 新增视频文件夹管理功能
- 新增AI分析预留接口
- 优化UI界面，添加侧边菜单栏
- 更新功能分析跳转链接

### v1.0 (初始版本)
- 基础截图功能
- 悬浮球系统
- 权限管理

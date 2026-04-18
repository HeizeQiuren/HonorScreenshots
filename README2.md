# 王者截图 - API接口文档 (v2.1)

本文档详细描述了"王者截图"应用的所有API接口，包括新增的"赛前分析"接口和阵容分析服务。

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
5. [阵容分析服务](#阵容分析服务)
6. [数据模型](#数据模型)
7. [扩展指南](#扩展指南)

---

## 概述

本应用采用 **MVVM 架构**，核心业务逻辑集中在 `MainViewModel.kt` 中。

### 文件位置

```
app/src/main/java/com/honorshots/screenshot/
├── ui/
│   ├── MainViewModel.kt      # 核心业务逻辑
│   ├── MainScreen.kt         # UI界面
│   ├── PermissionState.kt    # 状态模型
│   └── MainActivity.kt       # Activity
├── service/
│   ├── FloatBallService.kt       # 悬浮球服务
│   └── MatchAnalysisService.kt    # 阵容分析服务
└── data/
    ├── HeroData.kt           # 数据模型
    └── HeroDatabase.kt       # 英雄数据库
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

## 阵容分析服务

### MatchAnalysisService

阵容分析服务类，负责分析王者荣耀对局中的阵容优劣。

#### 文件位置
```
app/src/main/java/com/honorshots/screenshot/service/MatchAnalysisService.kt
```

#### 核心方法

##### `analyzeMatch()`

分析对局阵容。

```kotlin
fun analyzeMatch(
    blueTeamHeroes: List<String>,    // 蓝方英雄名称列表（5个）
    redTeamHeroes: List<String>,      // 红方英雄名称列表（5个）
    blueTeamRanks: List<String> = List(5) { "钻石" },  // 蓝方段位
    redTeamRanks: List<String> = List(5) { "钻石" }    // 红方段位
): MatchAnalysis
```

**返回值**：`MatchAnalysis` - 对局分析结果

**功能说明**：
1. 解析双方英雄数据
2. 智能分配分路位置
3. 分析各项能力指标
4. 生成克制关系
5. 计算胜率评估
6. 生成取胜关键点

**使用示例**：
```kotlin
val analysis = MatchAnalysisService.analyzeMatch(
    blueTeamHeroes = listOf("猪八戒", "娜可露露", "不知火舞", "后羿", "牛魔"),
    redTeamHeroes = listOf("铠", "孙悟空", "安琪拉", "马可波罗", "张飞")
)

// 获取分析结果
println("蓝方胜率: ${(analysis.winProbability * 100).toInt()}%")
println("预估时长: ${analysis.estimatedDuration}")
```

---

##### `generateDemoAnalysis()`

生成演示分析数据（用于测试）。

```kotlin
fun generateDemoAnalysis(): MatchAnalysis
```

**返回值**：`MatchAnalysis` - 演示用分析结果

---

### 分析能力指标

MatchAnalysisService 会计算以下能力指标：

| 指标 | 说明 | 计算方式 |
|------|------|----------|
| 坦度 (tankiness) | 阵容承伤能力 | 英雄坦度属性之和 |
| 控制 (control) | 控制技能数量 | 英雄控制属性之和 |
| 爆发 (burstDamage) | 瞬间伤害输出 | 英雄爆发属性之和 |
| 持续 (sustainedDamage) | 持续伤害输出 | 英雄持续伤害属性之和 |
| 前期 (earlyGameStrength) | 前期作战能力 | 英雄前期属性之和 |
| 后期 (lateGameStrength) | 后期作战能力 | 英雄后期属性之和 |
| 开团 (initiationAbility) | 开团先手能力 | 控制+坦克数量计算 |
| 保护 (peelAbility) | 保护后排能力 | 辅助+坦克+控制计算 |
| 推塔 (pushAbility) | 推塔速度 | 英雄推塔属性之和 |
| 团战 (teamFightStrength) | 团战胜负能力 | 综合计算 |

---

### 取胜关键点

分析服务会生成以下类型的关键点：

1. **坦度对比**：双方前排硬度对比
2. **控制对比**：控制技能数量对比
3. **前期对比**：前期强势程度对比
4. **后期对比**：后期强势程度对比
5. **开团能力**：开团与保护能力对比

---

## 数据模型

### Hero

英雄数据模型。

```kotlin
data class Hero(
    val id: String,                    // 英雄ID
    val name: String,                  // 英雄名称
    val role: String,                  // 职业定位
    val lane: Lane,                    // 推荐分路
    val isTank: Boolean = false,       // 是否为坦克
    val isFighter: Boolean = false,    // 是否为战士
    val isMage: Boolean = false,       // 是否为法师
    val isAssassin: Boolean = false,   // 是否为刺客
    val isMarksman: Boolean = false,   // 是否为射手
    val isSupport: Boolean = false,     // 是否为辅助
    val controlAbility: Int = 0,       // 控制能力 (1-5)
    val burstDamage: Int = 0,          // 爆发伤害 (1-5)
    val sustainedDamage: Int = 0,      // 持续伤害 (1-5)
    val tankiness: Int = 0,            // 坦度 (1-5)
    val earlyGameStrength: Int = 0,    // 前期强度 (1-5)
    val lateGameStrength: Int = 0,     // 后期强度 (1-5)
    val mobility: Int = 0,              // 机动性 (1-5)
    val pushAbility: Int = 0            // 推线能力 (1-5)
)
```

---

### Lane

分路枚举。

```kotlin
enum class Lane(val displayName: String) {
    TOP("对抗路"),
    JUNGLE("打野"),
    MID("中路"),
    ADC("发育路"),
    SUPPORT("辅助"),
    UNKNOWN("未知")
}
```

---

### Team

队伍枚举。

```kotlin
enum class Team(val displayName: String) {
    BLUE("蓝方"),
    RED("红方")
}
```

---

### Player

玩家数据模型。

```kotlin
data class Player(
    val hero: Hero,       // 所用英雄
    val rank: String,      // 段位
    val lane: Lane,       // 当前分路
    val team: Team        // 所属队伍
)
```

---

### TeamAnalysis

队伍分析结果。

```kotlin
data class TeamAnalysis(
    val team: Team,                        // 队伍
    val players: List<Player>,             // 队员列表
    val totalTankiness: Int,               // 总坦度
    val totalControl: Int,                  // 总控制能力
    val totalBurstDamage: Int,             // 总爆发伤害
    val totalSustainedDamage: Int,         // 总持续伤害
    val earlyGameStrength: Int,             // 前期强度
    val lateGameStrength: Int,             // 后期强度
    val initiationAbility: Int,             // 开团能力
    val peelAbility: Int,                  // 保护能力
    val pushAbility: Int,                  // 推塔能力
    val teamFightStrength: Int,            // 团战强度
    val strengths: List<String>,           // 优势点
    val weaknesses: List<String>,          // 劣势点
    val overallStrength: Int,              // 整体强度 1-10
    val recommendedStrategy: String         // 推荐策略
)
```

---

### MatchAnalysis

对局分析结果。

```kotlin
data class MatchAnalysis(
    val blueTeam: TeamAnalysis,                    // 蓝方分析
    val redTeam: TeamAnalysis,                     // 红方分析
    val counterAnalyses: List<CounterAnalysis>,   // 克制关系分析
    val keyFactors: List<String>,                 // 取胜关键点
    val recommendedPlaystyle: String,              // 推荐打法
    val warnings: List<String>,                  // 需要注意的点
    val winProbability: Float,                    // 胜率估算 (0-1)
    val estimatedDuration: String,                 // 预估时长
    val generatedTime: Long = System.currentTimeMillis()
)
```

---

### CounterAnalysis

克制关系分析。

```kotlin
data class CounterAnalysis(
    val heroName: String,           // 英雄名称
    val counteredBy: List<String>,   // 被谁克制
    val counters: List<String>,      // 克制谁
    val synergyPartners: List<String> // 最佳搭档
)
```

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

#### 4. 在悬浮球菜单中添加按钮

```xml
<!-- res/layout/float_ball_menu.xml -->
<LinearLayout
    android:id="@+id/btn_new_feature"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:background="?android:attr/selectableItemBackground"
    android:paddingHorizontal="8dp">

    <ImageView
        android:id="@+id/icon_new_feature"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:src="@drawable/ic_new_feature" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="12dp"
        android:text="新功能"
        android:textColor="#333333"
        android:textSize="14sp" />
</LinearLayout>
```

```kotlin
// FloatBallService.kt
menuView.findViewById<LinearLayout>(R.id.btn_new_feature).apply {
    setOnClickListener {
        dismiss()
        // 执行新功能
    }
}
```

---

### 数据持久化

应用使用 `SharedPreferences` 存储数据：

| 文件名 | 用途 |
|--------|------|
| `video_prefs` | 保存视频文件夹路径 |
| `float_ball_prefs` | 保存悬浮球设置（如透明度） |

```kotlin
// 保存数据
val prefs = context.getSharedPreferences("video_prefs", Context.MODE_PRIVATE)
prefs.edit().putString("video_folder_path", path).apply()

// 读取数据
val path = prefs.getString("video_folder_path", null)
```

---

### 添加新英雄

在 `HeroDatabase.kt` 中添加新英雄数据：

```kotlin
"新英雄" to Hero(
    id = "xxx",
    name = "新英雄",
    role = "战士",
    lane = Lane.TOP,
    isFighter = true,
    tankiness = 3,
    controlAbility = 3,
    earlyGameStrength = 3,
    lateGameStrength = 3,
    // ... 其他属性
)
```

---

## 更新日志

### v2.1 (2026-04-18)
- ✨ 新增赛前分析悬浮窗功能
- ✨ 新增 MatchAnalysisService 阵容分析服务
- ✨ 新增 HeroData.kt 数据模型
- ✨ 新增 HeroDatabase.kt 英雄数据库
- ✨ 实现阵容分析：坦度、控制、开团、输出等指标
- ✨ 实现胜率评估与取胜关键点生成
- ✨ 新增打法思路悬浮窗

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

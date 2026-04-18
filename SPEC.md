# 王者截图 - 游戏截屏助手

## 1. Project Overview

**Project Name**: 王者截图 (HonorScreenshots)
**Project Type**: Android Native Application
**Core Functionality**: 一款专为王者荣耀玩家设计的截屏工具，通过悬浮球实现游戏过程中快速截屏，保存精彩瞬间。

## 2. Technology Stack & Choices

### Framework & Language
- **Language**: Kotlin 1.9.x
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

### Key Libraries/Dependencies
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (简化依赖注入)
- **Coroutines**: Kotlin Coroutines + Flow
- **Storage**: Scoped Storage API (MediaStore)
- **Permissions**: Accompanist Permissions

### State Management
- ViewModel + StateFlow for UI state
- Service for floating ball state

### Architecture Pattern
- **Presentation Layer**: Compose UI + ViewModel
- **Domain Layer**: Use Cases
- **Data Layer**: Repository Pattern

## 3. Feature List

### Core Features
1. **悬浮球系统**
   - 悬浮球显示/隐藏切换
   - 悬浮球可拖拽定位
   - 点击截屏并保存
   - 最小化到屏幕边缘
   - 后台服务常驻

2. **截屏功能**
   - 截取当前屏幕内容
   - 自动保存到指定目录
   - 文件命名: "王者截图_YYYYMMDD_HHmmss.png"
   - Toast提示保存结果

3. **权限管理**
   - 悬浮窗权限检测与申请
   - 存储权限检测与申请
   - 直观展示授权状态
   - 一键跳转授权设置

4. **文件管理**
   - 截图保存目录: `Pictures/HonorScreenshots`
   - 打开截图文件夹快捷入口
   - 显示截图数量统计

## 4. UI/UX Design Direction

### Overall Visual Style
- **Material Design 3** with gaming aesthetic
- 深色主题为主 (适配游戏场景)
- 渐变色高亮 (紫色/蓝色电竞风格)

### Color Scheme
- **Primary**: #6366F1 (Indigo电竞紫)
- **Secondary**: #8B5CF6 (紫色渐变)
- **Background**: #0F172A (深蓝黑)
- **Surface**: #1E293B (深灰蓝)
- **Accent**: #22D3EE (青色高亮)

### Layout Approach
- **单页面设计** + 悬浮球overlay
- 主界面包含：
  - 顶部状态栏(授权状态指示)
  - 快捷功能卡片区
  - 悬浮球开关控制
  - 底部截图统计

### Interaction Design
- 悬浮球: 支持拖拽、长按菜单
- 主界面: 卡片点击反馈、权限状态实时更新
- 动画: 悬浮球出现/隐藏动画、按钮涟漪效果

---
name: fix-java-compilation
overview: 修复Android项目Java版本兼容性问题，将Java 17降级为Java 11以适配更广泛的JDK环境
todos:
  - id: fix-app-build-gradle
    content: 修改app/build.gradle.kts，将Java版本从17降级到11
    status: completed
  - id: fix-root-build-gradle
    content: 修改根build.gradle.kts，降级Android Gradle Plugin到8.0.2
    status: completed
  - id: fix-gradle-wrapper
    content: 修改gradle-wrapper.properties，降级Gradle到8.0
    status: completed
  - id: clean-rebuild
    content: 清理项目并重新编译验证
    status: completed
    dependencies:
      - fix-app-build-gradle
      - fix-root-build-gradle
      - fix-gradle-wrapper
---

## 问题描述

用户运行Android项目时遇到编译错误，错误信息：`error: cannot access 'jdk.compiler'`。这是Java版本不兼容导致的编译失败。

## 错误分析

- 项目配置了Java 17 (`JavaVersion.VERSION_17`)
- Android Gradle Plugin 8.2.0需要JDK 17
- 用户Android Studio使用的JDK版本低于17或模块访问受限
- Kapt编译器与Java 17存在兼容性问题

## 修复目标

将项目Java版本降级为Java 11（最广泛兼容的版本），同时调整相关Gradle插件版本以确保兼容性。

## 技术方案

### 问题根因

1. **Java版本不匹配**：项目配置Java 17，但用户环境可能使用Java 11或更低版本
2. **Kapt兼容性问题**：Kapt与Java 17在某些环境下存在模块访问限制
3. **Android Gradle Plugin版本过高**：8.2.0对JDK要求严格

### 解决方案

将Java版本从17降级到11，同时调整相关配置：

1. **修改app/build.gradle.kts**：

- Java版本从VERSION_17改为VERSION_11
- jvmTarget从"17"改为"11"
- 保持其他配置不变

2. **修改根build.gradle.kts**：

- Android Gradle Plugin从8.2.0降级到8.0.2（兼容Java 11）
- Kotlin版本保持1.9.20
- Hilt版本保持2.48.1

3. **修改gradle-wrapper.properties**：

- Gradle版本从8.2改为8.0（兼容AGP 8.0.2）

### 兼容性说明

- Java 11是Android开发的稳定版本，兼容性最好
- AGP 8.0.2 + Gradle 8.0 + Java 11是经过验证的稳定组合
- 所有Compose和Hilt功能在Java 11下完全支持
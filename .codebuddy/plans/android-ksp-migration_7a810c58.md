---
name: android-ksp-migration
overview: 将项目从 kapt 迁移到 KSP，解决 JDK 17+ 兼容性问题
todos:
  - id: add-ksp-plugin
    content: 在根 build.gradle.kts 添加 KSP 插件配置
    status: completed
  - id: replace-kapt-with-ksp
    content: 修改 app/build.gradle.kts：将 kapt 替换为 ksp
    status: completed
    dependencies:
      - add-ksp-plugin
  - id: cleanup-gradle-properties
    content: 清理 gradle.properties 中的 JVM 参数
    status: completed
    dependencies:
      - add-ksp-plugin
---

## 用户需求

修复构建错误：JDK 17+ 与 kapt 不兼容，错误信息为 `class org.jetbrains.kotlin.kapt3.base.javac.KaptJavaCompiler cannot access class com.sun.tools.javac.main.JavaCompiler`。

## 解决方案

将项目从 kapt 迁移到 KSP（Kotlin Symbol Processing）。KSP 完全兼容 JDK 17+，性能更好，是 Hilt 官方推荐的现代方案。

## 核心修改内容

1. 在根 `build.gradle.kts` 添加 KSP 插件
2. 在 `app/build.gradle.kts` 中将 `kapt` 替换为 `ksp`
3. 将 Hilt 注解处理器从 `kapt` 改为 `ksp`
4. 清理 gradle.properties 中之前添加的 JVM 参数

## 技术方案

### KSP 迁移

KSP (Kotlin Symbol Processing) 是 Kotlin 官方推出的注解处理工具，比 kapt 更快速且完全兼容 JDK 17+。

### 需要修改的文件

| 文件 | 修改内容 |
| --- | --- |
| `build.gradle.kts` | 添加 KSP 插件版本 `1.9.20-1.0.14` |
| `app/build.gradle.kts` | 移除 `kapt` 插件，添加 `ksp` 插件；将 `kapt("...")` 改为 `ksp("...")` |
| `gradle.properties` | 清理之前添加的多行 JVM 参数（不再需要） |
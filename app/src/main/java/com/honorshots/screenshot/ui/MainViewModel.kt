package com.honorshots.screenshot.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.honorshots.screenshot.service.FloatBallService
import com.honorshots.screenshot.service.ProjectionPermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(PermissionState())
    val state: StateFlow<PermissionState> = _state.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    init {
        checkPermissions()
        updateScreenshotCount()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            val hasOverlay = Settings.canDrawOverlays(application)
            val hasStorage = checkStoragePermission()
            val isServiceRunning = FloatBallService.isRunning.value

            // 使用 ProjectionPermissionManager 检查权限
            val hasProjection = ProjectionPermissionManager.hasValidPermission()

            _state.update { current ->
                current.copy(
                    hasOverlayPermission = hasOverlay,
                    hasStoragePermission = hasStorage,
                    hasProjectionPermission = hasProjection,
                    isServiceRunning = isServiceRunning
                )
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true // Android 13+ 使用 MediaStore，无需明确权限
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Android 10+ 使用 Scoped Storage
        } else {
            val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            application.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${application.packageName}")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
    }

    fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${application.packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        }
    }

    fun toggleFloatBallService() {
        val hasAllPermissions = _state.value.hasOverlayPermission && 
                _state.value.hasStoragePermission && 
                ProjectionPermissionManager.hasValidPermission()

        if (!hasAllPermissions) {
            showToast("请先授予所有权限")
            return
        }

        val intent = Intent(application, FloatBallService::class.java)
        if (_state.value.isServiceRunning) {
            application.stopService(intent)
        } else {
            application.startService(intent)
        }
        
        handler.postDelayed({
            checkPermissions()
        }, 500)
    }

    fun openScreenshotFolder() {
        viewModelScope.launch {
            val folder = getScreenshotFolder()
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse("file://${folder.absolutePath}"), "resource/folder")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                application.startActivity(Intent.createChooser(intent, "打开文件夹").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                // 如果无法打开，显示文件路径
                showToast("截图保存在: ${folder.absolutePath}")
            }
        }
    }

    fun openSaveFolderSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${application.packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        }
    }

    fun getScreenshotFolder(): File {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val honorShotsDir = File(picturesDir, "HonorScreenshots")
        if (!honorShotsDir.exists()) {
            honorShotsDir.mkdirs()
        }
        return honorShotsDir
    }

    fun updateScreenshotCount() {
        viewModelScope.launch {
            val count = getScreenshotFolder().listFiles()?.count { it.extension == "png" } ?: 0
            _state.update { it.copy(screenshotCount = count) }
        }
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _state.update { it.copy(toastMessage = message) }
            delay(2000)
            _state.update { it.copy(toastMessage = null) }
        }
    }

    fun setProjectionPermissionGranted() {
        // 直接设置截屏权限状态
        _state.update { it.copy(hasProjectionPermission = true) }
    }

    fun onScreenshotTaken(success: Boolean) {
        if (success) {
            updateScreenshotCount()
            showToast("截图已保存到 Pictures/HonorScreenshots")
        } else {
            showToast("截图失败，请重试")
        }
    }

    // ==================== 新增功能接口 ====================

    /**
     * API接口：保存视频文件夹路径
     * 用于后续扩展功能读取视频文件
     * @param path 文件夹路径
     */
    fun saveVideoFolderPath(path: String) {
        viewModelScope.launch {
            val prefs = application.getSharedPreferences("video_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("video_folder_path", path).apply()
            _state.update { it.copy(videoFolderPath = path) }
            showToast("视频文件夹已设置: $path")
        }
    }

    /**
     * 获取已保存的视频文件夹路径
     * @return 保存的路径，如果未设置则返回null
     */
    fun getVideoFolderPath(): String? {
        val prefs = application.getSharedPreferences("video_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("video_folder_path", null)
    }

    /**
     * 打开视频文件夹
     * 如果未设置路径，显示提示
     */
    fun openVideoFolder() {
        val path = getVideoFolderPath()
        if (path == null) {
            showToast("请先设置视频文件夹路径")
            return
        }
        
        val folder = File(path)
        if (!folder.exists()) {
            showToast("视频文件夹不存在: $path")
            return
        }
        
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse("file://$path"), "resource/folder")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            application.startActivity(Intent.createChooser(intent, "打开视频文件夹").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            showToast("无法打开文件夹: $path")
        }
    }

    /**
     * API接口：进行AI分析
     * 点击后在应用内部打开指定的AI分析链接
     */
    fun performAIAnalysis() {
        viewModelScope.launch {
            // 在应用内打开AI分析链接
            openAIAnalysisUrl()
        }
    }

    /**
     * 打开AI分析URL
     * 使用应用内浏览器打开指定链接
     */
    fun openAIAnalysisUrl() {
        viewModelScope.launch {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AI_ANALYSIS_URL)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                application.startActivity(intent)
            } catch (e: Exception) {
                showToast("无法打开链接，请检查网络")
            }
        }
    }

    companion object {
        // AI分析链接地址
        const val AI_ANALYSIS_URL = "http://110.40.192.112"
    }

    init {
        // 加载已保存的视频文件夹路径
        _state.update { it.copy(videoFolderPath = getVideoFolderPath()) }
    }
}

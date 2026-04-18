package com.honorshots.screenshot.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 共享的投影权限状态管理器
 * 
 * 关键设计：MediaProjection 只能在授权后立即创建一次
 * Service 和 MainActivity 在同一进程，可以共享 resultCode 和 resultData
 */
object ProjectionPermissionManager {
    
    private const val TAG = "ProjectionMgr"
    
    private val _hasProjectionPermission = MutableStateFlow(false)
    val hasProjectionPermission: StateFlow<Boolean> = _hasProjectionPermission.asStateFlow()
    
    // 权限数据存储（授权后由 MainActivity 设置，Service 使用）
    var resultCode: Int = Activity.RESULT_CANCELED
        private set
    var resultData: Intent? = null
        private set
    
    // MediaProjection 实例（只创建一次）
    private var storedProjection: MediaProjection? = null
    
    /**
     * 设置投影权限授予状态
     * 在 MainActivity 的授权回调中调用
     */
    fun setProjectionGranted(code: Int, data: Intent?) {
        Log.d(TAG, "setProjectionGranted: code=$code, data=${data != null}")
        
        resultCode = code
        resultData = data
        
        val isValid = code == Activity.RESULT_OK && data != null
        Log.d(TAG, "isValid = $isValid")
        
        _hasProjectionPermission.value = isValid
    }
    
    /**
     * 创建并获取 MediaProjection
     * 在 FloatBallService 中调用（与 MainActivity 同一进程）
     */
    fun createAndGetMediaProjection(context: Context): MediaProjection? {
        if (storedProjection != null) {
            Log.d(TAG, "Using existing MediaProjection")
            return storedProjection
        }
        
        if (resultCode == Activity.RESULT_CANCELED || resultData == null) {
            Log.w(TAG, "No valid permission data")
            return null
        }
        
        try {
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            storedProjection = projectionManager.getMediaProjection(resultCode, resultData!!)
            Log.d(TAG, "MediaProjection created successfully")
            return storedProjection
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create MediaProjection: ${e.message}")
            _hasProjectionPermission.value = false
            return null
        }
    }
    
    /**
     * 获取 MediaProjection（如果已创建）
     */
    fun getMediaProjection(): MediaProjection? {
        return storedProjection
    }
    
    /**
     * 检查权限是否有效
     */
    fun hasValidPermission(): Boolean {
        return _hasProjectionPermission.value
    }
    
    /**
     * 清除权限状态
     */
    fun clear() {
        try {
            storedProjection?.stop()
        } catch (e: Exception) {
            // 忽略
        }
        storedProjection = null
        resultCode = Activity.RESULT_CANCELED
        resultData = null
        _hasProjectionPermission.value = false
    }
}

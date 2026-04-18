package com.honorshots.screenshot.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.honorshots.screenshot.service.FloatBallService
import com.honorshots.screenshot.service.ProjectionPermissionManager
import com.honorshots.screenshot.ui.theme.Background
import com.honorshots.screenshot.ui.theme.HonorScreenshotsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // 截图计数更新广播接收器
    private val screenshotUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                FloatBallService.ACTION_SCREENSHOT_COUNT_UPDATED -> {
                    Log.d(TAG, "Received screenshot update broadcast")
                    // 通知 ViewModel 更新截图计数
                    viewModelForScreenshotUpdate?.updateScreenshotCount()
                }
            }
        }
    }

    // ViewModel 引用，用于更新截图计数
    private var viewModelForScreenshotUpdate: MainViewModel? = null

    private val projectionPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, ">>> Permission callback: resultCode=${result.resultCode}, data=${result.data != null}")
        
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            Log.d(TAG, ">>> Permission granted")
            
            // 保存权限数据
            ProjectionPermissionManager.setProjectionGranted(result.resultCode, result.data)
            
            // 启动悬浮球服务，让它在同一个进程中创建 MediaProjection
            val serviceIntent = Intent(this, FloatBallService::class.java).apply {
                action = FloatBallService.ACTION_INIT_PROJECTION
            }
            startService(serviceIntent)
            
            Toast.makeText(this, "截屏权限已获取", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, ">>> Permission denied or cancelled")
            Toast.makeText(this, "截屏权限被拒绝", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Log.d(TAG, ">>> onCreate: hasPermission=${ProjectionPermissionManager.hasValidPermission()}")

        // 注册广播接收器
        val filter = IntentFilter(FloatBallService.ACTION_SCREENSHOT_COUNT_UPDATED)
        registerReceiver(screenshotUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)

        setContent {
            HonorScreenshotsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    MainScreenWithViewModel(
                        onRequestProjectionPermission = { requestProjectionPermission() },
                        onViewModelReady = { vm ->
                            viewModelForScreenshotUpdate = vm
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到 App 时刷新截图数量
        viewModelForScreenshotUpdate?.updateScreenshotCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenshotUpdateReceiver)
        } catch (e: Exception) {
            // 忽略未注册的异常
        }
    }

    private fun requestProjectionPermission() {
        try {
            Log.d(TAG, ">>> requestProjectionPermission called")
            val projectionManager = getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE)
                    as android.media.projection.MediaProjectionManager
            projectionPermissionLauncher.launch(projectionManager.createScreenCaptureIntent())
        } catch (e: Exception) {
            Log.e(TAG, ">>> Failed to request permission: ${e.message}")
            Toast.makeText(this, "无法请求截屏权限: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

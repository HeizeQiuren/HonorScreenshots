package com.honorshots.screenshot.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.honorshots.screenshot.R
import com.honorshots.screenshot.ui.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FloatBallService : Service() {

    companion object {
        private const val TAG = "FloatBallService"
        
        const val ACTION_SCREENSHOT = "com.honorshots.screenshot.ACTION_SCREENSHOT"
        const val ACTION_INIT_PROJECTION = "com.honorshots.screenshot.ACTION_INIT_PROJECTION"
        const val ACTION_SCREENSHOT_COUNT_UPDATED = "com.honorshots.screenshot.ACTION_SCREENSHOT_COUNT_UPDATED"
        const val EXTRA_SCREENSHOT_PATH = "screenshot_path"
        const val CHANNEL_ID = "float_ball_channel"
        const val CHANNEL_ID_SCREENSHOT = "screenshot_channel"
        const val NOTIFICATION_ID = 1001
        const val SCREENSHOT_NOTIFICATION_ID = 1002
        
        // 服务运行状态
        val isRunning = kotlinx.coroutines.flow.MutableStateFlow(false)
    }

    private var windowManager: WindowManager? = null
    private var floatBallView: FrameLayout? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    
    // 菜单相关
    private var menuPopupWindow: PopupWindow? = null
    private var currentOpacity = 100
    
    // 透明度偏好设置
    private val prefs by lazy {
        getSharedPreferences("float_ball_prefs", Context.MODE_PRIVATE)
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    
    // 标记是否已初始化截图环境
    private var screenshotInitialized = false
    private var isCapturing = false  // 防止并发截图
    
    // MediaProjection 回调（Android 11+ 必须注册）
    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Log.d(TAG, "MediaProjection stopped by user")
            releaseResources()
        }
    }
    
    // VirtualDisplay 回调
    private val virtualDisplayCallback = object : VirtualDisplay.Callback() {
        override fun onPaused() {
            Log.d(TAG, "VirtualDisplay paused")
        }
        
        override fun onResumed() {
            Log.d(TAG, "VirtualDisplay resumed")
        }
        
        override fun onStopped() {
            Log.d(TAG, "VirtualDisplay stopped")
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isDragging = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var screenWidth = 0
    private var screenHeight = 0
    private var densityDpi = 0

    // MediaProjection 是否已初始化
    private var projectionInitialized = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, ">>> onCreate called")
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        // 加载保存的透明度
        currentOpacity = prefs.getInt("opacity", 100)
        
        createFloatBall()
        
        isRunning.value = true
        
        Log.d(TAG, ">>> FloatBall created, checking permission...")
        
        // 检查是否已有权限，如果有则初始化投影
        if (ProjectionPermissionManager.hasValidPermission()) {
            Log.d(TAG, ">>> Permission already granted, initializing projection")
            initProjection()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, ">>> onStartCommand: action=${intent?.action}, flags=$flags, startId=$startId")
        
        when (intent?.action) {
            ACTION_SCREENSHOT -> {
                Log.d(TAG, "ACTION_SCREENSHOT received")
                takeScreenshot()
            }
            ACTION_INIT_PROJECTION -> {
                Log.d(TAG, "ACTION_INIT_PROJECTION received")
                initProjection()
            }
            GameScreenshotService.ACTION_AUTO_SCREENSHOT -> {
                Log.d(TAG, "ACTION_AUTO_SCREENSHOT received (game detected)")
                takeScreenshot()
            }
        }
        return START_STICKY
    }

    /**
     * 初始化投影：在 Service 中创建 MediaProjection（与 MainActivity 同一进程）
     */
    private fun initProjection() {
        if (!ProjectionPermissionManager.hasValidPermission()) {
            Log.w(TAG, "No valid permission")
            return
        }
        
        // 如果已经初始化过截图环境，跳过
        if (screenshotInitialized && virtualDisplay != null) {
            Log.d(TAG, "Screenshot already initialized, reusing")
            return
        }
        
        // 在 Service 中创建 MediaProjection
        mediaProjection = ProjectionPermissionManager.createAndGetMediaProjection(this)
        if (mediaProjection != null) {
            // Android 11+ 必须注册 callback
            mediaProjection?.registerCallback(mediaProjectionCallback, handler)
            
            // 创建 ImageReader（只创建一次）
            setupImageReader()
            
            // 创建 VirtualDisplay（只创建一次）
            try {
                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "HonorScreenshot",
                    screenWidth,
                    screenHeight,
                    densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface,
                    virtualDisplayCallback,
                    handler
                )
                screenshotInitialized = true
                projectionInitialized = true
                Log.d(TAG, "Screenshot environment initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create VirtualDisplay: ${e.message}")
            }
        } else {
            Log.e(TAG, "Failed to initialize projection!")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning.value = false
        removeFloatBall()
        releaseResources()
        ProjectionPermissionManager.clear()
    }

    private fun createNotificationChannel() {
        // 悬浮球服务通知渠道
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.float_ball_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "悬浮球服务通知"
            setShowBadge(false)
        }
        
        // 截图成功通知渠道（使用较高优先级确保可见）
        val screenshotChannel = NotificationChannel(
            CHANNEL_ID_SCREENSHOT,
            "截图通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "截图成功通知"
            enableVibration(true)
            setShowBadge(true)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        notificationManager.createNotificationChannel(screenshotChannel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.float_ball_notification_title))
            .setContentText(getString(R.string.float_ball_notification_text))
            .setSmallIcon(R.drawable.ic_screenshot)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createFloatBall() {
        // 如果已经存在，先移除
        if (floatBallView != null) {
            Log.d(TAG, "FloatBall already exists, removing first...")
            removeFloatBall()
        }
        
        // 获取屏幕尺寸
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        densityDpi = displayMetrics.densityDpi
        
        Log.d(TAG, "Screen: ${screenWidth}x${screenHeight}, density=$densityDpi")

        // 创建悬浮球容器
        floatBallView = FrameLayout(this).apply {
            // 背景圆
            setBackgroundResource(R.drawable.float_ball_background)
            
            // 设置透明度
            alpha = currentOpacity / 100f

            // 相机图标
            val iconView = ImageView(this@FloatBallService).apply {
                setImageResource(R.drawable.ic_float_ball_camera)
                layoutParams = FrameLayout.LayoutParams(
                    dpToPx(32),
                    dpToPx(32)
                ).apply {
                    gravity = Gravity.CENTER
                }
            }
            addView(iconView)

            // 设置触摸事件（同时处理点击和拖动）
            setOnTouchListener(FloatBallTouchListener())
        }

        // 设置布局参数
        // 使用 TYPE_APPLICATION_OVERLAY（Android 8.0+ 标准悬浮窗类型）
        // 注意：王者荣耀等全屏游戏可能会覆盖悬浮窗，这是系统设计限制
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = screenWidth - dpToPx(70)
            y = screenHeight / 2 - dpToPx(30)
        }

        try {
            windowManager?.addView(floatBallView, layoutParams)
            Log.d(TAG, "FloatBall added to window successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add float ball: ${e.message}", e)
            // 检查是否是权限问题
            if (!android.provider.Settings.canDrawOverlays(this)) {
                Log.e(TAG, "SYSTEM_ALERT_WINDOW permission not granted!")
                Toast.makeText(this, "请授予悬浮窗权限", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "创建悬浮球失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private inner class FloatBallTouchListener : View.OnTouchListener {
        private var startX = 0f
        private var startY = 0f
        
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    startX = event.rawX
                    startY = event.rawY
                    Log.d(TAG, "ACTION_DOWN: x=${event.x}, y=${event.y}")
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - startX).toInt()
                    val deltaY = (event.rawY - startY).toInt()
                    
                    Log.d(TAG, "ACTION_MOVE: deltaX=$deltaX, deltaY=$deltaY, isDragging=$isDragging")

                    // 如果移动距离超过阈值，认为是拖拽
                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isDragging = true
                        Log.d(TAG, "Dragging started")
                    }

                    if (isDragging) {
                        layoutParams?.x = initialX + deltaX
                        layoutParams?.y = initialY + deltaY
                        windowManager?.updateViewLayout(floatBallView, layoutParams)
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    val deltaX = (event.rawX - startX).toInt()
                    val deltaY = (event.rawY - startY).toInt()
                    Log.d(TAG, "ACTION_UP: isDragging=$isDragging, deltaX=$deltaX, deltaY=$deltaY")
                    
                    if (isDragging) {
                        // 吸附到屏幕边缘
                        val newX = layoutParams?.x ?: 0
                        val targetX = if (newX < screenWidth / 2) {
                            dpToPx(10) // 吸附到左边
                        } else {
                            screenWidth - dpToPx(70) // 吸附到右边
                        }
                        
                        // 使用动画平滑移动到边缘
                        animateToEdge(targetX)
                        return true // 拖动时消费事件
                    } else {
                        // 点击事件：显示菜单
                        Log.d(TAG, ">>> Tap detected, showing menu")
                        showFloatBallMenu()
                        return true
                    }
                }
            }
            return false
        }
    }

    private fun animateToEdge(targetX: Int) {
        val currentX = layoutParams?.x ?: 0
        val distance = targetX - currentX
        val steps = 10
        val stepDistance = distance / steps
        var step = 0
        
        val runnable = object : Runnable {
            override fun run() {
                step++
                layoutParams?.x = currentX + stepDistance * step
                windowManager?.updateViewLayout(floatBallView, layoutParams)
                if (step < steps) {
                    handler.postDelayed(this, 20)
                }
            }
        }
        handler.post(runnable)
    }
    
    /**
     * 显示悬浮球菜单
     */
    @SuppressLint("InflateParams")
    private fun showFloatBallMenu() {
        // 如果菜单已显示，先关闭
        if (menuPopupWindow != null && menuPopupWindow!!.isShowing) {
            menuPopupWindow?.dismiss()
            return
        }
        
        // 加载菜单布局
        val menuView = android.view.LayoutInflater.from(this).inflate(R.layout.float_ball_menu, null)
        
        // 创建PopupWindow
        menuPopupWindow = PopupWindow(
            menuView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            // 设置外部可点击
            isOutsideTouchable = true
            // 设置背景
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.WHITE))
            // 设置动画
            animationStyle = android.R.style.Animation_Dialog
            
            // 设置透明度SeekBar
            val seekBar = menuView.findViewById<SeekBar>(R.id.seekbar_opacity)
            val opacityText = menuView.findViewById<TextView>(R.id.text_opacity)
            
            seekBar.progress = currentOpacity
            opacityText.text = "${currentOpacity}%"
            
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        // 确保最小透明度为30%
                        val newProgress = progress.coerceAtLeast(30)
                        seekBar?.progress = newProgress
                        currentOpacity = newProgress
                        opacityText.text = "${newProgress}%"
                        
                        // 设置悬浮球透明度
                        floatBallView?.alpha = newProgress / 100f
                        
                        // 保存透明度
                        prefs.edit().putInt("opacity", newProgress).apply()
                    }
                }
                
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            
            // 设置截图按钮
            menuView.findViewById<LinearLayout>(R.id.btn_screenshot).apply {
                // 设置图标颜色
                findViewById<ImageView>(R.id.icon_screenshot)?.setColorFilter(Color.parseColor("#333333"))
                setOnClickListener {
                    dismiss()
                    takeScreenshot()
                }
            }
            
            // 设置关闭按钮
            menuView.findViewById<LinearLayout>(R.id.btn_close).apply {
                // 设置图标颜色
                findViewById<ImageView>(R.id.icon_close)?.setColorFilter(Color.parseColor("#E53935"))
                setOnClickListener {
                    dismiss()
                    stopSelf()
                }
            }
            
            // 设置外部点击关闭
            setOnDismissListener {
                menuPopupWindow = null
            }
        }
        
        // 显示菜单（位于悬浮球旁边）
        floatBallView?.let { anchor ->
            val anchorLocation = IntArray(2)
            anchor.getLocationOnScreen(anchorLocation)
            
            // 计算菜单位置，确保不超出屏幕
            val menuWidth = dpToPx(200) // 估计菜单宽度
            var x = anchorLocation[0] + anchor.width + dpToPx(8)
            var y = anchorLocation[1]
            
            // 如果右侧空间不够，显示在左侧
            if (x + menuWidth > screenWidth) {
                x = anchorLocation[0] - menuWidth - dpToPx(8)
            }
            
            // 如果下方空间不够，显示在上方
            val menuHeight = dpToPx(180) // 估计菜单高度
            if (y + menuHeight > screenHeight) {
                y = screenHeight - menuHeight - dpToPx(20)
            }
            
            menuPopupWindow?.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
        }
    }

    private fun onFloatBallClick() {
        Log.d(TAG, ">>> onFloatBallClick called, isDragging=$isDragging")
        if (!isDragging) {
            Log.d(TAG, ">>> Calling takeScreenshot()")
            takeScreenshot()
        } else {
            Log.d(TAG, ">>> Skipping screenshot (is dragging)")
        }
    }

    private fun removeFloatBall() {
        // 先关闭菜单
        menuPopupWindow?.dismiss()
        menuPopupWindow = null
        
        try {
            floatBallView?.let { view ->
                windowManager?.let { wm ->
                    try {
                        wm.removeView(view)
                        Log.d(TAG, "FloatBall removed from window")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error removing view: ${e.message}")
                    }
                }
            }
            floatBallView = null
            layoutParams = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in removeFloatBall: ${e.message}")
            floatBallView = null
            layoutParams = null
        }
    }

    fun takeScreenshot() {
        Log.d(TAG, ">>> takeScreenshot called")
        Log.d(TAG, ">>> hasValidPermission=${ProjectionPermissionManager.hasValidPermission()}")
        
        if (!ProjectionPermissionManager.hasValidPermission()) {
            Log.w(TAG, ">>> No valid permission!")
            Toast.makeText(this, "请先授予截屏权限", Toast.LENGTH_LONG).show()
            notifyMainActivityForPermission()
            return
        }

        handler.post {
            Log.d(TAG, ">>> Starting screenshot in handler")
            captureScreen()
        }
    }

    private fun notifyMainActivityForPermission() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "REQUEST_SCREENSHOT_PERMISSION"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun setupImageReader() {
        // 每次截图都创建新的 ImageReader
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )
    }

    @SuppressLint("WrongConstant")
    private fun captureScreen() {
        Log.d(TAG, "captureScreen called")
        
        // 防止并发截图
        if (isCapturing) {
            Log.w(TAG, "Already capturing, skip")
            return
        }
        
        // 确保截图环境已初始化
        if (!screenshotInitialized || virtualDisplay == null || imageReader == null) {
            Log.w(TAG, "Screenshot not initialized, initializing now...")
            initProjection()
            if (!screenshotInitialized) {
                Toast.makeText(this, "截图环境初始化失败", Toast.LENGTH_SHORT).show()
                return
            }
            // 初始化后稍等一下再截图
            handler.postDelayed({
                isCapturing = true
                acquireImage()
            }, 200)
            return
        }

        try {
            isCapturing = true
            // 直接从已存在的 VirtualDisplay 获取图像（不需要重建）
            acquireImage()
        } catch (e: Exception) {
            isCapturing = false
            Log.e(TAG, "captureScreen failed: ${e.message}")
            Toast.makeText(this, "截屏失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private var retryCount = 0
    private val maxRetries = 10
    
    private fun acquireImage() {
        try {
            val image = imageReader?.acquireLatestImage()
            if (image != null) {
                retryCount = 0
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * screenWidth

                val bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride,
                    screenHeight,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()

                // 裁剪到实际屏幕大小
                val croppedBitmap = if (rowPadding > 0) {
                    Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
                } else {
                    bitmap
                }

                if (bitmap != croppedBitmap) {
                    bitmap.recycle()
                }

                saveBitmap(croppedBitmap)
                isCapturing = false
            } else {
                // 没有获取到图像，重试（最多重试 maxRetries 次）
                retryCount++
                if (retryCount <= maxRetries) {
                    Log.w(TAG, "No image acquired, retrying... ($retryCount/$maxRetries)")
                    handler.postDelayed({
                        acquireImage()
                    }, 100)
                } else {
                    Log.e(TAG, "Failed to acquire image after $maxRetries retries")
                    retryCount = 0
                    isCapturing = false
                    Toast.makeText(this, "截图失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "acquireImage failed: ${e.message}")
            isCapturing = false
            // 不要释放 VirtualDisplay，保持环境以便下次使用
            Toast.makeText(this, "获取图像失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val honorShotsDir = File(picturesDir, "HonorScreenshots")
            if (!honorShotsDir.exists()) {
                honorShotsDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "王者截图_$timestamp.png"
            val file = File(honorShotsDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            Log.d(TAG, "Screenshot saved: ${file.absolutePath}")

            // 通知媒体库更新
            MediaScannerConnection.scanFile(
                this,
                arrayOf(file.absolutePath),
                arrayOf("image/png"),
                null
            )

            // 发送广播通知截图成功，用于更新 UI 计数
            val updateIntent = Intent(ACTION_SCREENSHOT_COUNT_UPDATED)
            updateIntent.putExtra(EXTRA_SCREENSHOT_PATH, file.absolutePath)
            sendBroadcast(updateIntent)
            Log.d(TAG, "Sent screenshot count update broadcast")

            // 显示截图成功通知（Toast 在后台服务中可能不可见，改用通知）
            showScreenshotNotification(file.name, file.absolutePath)

            bitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "saveBitmap failed: ${e.message}")
            // 显示失败通知
            showScreenshotNotification("截图失败: ${e.message}", null)
        }
        // 注意：不要释放 VirtualDisplay，保持截图环境以便下次使用
    }
    
    /**
     * 显示截图通知
     */
    private fun showScreenshotNotification(message: String, filePath: String?) {
        try {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID_SCREENSHOT)
                .setSmallIcon(R.drawable.ic_screenshot)
                .setContentTitle("截图通知")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 100, 50, 100))
            
            // 如果有文件路径，添加查看按钮
            if (filePath != null) {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        android.net.Uri.parse("file://$filePath"),
                        "image/*"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, viewIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.setContentIntent(pendingIntent)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(SCREENSHOT_NOTIFICATION_ID, builder.build())
            Log.d(TAG, "Screenshot notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification: ${e.message}")
            // 降级到 Toast
            handler.post {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 只释放 VirtualDisplay（Service 销毁时调用）
     */
    private fun releaseVirtualDisplay() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            screenshotInitialized = false
            Log.d(TAG, "VirtualDisplay released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing VirtualDisplay: ${e.message}")
            virtualDisplay = null
            imageReader = null
            screenshotInitialized = false
        }
    }

    /**
     * 释放所有资源（在 Service 销毁时调用）
     */
    private fun releaseResources() {
        releaseVirtualDisplay()
        try {
            mediaProjection?.unregisterCallback(mediaProjectionCallback)
        } catch (e: Exception) { }
        // 注意：不调用 mediaProjection.stop()，让它被系统自动清理
        mediaProjection = null
        projectionInitialized = false
        Log.d(TAG, "All resources released")
    }
}

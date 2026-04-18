package com.honorshots.screenshot.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class  GameScreenshotService : AccessibilityService() {
    
    companion object {
        private const val TAG = "GameScreenshotSvc"
        
        // 王者荣耀包名
        const val KING_OF_GLORY_PACKAGE = "com.tencent.tmgp.sgame"
        
        // 其他常用游戏包名（可扩展）
        val GAME_PACKAGES = listOf(
            "com.tencent.tmgp.sgame",           // 王者荣耀
            "com.tencent.ig",                     // PUBG
            "com.pubg.krmobile",                 // PUBG Mobile Korea
            "com.tencent.lolm",                  // 英雄联盟手游
            "com.mobile.legends",                // Mobile Legends
        )
        
        const val ACTION_AUTO_SCREENSHOT = "com.honorshots.screenshot.ACTION_AUTO_SCREENSHOT"
    }
    
    private var lastScreenshotTime = 0L
    private val screenshotCooldown = 5000L // 5秒内不重复截图
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val packageName = event.packageName?.toString() ?: return
        
        // 检测应用切换
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (GAME_PACKAGES.contains(packageName)) {
                    Log.d(TAG, "Game detected: $packageName")
                    autoScreenshot()
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // 检测游戏加载完成（可以用于更精确的截图时机）
            }
        }
    }
    
    private fun autoScreenshot() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScreenshotTime < screenshotCooldown) {
            Log.d(TAG, "Screenshot cooldown, skipping")
            return
        }
        lastScreenshotTime = currentTime
        
        Log.d(TAG, "Auto screenshot triggered")
        
        // 发送截图请求给 FloatBallService
        val intent = Intent(this, FloatBallService::class.java).apply {
            action = ACTION_AUTO_SCREENSHOT
        }
        startService(intent)
    }
    
    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Game screenshot service connected")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Game screenshot service destroyed")
    }
}

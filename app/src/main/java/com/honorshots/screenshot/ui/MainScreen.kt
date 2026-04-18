package com.honorshots.screenshot.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.honorshots.screenshot.service.FloatBallService
import com.honorshots.screenshot.service.ProjectionPermissionManager
import com.honorshots.screenshot.ui.theme.Accent
import com.honorshots.screenshot.ui.theme.Primary
import com.honorshots.screenshot.ui.theme.Secondary
import com.honorshots.screenshot.ui.theme.Success
import com.honorshots.screenshot.ui.theme.Warning
import kotlinx.coroutines.launch

// 白色主题颜色
private val WhiteBackground = Color(0xFFFFFFFF)
private val LightGray = Color(0xFFF5F5F5)
private val DarkText = Color(0xFF212121)
private val GrayText = Color(0xFF757575)
private val LightBlue = Color(0xFF2196F3)

// ==================== 主界面 ====================
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onRequestProjectionPermission: () -> Unit = {}
) {
    MainScreenWithViewModel(
        viewModel = viewModel,
        onRequestProjectionPermission = onRequestProjectionPermission,
        onViewModelReady = {}
    )
}

@Composable
fun MainScreenWithViewModel(
    viewModel: MainViewModel = hiltViewModel(),
    onRequestProjectionPermission: () -> Unit = {},
    onViewModelReady: (MainViewModel) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // 使用共享的投影权限状态
    val hasProjectionPermission by ProjectionPermissionManager.hasProjectionPermission.collectAsState()
    
    // 通知父组件 ViewModel 已准备好
    LaunchedEffect(viewModel) {
        onViewModelReady(viewModel)
    }
    
    // 监听投影权限变化，更新ViewModel状态
    LaunchedEffect(hasProjectionPermission) {
        if (hasProjectionPermission) {
            viewModel.setProjectionPermissionGranted()
        }
    }
    
    // 定期刷新截图数量
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateScreenshotCount()
            kotlinx.coroutines.delay(2000)
        }
    }

    // 权限请求launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissions()
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissions()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainScreen", "Notification permission: $isGranted")
        viewModel.checkPermissions()
    }

    // 视频文件夹选择器
    val videoFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val path = getPathFromUri(context, it)
            if (path != null) {
                viewModel.saveVideoFolderPath(path)
            } else {
                viewModel.saveVideoFolderPath(it.toString())
            }
        }
    }

    // 功能分析跳转launcher
    val featureAnalysisLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    // 检查通知权限
    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    val hasNotificationPermission = remember { checkNotificationPermission() }

    // 定期检查权限
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.checkPermissions()
            kotlinx.coroutines.delay(1000)
        }
    }

    // Toast消息
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // 创建侧边菜单内容
    val drawerContent = @Composable {
        DrawerContent(
            state = state,
            hasProjectionPermission = hasProjectionPermission,
            hasNotificationPermission = hasNotificationPermission,
            onCloseDrawer = { scope.launch { drawerState.close() } },
            onRequestOverlayPermission = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                overlayPermissionLauncher.launch(intent)
            },
            onRequestStoragePermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    storagePermissionLauncher.launch(intent)
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:${context.packageName}")
                    storagePermissionLauncher.launch(intent)
                }
            },
            onRequestProjectionPermission = onRequestProjectionPermission,
            onRequestNotificationPermission = {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            },
            onOpenScreenshotFolder = { viewModel.openScreenshotFolder() },
            onOpenSaveFolderSettings = { viewModel.openSaveFolderSettings() },
            onSelectVideoFolder = { videoFolderPickerLauncher.launch(null) },
            onOpenVideoFolder = { viewModel.openVideoFolder() },
            onPerformAIAnalysis = { viewModel.performAIAnalysis() },
            onFeatureAnalysis = {
                val url = viewModel.getFeatureAnalysisUrl()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                featureAnalysisLauncher.launch(intent)
            },
            onPreMatchAnalysis = {
                viewModel.performPreMatchAnalysis()
            }
        )
    }

    // 使用ModalNavigationDrawer实现侧边菜单
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = drawerContent,
        gesturesEnabled = true
    ) {
        // 主页面 - 白色背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // 顶部栏 - 菜单按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "菜单",
                            tint = DarkText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Text(
                        text = "王者截图",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    
                    // 占位，保持标题居中
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 截图统计卡片
                HomeScreenshotStatsCard(count = state.screenshotCount)

                Spacer(modifier = Modifier.height(20.dp))

                // 悬浮球开关
                HomeFloatBallSwitch(
                    isEnabled = state.isServiceRunning,
                    allPermissionsGranted = state.allPermissionsGranted,
                    hasProjectionPermission = hasProjectionPermission,
                    onToggle = {
                        val hasAllPermissions = state.hasOverlayPermission && 
                                state.hasStoragePermission && 
                                hasProjectionPermission
                        
                        if (!hasAllPermissions) {
                            Toast.makeText(context, "请先授予所有权限", Toast.LENGTH_SHORT).show()
                            return@HomeFloatBallSwitch
                        }
                        
                        val serviceIntent = Intent(context, FloatBallService::class.java)
                        if (state.isServiceRunning) {
                            context.stopService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        viewModel.checkPermissions()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 截图文件夹路径卡片
                HomeFolderPathCard(
                    icon = Icons.Outlined.PhotoLibrary,
                    title = "截图文件夹路径",
                    path = "Pictures/HonorScreenshots",
                    onClick = { viewModel.openScreenshotFolder() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 视频文件夹路径卡片
                HomeFolderPathCard(
                    icon = Icons.Outlined.VideoLibrary,
                    title = "视频文件夹路径",
                    path = state.videoFolderPath ?: "未设置",
                    onClick = {
                        if (state.videoFolderPath != null) {
                            viewModel.openVideoFolder()
                        } else {
                            videoFolderPickerLauncher.launch(null)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 使用说明
                HomeUsageInstructions()
            }
        }
    }
}

// ==================== 侧边菜单内容 ====================
@Composable
fun DrawerContent(
    state: PermissionState,
    hasProjectionPermission: Boolean,
    hasNotificationPermission: Boolean,
    onCloseDrawer: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestStoragePermission: () -> Unit,
    onRequestProjectionPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenScreenshotFolder: () -> Unit,
    onOpenSaveFolderSettings: () -> Unit,
    onSelectVideoFolder: () -> Unit,
    onOpenVideoFolder: () -> Unit,
    onPerformAIAnalysis: () -> Unit,
    onFeatureAnalysis: () -> Unit,
    onPreMatchAnalysis: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.85f)
            .background(WhiteBackground)
            .padding(vertical = 16.dp)
    ) {
        // 菜单头部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "菜单",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onCloseDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "关闭",
                    tint = GrayText
                )
            }
        }

        Divider(color = LightGray)

        // ===== 权限一栏 =====
        DrawerSectionTitle(title = "权限")
        
        DrawerPermissionItem(
            title = "悬浮窗权限",
            isGranted = state.hasOverlayPermission,
            onClick = onRequestOverlayPermission
        )
        
        DrawerPermissionItem(
            title = "存储权限",
            isGranted = state.hasStoragePermission,
            onClick = onRequestStoragePermission
        )
        
        DrawerPermissionItem(
            title = "截屏权限",
            isGranted = hasProjectionPermission,
            onClick = onRequestProjectionPermission
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            DrawerPermissionItem(
                title = "通知权限",
                isGranted = hasNotificationPermission,
                onClick = onRequestNotificationPermission
            )
        }

        Divider(color = LightGray, modifier = Modifier.padding(vertical = 12.dp))

        // ===== 功能一栏 =====
        DrawerSectionTitle(title = "功能")
        
        DrawerFunctionItem(
            icon = Icons.Default.PhotoLibrary,
            title = "打开截图文件夹",
            onClick = {
                onOpenScreenshotFolder()
                onCloseDrawer()
            }
        )

        DrawerFunctionItem(
            icon = Icons.Default.Settings,
            title = "保存文件夹设置",
            onClick = {
                onOpenSaveFolderSettings()
                onCloseDrawer()
            }
        )

        DrawerFunctionItem(
            icon = Icons.Default.VideoLibrary,
            title = "保存视频文件路径",
            onClick = {
                onSelectVideoFolder()
                onCloseDrawer()
            }
        )

        DrawerFunctionItem(
            icon = Icons.Default.Folder,
            title = "打开视频文件夹",
            onClick = {
                onOpenVideoFolder()
                onCloseDrawer()
            }
        )

        DrawerFunctionItem(
            icon = Icons.Default.Psychology,
            title = "进行AI分析",
            onClick = {
                onPerformAIAnalysis()
                onCloseDrawer()
            }
        )

        DrawerFunctionItem(
            icon = Icons.Default.Analytics,
            title = "搜索教程视频",
            onClick = {
                onFeatureAnalysis()
                onCloseDrawer()
            }
        )

        // 赛前分析按钮
        DrawerFunctionItem(
            icon = Icons.Default.Assessment,
            title = "赛前分析",
            onClick = {
                onPreMatchAnalysis()
                onCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // 底部信息
        Text(
            text = "王者截图 v1.0",
            fontSize = 12.sp,
            color = GrayText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun DrawerSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = LightBlue,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
fun DrawerPermissionItem(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isGranted) Success else Warning,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = DarkText,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (isGranted) "已授权" else "未授权",
            fontSize = 12.sp,
            color = if (isGranted) Success else Warning
        )
    }
}

@Composable
fun DrawerFunctionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = DarkText
        )
    }
}

// ==================== 主页组件 ====================
@Composable
fun HomeScreenshotStatsCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$count",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "已截取图片数量",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }
        }
    }
}

@Composable
fun HomeFloatBallSwitch(
    isEnabled: Boolean,
    allPermissionsGranted: Boolean,
    hasProjectionPermission: Boolean,
    onToggle: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scale by animateFloatAsState(
        targetValue = if (isEnabled) 1.02f else 1f,
        label = "scale"
    )
    val canEnable = allPermissionsGranted && hasProjectionPermission

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                if (canEnable || isEnabled) {
                    onToggle()
                } else {
                    Toast.makeText(context, "请先授予所有权限", Toast.LENGTH_SHORT).show()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) Primary else LightGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isEnabled) "悬浮球已开启" else "悬浮球已关闭",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) Color.White else DarkText
                )
                Text(
                    text = when {
                        isEnabled -> "点击悬浮球截屏"
                        !canEnable -> "请先授予所有权限"
                        else -> "点击开启悬浮球"
                    },
                    fontSize = 12.sp,
                    color = if (isEnabled) Color.White.copy(alpha = 0.8f) else GrayText
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { 
                    if (canEnable || isEnabled) {
                        onToggle()
                    }
                },
                enabled = canEnable || isEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Primary,
                    checkedTrackColor = Color.White.copy(alpha = 0.5f),
                    uncheckedThumbColor = GrayText,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}

@Composable
fun HomeFolderPathCard(
    icon: ImageVector,
    title: String,
    path: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = GrayText
                )
                Text(
                    text = path,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = GrayText,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HomeUsageInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "使用说明",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )
            Spacer(modifier = Modifier.height(12.dp))
            val instructions = listOf(
                "1. 请授予所有必需权限（通过侧边菜单）",
                "2. 开启悬浮球后，拖动到屏幕边缘可隐藏",
                "3. 点击悬浮球即可截取当前画面",
                "4. 截图自动保存到指定文件夹",
                "5. 在游戏中建议将悬浮球隐藏到边缘"
            )
            instructions.forEach { instruction ->
                Text(
                    text = instruction,
                    fontSize = 13.sp,
                    color = GrayText,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

// Helper function
private fun getPathFromUri(context: android.content.Context, uri: Uri): String? {
    val docId = uri.lastPathSegment ?: return null
    val split = docId.split(":")
    val type = split.getOrNull(0)
    val relativePath = split.getOrNull(1) ?: ""
    return when {
        "primary".equals(type, ignoreCase = true) -> {
            "${Environment.getExternalStorageDirectory()}/$relativePath"
        }
        else -> "/storage/$type/$relativePath"
    }
}

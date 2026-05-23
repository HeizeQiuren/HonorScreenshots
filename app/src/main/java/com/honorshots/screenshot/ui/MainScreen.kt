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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.honorshots.screenshot.service.FloatBallService
import com.honorshots.screenshot.service.ProjectionPermissionManager
import com.honorshots.screenshot.ui.theme.CardBackground
import com.honorshots.screenshot.ui.theme.CardBorder
import com.honorshots.screenshot.ui.theme.DarkGold
import com.honorshots.screenshot.ui.theme.DarkGoldDim
import com.honorshots.screenshot.ui.theme.DarkGoldLight
import com.honorshots.screenshot.ui.theme.GoldDivider
import com.honorshots.screenshot.ui.theme.GoldLine
import com.honorshots.screenshot.ui.theme.MatteBlack
import com.honorshots.screenshot.ui.theme.Success
import com.honorshots.screenshot.ui.theme.SurfaceDark
import com.honorshots.screenshot.ui.theme.TextDimGray
import com.honorshots.screenshot.ui.theme.TextGold
import com.honorshots.screenshot.ui.theme.TextGoldSecondary
import com.honorshots.screenshot.ui.theme.Warning
import kotlinx.coroutines.launch

// ===== 暗黑金主题常量 =====
private val KaiTiBold = FontFamily.Serif

// ==================== 主界面 ====================
@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithViewModel(
    viewModel: MainViewModel = hiltViewModel(),
    onRequestProjectionPermission: () -> Unit = {},
    onViewModelReady: (MainViewModel) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(1) } // 默认选中"悬浮球"

    // 使用共享投影权限状态
    val hasProjectionPermission by ProjectionPermissionManager.hasProjectionPermission.collectAsState()

    LaunchedEffect(viewModel) { onViewModelReady(viewModel) }

    LaunchedEffect(hasProjectionPermission) {
        if (hasProjectionPermission) viewModel.setProjectionPermissionGranted()
    }

    // 定期刷新截图数量与权限
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateScreenshotCount()
            viewModel.checkPermissions()
            kotlinx.coroutines.delay(2000)
        }
    }

    // Toast 消息
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // ===== 权限请求 Launcher =====
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.checkPermissions() }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.checkPermissions() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainScreen", "Notification permission: $isGranted")
        viewModel.checkPermissions()
    }

    val videoFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val path = getPathFromUri(it)
            if (path != null) viewModel.saveVideoFolderPath(path)
            else viewModel.saveVideoFolderPath(it.toString())
        }
    }

    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }
    val hasNotificationPermission = remember { checkNotificationPermission() }

    // ===== 权限回调 =====
    val onRequestOverlayPermission: () -> Unit = {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        overlayPermissionLauncher.launch(intent)
    }
    val onRequestStoragePermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            storagePermissionLauncher.launch(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            storagePermissionLauncher.launch(intent)
        }
    }
    val onRequestNotificationPermission: () -> Unit = {
        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    // ===== Scaffold + 三标签导航 =====
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MatteBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "王者截图",
                        fontFamily = KaiTiBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DarkGoldLight
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MatteBlack,
                    titleContentColor = DarkGoldLight
                )
            )
        },
        bottomBar = {
            // ===== 底部三等分导航栏 =====
            NavigationBar(
                containerColor = MatteBlack,
                tonalElevation = 0.dp
            ) {
                // 标签1：权限
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "权限",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "权限",
                            fontFamily = KaiTiBold,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkGoldLight,
                        selectedTextColor = DarkGoldLight,
                        unselectedIconColor = TextDimGray,
                        unselectedTextColor = TextDimGray,
                        indicatorColor = DarkGoldDim.copy(alpha = 0.15f)
                    )
                )
                // 标签2：悬浮球
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Circle,
                            contentDescription = "悬浮球",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "悬浮球",
                            fontFamily = KaiTiBold,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkGoldLight,
                        selectedTextColor = DarkGoldLight,
                        unselectedIconColor = TextDimGray,
                        unselectedTextColor = TextDimGray,
                        indicatorColor = DarkGoldDim.copy(alpha = 0.15f)
                    )
                )
                // 标签3：赛后
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Psychology,
                            contentDescription = "赛后",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "赛后",
                            fontFamily = KaiTiBold,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkGoldLight,
                        selectedTextColor = DarkGoldLight,
                        unselectedIconColor = TextDimGray,
                        unselectedTextColor = TextDimGray,
                        indicatorColor = DarkGoldDim.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        // 内容区
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MatteBlack)
        ) {
            when (selectedTab) {
                0 -> PermissionTab(
                    state = state,
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestStoragePermission = onRequestStoragePermission,
                    onRequestNotificationPermission = onRequestNotificationPermission,
                    onOpenScreenshotFolder = { viewModel.openScreenshotFolder() },
                    onOpenSaveFolderSettings = { viewModel.openSaveFolderSettings() },
                    onSelectVideoFolder = { videoFolderPickerLauncher.launch(null) },
                    onOpenVideoFolder = { viewModel.openVideoFolder() }
                )
                1 -> FloatBallTab(
                    state = state,
                    hasProjectionPermission = hasProjectionPermission,
                    onRequestOverlayPermission = onRequestOverlayPermission,
                    onRequestProjectionPermission = onRequestProjectionPermission,
                    onToggleFloatBall = {
                        val hasAllPermissions = state.hasOverlayPermission &&
                                state.hasStoragePermission &&
                                hasProjectionPermission
                        if (!hasAllPermissions) {
                            Toast.makeText(context, "请先授予所有权限", Toast.LENGTH_SHORT).show()
                            return@FloatBallTab
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
                2 -> PostMatchTab(
                    onAIAnalysis = { viewModel.openAIAnalysisUrl() }
                )
            }
        }
    }
}

// ==================== 标签页1：权限 ====================
@Composable
fun PermissionTab(
    state: PermissionState,
    hasNotificationPermission: Boolean,
    onRequestStoragePermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenScreenshotFolder: () -> Unit,
    onOpenSaveFolderSettings: () -> Unit,
    onSelectVideoFolder: () -> Unit,
    onOpenVideoFolder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 分区标题：权限
        SectionTitle("权限状态")

        // 存储权限
        DarkGoldPermissionItem(
            title = "存储权限",
            isGranted = state.hasStoragePermission,
            onClick = onRequestStoragePermission
        )

        // 通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(8.dp))
            DarkGoldPermissionItem(
                title = "通知权限",
                isGranted = hasNotificationPermission,
                onClick = onRequestNotificationPermission
            )
        }

        // 云纹装饰分割线
        CloudDivider()

        // 分区标题：文件管理
        SectionTitle("文件管理")

        // 文件相关功能
        DarkGoldFunctionItem(
            icon = Icons.Outlined.PhotoLibrary,
            title = "打开截图文件夹",
            onClick = onOpenScreenshotFolder
        )
        DarkGoldFunctionItem(
            icon = Icons.Outlined.Settings,
            title = "保存文件夹设置",
            onClick = onOpenSaveFolderSettings
        )
        DarkGoldFunctionItem(
            icon = Icons.Outlined.VideoLibrary,
            title = "保存视频文件路径",
            onClick = onSelectVideoFolder
        )
        DarkGoldFunctionItem(
            icon = Icons.Outlined.Folder,
            title = "打开视频文件夹",
            onClick = onOpenVideoFolder
        )

        // 快捷入口
        CloudDivider()
        SectionTitle("快捷入口")

        DarkGoldFunctionItem(
            icon = Icons.Outlined.PhotoLibrary,
            title = "截图文件夹快捷入口",
            subtitle = "Pictures/HonorScreenshots",
            onClick = onOpenScreenshotFolder
        )
        DarkGoldFunctionItem(
            icon = Icons.Outlined.VideoLibrary,
            title = "视频文件夹快捷入口",
            subtitle = state.videoFolderPath ?: "未设置",
            onClick = {
                if (state.videoFolderPath != null) onOpenVideoFolder()
                else onSelectVideoFolder()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==================== 标签页2：悬浮球 ====================
@Composable
fun FloatBallTab(
    state: PermissionState,
    hasProjectionPermission: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onRequestProjectionPermission: () -> Unit,
    onToggleFloatBall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部分区：权限状态
        SectionTitle("权限状态")

        // 悬浮窗权限
        DarkGoldPermissionItem(
            title = "悬浮窗权限",
            isGranted = state.hasOverlayPermission,
            onClick = onRequestOverlayPermission
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 截屏权限
        DarkGoldPermissionItem(
            title = "截屏权限",
            isGranted = hasProjectionPermission,
            onClick = onRequestProjectionPermission
        )

        // 云纹装饰分割线
        CloudDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // 核心控件：悬浮球总开关（居中）
        DarkGoldFloatBallSwitch(
            isEnabled = state.isServiceRunning,
            canEnable = state.hasOverlayPermission && state.hasStoragePermission && hasProjectionPermission,
            onToggle = onToggleFloatBall
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ==================== 标签页3：赛后 ====================
@Composable
fun PostMatchTab(
    onAIAnalysis: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DarkGoldAIAnalysisCard(onClick = onAIAnalysis)
    }
}

// ==================== 可复用组件 ====================

// 分区标题
@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧暗金装饰线
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkGoldLight, DarkGoldDim, Color.Transparent)
                    )
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontFamily = KaiTiBold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = DarkGoldLight
        )
    }
}

// 权限条目组件（毛玻璃卡片）
@Composable
fun DarkGoldPermissionItem(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态图标
            Icon(
                imageVector = if (isGranted) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                contentDescription = null,
                tint = if (isGranted) Success else Warning,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontFamily = KaiTiBold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextGold,
                modifier = Modifier.weight(1f)
            )
            // 状态标签
            Text(
                text = if (isGranted) "✓ 已授权" else "⚠ 未授权",
                fontFamily = KaiTiBold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isGranted) Success else Warning
            )
        }
    }
}

// 功能条目组件（毛玻璃卡片）
@Composable
fun DarkGoldFunctionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DarkGoldLight,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = KaiTiBold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextGold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontFamily = KaiTiBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextGoldSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // 右侧箭头指示
            Text(
                text = "›",
                fontFamily = KaiTiBold,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextDimGray
            )
        }
    }
}

// 云纹分割线
@Composable
fun CloudDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // 左侧线条
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, GoldDivider, GoldDivider)
                    )
                )
        )
        // 中间云纹装饰点
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(DarkGoldLight.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(2.dp)
                .clip(CircleShape)
                .background(DarkGoldLight.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        // 右侧线条
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GoldDivider, GoldDivider, Color.Transparent)
                    )
                )
        )
    }
}

// 悬浮球总开关（核心控件，居中）
@Composable
fun DarkGoldFloatBallSwitch(
    isEnabled: Boolean,
    canEnable: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val scale by animateFloatAsState(
        targetValue = if (isEnabled) 1.03f else 1f,
        label = "floatBallScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = if (isEnabled) listOf(DarkGoldLight, DarkGoldDim)
                    else listOf(GoldDivider, CardBorder)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .background(if (isEnabled) DarkGoldDim.copy(alpha = 0.25f) else CardBackground)
            .clickable {
                if (canEnable || isEnabled) {
                    onToggle()
                } else {
                    Toast.makeText(context, "请先授予所有权限", Toast.LENGTH_SHORT).show()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 悬浮球图标（大号圆形）
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled)
                            Brush.radialGradient(
                                colors = listOf(DarkGoldLight, DarkGold, DarkGoldDim)
                            )
                        else
                            Brush.radialGradient(
                                colors = listOf(TextDimGray, Color(0xFF3A3A3A), Color(0xFF1A1A1A))
                            )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (isEnabled) MatteBlack else TextDimGray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isEnabled) "悬浮球已开启" else "悬浮球已关闭",
                fontFamily = KaiTiBold,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (isEnabled) DarkGoldLight else TextGoldSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    isEnabled -> "点击悬浮球截屏 · 拖拽移动位置"
                    !canEnable -> "请先在「权限」标签页授予所有权限"
                    else -> "点击下方开关启用悬浮球"
                },
                fontFamily = KaiTiBold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isEnabled) TextGoldSecondary else TextDimGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 开关
            Switch(
                checked = isEnabled,
                onCheckedChange = {
                    if (canEnable || isEnabled) onToggle()
                },
                enabled = canEnable || isEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkGoldLight,
                    checkedTrackColor = DarkGoldDim.copy(alpha = 0.4f),
                    uncheckedThumbColor = TextDimGray,
                    uncheckedTrackColor = Color(0xFF2A2A2A),
                    disabledCheckedThumbColor = DarkGoldDim,
                    disabledCheckedTrackColor = GoldDivider,
                    disabledUncheckedThumbColor = TextDimGray,
                    disabledUncheckedTrackColor = Color(0xFF1A1A1A)
                )
            )
        }
    }
}

// AI分析卡片（赛后标签页）
@Composable
fun DarkGoldAIAnalysisCard(onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        label = "aiCardScale"
    )

    Box(
        modifier = Modifier
            .padding(24.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(DarkGoldLight, DarkGoldDim, CardBorder)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI图标
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DarkGoldDim.copy(alpha = 0.3f))
                    .border(1.dp, DarkGoldLight.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = null,
                    tint = DarkGoldLight,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AI 赛后分析",
                fontFamily = KaiTiBold,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = DarkGoldLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "点击此处进入AI分析页面\n获取王者荣耀对局深度复盘",
                fontFamily = KaiTiBold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextGoldSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 进入按钮
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkGoldLight.copy(alpha = 0.15f))
                    .border(1.dp, DarkGoldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "开始分析",
                        fontFamily = KaiTiBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkGoldLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "→",
                        fontFamily = KaiTiBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkGoldLight
                    )
                }
            }
        }
    }
}

// ==================== 帮助函数 ====================
private fun getPathFromUri(uri: Uri): String? {
    val docId = uri.lastPathSegment ?: return null
    val split = docId.split(":")
    val type = split.getOrNull(0)
    val relativePath = split.getOrNull(1) ?: ""
    return when {
        "primary".equals(type, ignoreCase = true) ->
            "${Environment.getExternalStorageDirectory()}/$relativePath"
        else -> "/storage/$type/$relativePath"
    }
}

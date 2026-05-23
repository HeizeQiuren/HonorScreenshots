package com.honorshots.screenshot.ui.theme

import androidx.compose.ui.graphics.Color

// ===== 暗黑金高级风格颜色系统 =====

// Dark Gold Primary
val DarkGold = Color(0xFFC9A96E)
val DarkGoldLight = Color(0xFFD4AF37)
val DarkGoldDim = Color(0xFF8B7332)

// Matte Pure Black Background
val MatteBlack = Color(0xFF0A0A0A)
val CardBackground = Color(0x1AC9A96E)         // 半通透毛玻璃卡片底
val CardBorder = Color(0x33C9A96E)             // 烫金边框
val SurfaceDark = Color(0xFF141414)             // 深色表面层

// Text Colors - Dark Gold Tone
val TextGold = Color(0xFFC9A96E)               // 暗金主文字
val TextGoldSecondary = Color(0xFF8B7332)       // 暗金辅助文字
val TextDimGray = Color(0xFF555555)             // 极暗灰（未选中状态）

// Accent / Divider
val GoldDivider = Color(0x33D4AF37)            // 纤细烫金分割线
val GoldLine = Color(0xFF3D3520)               // 暗金线条

// Status Colors (保持原有)
val Success = Color(0xFF22C55E)
val Error = Color(0xFFEF4444)
val Warning = Color(0xFFF59E0B)

// Float Ball (保持悬浮球原有配色)
val FloatBallBg = Color(0xFF6366F1)
val FloatBallIcon = Color(0xFFFFFFFF)

// 向下兼容别名（用于Theme.kt和FloatBallService等已有代码）
val Primary = DarkGold
val PrimaryVariant = DarkGoldDim
val Secondary = DarkGoldLight
val Background = MatteBlack
val Surface = SurfaceDark
val SurfaceVariant = Color(0xFF2A2A2A)
val Accent = DarkGoldLight
val AccentVariant = DarkGoldDim
val TextPrimary = TextGold
val TextSecondary = TextGoldSecondary

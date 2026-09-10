package com.notiguard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 色票見 DESIGN_TOKENS.md。這個 App 只有深色一種樣貌，
 * 淺色底會讓「攔截中」的紅藍語意失去對比，所以不做 light theme。
 */
object NG {
    val base = Color(0xFF050F16)
    val surface = Color(0xFF0A1923)
    val card = Color(0xFF0C1C27)
    val cardMuted = Color(0xFF152630)
    val raise = Color(0xFF243B4B)

    val line = Color(0xFF233E50)
    val lineSoft = Color(0xFF1B303D)

    val ink = Color(0xFFEAF2FF)
    val inkMuted = Color(0xFFB1C5DA)
    val inkFaint = Color(0xFF91A9BF)

    val blue = Color(0xFF187BFF)
    val actionBlue = Color(0xFF0867E8)
    val blueLight = Color(0xFF38BFFF)
    val blueSoft = Color(0x243B82F6)

    val green = Color(0xFF22C55E)

    val red = Color(0xFFEF4444)
    val redLight = Color(0xFFF87171)
    val redSoft = Color(0x21EF4444)

    val cardBrush = Brush.linearGradient(listOf(Color(0xFF10222E), card, Color(0xFF091720)))
    val actionBrush = Brush.verticalGradient(listOf(Color(0xFF2586FF), actionBlue))

    // 形狀
    val cardShape = RoundedCornerShape(16.dp)
    val rowShape = RoundedCornerShape(14.dp)
    val buttonShape = RoundedCornerShape(14.dp)
    val iconShape = RoundedCornerShape(11.dp)
    val iconShapeSmall = RoundedCornerShape(10.dp)

    // 字級
    val brand = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp)
    val navTitle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    val statValue = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum", letterSpacing = (-0.4).sp)
    val sectionTitle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    val appName = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
    val heroName = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold)
    val body = TextStyle(fontSize = 14.5f.sp, lineHeight = 22.sp)
    val summary = TextStyle(fontSize = 13.sp)
    val caption = TextStyle(fontSize = 12.5f.sp)
    val pill = TextStyle(fontSize = 11.5f.sp)
}

private val NotiGuardColors = darkColorScheme(
    primary = NG.actionBlue,
    onPrimary = Color.White,
    background = NG.base,
    onBackground = NG.ink,
    surface = NG.card,
    onSurface = NG.ink,
    surfaceVariant = NG.cardMuted,
    onSurfaceVariant = NG.inkMuted,
    error = NG.red,
    outline = NG.line,
)

@Composable
fun NotiGuardTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = NotiGuardColors,
        typography = Typography(),
        content = content,
    )
}

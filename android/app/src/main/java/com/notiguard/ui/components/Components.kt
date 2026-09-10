package com.notiguard.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.notiguard.ui.theme.NG

/**
 * 常見 App 的品牌色與縮寫。示範資料裡的 App 沒安裝時也能顯示得出來，
 * 真的裝了就用系統圖示（見 [AppGlyph]）。
 */
object Brand {
    private val TABLE = mapOf(
        "jp.naver.line.android" to (Color(0xFF06C755) to "LINE"),
        "com.google.android.youtube" to (Color(0xFFFF0000) to "YT"),
        "com.instagram.android" to (Color(0xFFD62976) to "IG"),
        "com.facebook.katana" to (Color(0xFF1877F2) to "f"),
        "com.google.android.gm" to (Color(0xFFEA4335) to "M"),
        "com.android.chrome" to (Color(0xFF4285F4) to "C"),
        "com.shopee.tw" to (Color(0xFFEE4D2D) to "蝦"),
        "com.dcard.mobile" to (Color(0xFF0A6CA8) to "D"),
        "com.android.systemui" to (Color(0xFF33445C) to "系"),
        "com.whatsapp" to (Color(0xFF25D366) to "WA"),
        "org.telegram.messenger" to (Color(0xFF2AABEE) to "TG"),
        "com.Slack" to (Color(0xFF4A154B) to "S"),
        "com.discord" to (Color(0xFF5865F2) to "DC"),
    )

    fun color(packageName: String): Color =
        TABLE[packageName]?.first ?: fallbackColor(packageName)

    fun short(packageName: String, appLabel: String): String =
        TABLE[packageName]?.second ?: appLabel.take(2).ifBlank { "?" }

    /** 沒登記的 App 用 package name 的雜湊挑一個穩定的藍灰色調。 */
    private fun fallbackColor(packageName: String): Color {
        val palette = listOf(
            Color(0xFF3E5C82), Color(0xFF4A6B8A), Color(0xFF2F4A6B),
            Color(0xFF56708F), Color(0xFF34506E),
        )
        return palette[(packageName.hashCode().and(Int.MAX_VALUE)) % palette.size]
    }
}

/** App 圖示。有系統圖示就用系統的，沒有就退回品牌色字塊。 */
@Composable
fun AppGlyph(
    packageName: String,
    appLabel: String,
    icon: ImageBitmap?,
    size: Int = 40,
) {
    val shape = if (size >= 40) NG.iconShape else NG.iconShapeSmall
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size.dp).clip(shape),
        )
        return
    }
    val color = Brand.color(packageName)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(shape)
            .background(if (packageName == "com.google.android.gm" || packageName == "com.android.chrome") Color.White else color),
        contentAlignment = Alignment.Center,
    ) {
        if (packageName == "jp.naver.line.android") {
            Canvas(Modifier.size((size * 0.82f).dp)) {
                drawOval(Color.White, topLeft = Offset(0f, this.size.height * 0.06f),
                    size = Size(this.size.width, this.size.height * 0.78f))
                drawPath(Path().apply {
                    moveTo(this@Canvas.size.width * 0.44f, this@Canvas.size.height * 0.72f)
                    lineTo(this@Canvas.size.width * 0.40f, this@Canvas.size.height * 0.98f)
                    lineTo(this@Canvas.size.width * 0.75f, this@Canvas.size.height * 0.70f)
                    close()
                }, Color.White)
            }
            Text("LINE", color = color, fontSize = (size * 0.22f).sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-size * 0.04f).dp))
        } else if (packageName == "com.google.android.youtube") {
            Canvas(Modifier.size((size * 0.70f).dp)) {
                drawRoundRect(Color.White, cornerRadius = androidx.compose.ui.geometry.CornerRadius(this.size.width * 0.18f),
                    topLeft = Offset(0f, this.size.height * 0.14f), size = Size(this.size.width, this.size.height * 0.72f))
                drawPath(Path().apply {
                    moveTo(this@Canvas.size.width * 0.40f, this@Canvas.size.height * 0.32f)
                    lineTo(this@Canvas.size.width * 0.70f, this@Canvas.size.height * 0.50f)
                    lineTo(this@Canvas.size.width * 0.40f, this@Canvas.size.height * 0.68f)
                    close()
                }, color)
            }
        } else if (packageName == "com.instagram.android") {
            Canvas(Modifier.size((size * 0.68f).dp)) {
                val stroke = Stroke(this.size.width * 0.08f)
                drawRoundRect(Color.White, cornerRadius = androidx.compose.ui.geometry.CornerRadius(this.size.width * 0.26f), style = stroke)
                drawCircle(Color.White, radius = this.size.width * 0.23f, style = stroke)
                drawCircle(Color.White, radius = this.size.width * 0.055f,
                    center = Offset(this.size.width * 0.77f, this.size.height * 0.23f))
            }
        } else if (packageName == "com.android.chrome") {
            Canvas(Modifier.size((size * 0.90f).dp)) {
                val diameter = this.size.width
                drawCircle(Color.White)
                val inset = diameter * 0.04f
                val bounds = Size(diameter - inset * 2, diameter - inset * 2)
                drawArc(Color(0xFFEA4335), 210f, 120f, true, Offset(inset, inset), bounds)
                drawArc(Color(0xFFFBBC05), 330f, 120f, true, Offset(inset, inset), bounds)
                drawArc(Color(0xFF34A853), 90f, 120f, true, Offset(inset, inset), bounds)
                drawCircle(Color.White, radius = diameter * 0.23f)
                drawCircle(Color(0xFF4285F4), radius = diameter * 0.18f)
            }
        } else if (packageName == "com.google.android.gm") {
            Canvas(Modifier.size((size * 0.72f).dp)) {
                val w = this.size.width
                val h = this.size.height
                val stroke = w * 0.14f
                drawLine(Color(0xFF4285F4), Offset(w * 0.08f, h * 0.25f), Offset(w * 0.08f, h * 0.86f), stroke)
                drawLine(Color(0xFF34A853), Offset(w * 0.92f, h * 0.25f), Offset(w * 0.92f, h * 0.86f), stroke)
                drawLine(Color(0xFFEA4335), Offset(w * 0.08f, h * 0.25f), Offset(w * 0.50f, h * 0.56f), stroke)
                drawLine(Color(0xFFEA4335), Offset(w * 0.50f, h * 0.56f), Offset(w * 0.92f, h * 0.25f), stroke)
            }
        } else if (packageName == "com.android.systemui") {
            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Outlined.Settings,
                null, tint = Color.White, modifier = Modifier.size((size * 0.76f).dp))
        } else {
        Text(
            text = Brand.short(packageName, appLabel),
            color = Color.White,
            fontSize = (size * 0.34f).sp,
            fontWeight = FontWeight.ExtraBold,
        )
        }
    }
}

/** 首頁三格統計中的一格。 */
@Composable
fun StatTile(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color = NG.ink,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NG.cardBrush)
            .border(1.dp, NG.line, RoundedCornerShape(14.dp))
            .padding(vertical = 13.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = NG.statValue, color = valueColor)
        Text(label, style = NG.caption, color = NG.inkFaint)
    }
}

/**
 * 設計稿的開關。Material3 的 Switch 尺寸與描邊都對不上，自己畫比覆寫樣式短。
 */
@Composable
fun GuardSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
) {
    val track by animateColorAsState(
        targetValue = if (checked) NG.blue else NG.raise,
        label = "track",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 25.dp else 3.dp,
        label = "thumb",
    )
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(48.dp)
            .semantics { this.contentDescription = contentDescription }
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 9.dp)
            .clip(CircleShape)
            .background(track)
            .border(1.dp, if (checked) NG.blueLight.copy(alpha = 0.7f) else NG.line, CircleShape),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .align(Alignment.CenterStart)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

/** 藍色「允許」／紅色「攔截」徽章。 */
@Composable
fun StatusPill(blocked: Boolean, expanded: Boolean = false) {
    val fg = if (blocked) NG.redLight else NG.blueLight
    val bg = if (blocked) NG.redSoft else NG.blueSoft
    val border = if (blocked) NG.redLight.copy(alpha = 0.36f) else NG.blueLight.copy(alpha = 0.36f)
    val label = when {
        blocked && expanded -> "已攔截"
        blocked -> "攔截"
        expanded -> "已允許"
        else -> "允許"
    }
    Text(
        text = label,
        style = NG.pill,
        color = fg,
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

/** 全部 / 已攔截 / 已允許 三段切換。 */
@Composable
fun <T> SegmentedTabs(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NG.cardMuted)
            .border(1.dp, NG.lineSoft, RoundedCornerShape(12.dp))
            .selectableGroup()
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) NG.actionBlue else Color.Transparent)
                    .selectable(selected = active, role = Role.Tab, onClick = { onSelect(value) })
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 13.5f.sp,
                    fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                    color = if (active) Color.White else NG.inkMuted,
                )
            }
        }
    }
}

/** 清單底部的收尾說明。 */
@Composable
fun EndNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = NG.pill,
        color = NG.inkFaint,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

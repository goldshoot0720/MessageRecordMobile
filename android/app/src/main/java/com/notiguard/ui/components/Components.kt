package com.notiguard.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.annotation.DrawableRes
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.selection.selectableGroup
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
            GuardIcon(GuardIcons.Settings, null, modifier = Modifier.size((size * 0.72f).dp))
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

/**
 * 畫面底色：垂直漸層加左上角一圈藍霧。
 * 純色底在 OLED 上會讓卡片邊界看起來浮起來，加一點光暈才貼得住。
 */
fun Modifier.screenBackground(): Modifier = this
    .background(NG.screenBrush)
    .drawBehind {
        val center = Offset(size.width * 0.12f, 0f)
        val radius = size.width * 0.95f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x2E187BFF), Color(0x00187BFF)),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }

/** 藍底圓角方塊包一顆圖示，品牌列與卡片開頭都用它。 */
@Composable
fun GlyphBadge(
    @DrawableRes icon: Int,
    size: Int = 34,
    background: Color = NG.blueSoft,
    border: Color = NG.blueLight.copy(alpha = 0.4f),
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(NG.iconShapeSmall)
            .background(background)
            .border(1.dp, border, NG.iconShapeSmall),
        contentAlignment = Alignment.Center,
    ) {
        GuardIcon(icon, null, modifier = Modifier.size((size * 0.56f).dp))
    }
}

/** 導覽列上的圖示按鈕：48dp 觸控區、圓角底、按下去有回饋。 */
@Composable
fun IconAction(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "iconPress")
    Box(
        modifier = modifier
            .size(42.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(NG.chipShape)
            .background(if (filled) NG.cardMuted else Color.Transparent)
            .border(1.dp, if (filled) NG.lineSoft else Color.Transparent, NG.chipShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true, radius = 22.dp),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        GuardIcon(icon, contentDescription, modifier = Modifier.size(20.dp))
    }
}

/** 按鈕語氣。藍＝主要動作，紅＝破壞性，其餘用中性底或純文字。 */
enum class ButtonTone { Primary, Tonal, Danger, Quiet }

/**
 * 全 App 共用的按鈕。帶圖示、按下縮放，三種語氣共用同一組尺寸，
 * 上下並排時邊界會對齊。
 */
@Composable
fun GuardButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
    tone: ButtonTone = ButtonTone.Primary,
    fillWidth: Boolean = true,
    contentColor: Color? = null,
) {
    val content = contentColor ?: when (tone) {
        ButtonTone.Primary -> Color.White
        ButtonTone.Tonal -> NG.ink
        ButtonTone.Danger -> NG.redLight
        ButtonTone.Quiet -> NG.inkMuted
    }
    val fill: Brush = when (tone) {
        ButtonTone.Primary -> NG.actionBrush
        ButtonTone.Tonal -> NG.tonalBrush
        ButtonTone.Danger -> NG.dangerBrush
        ButtonTone.Quiet -> SolidColor(Color.Transparent)
    }
    val stroke = when (tone) {
        ButtonTone.Primary -> NG.blueLight.copy(alpha = 0.5f)
        ButtonTone.Tonal -> NG.line
        ButtonTone.Danger -> NG.redLight.copy(alpha = 0.55f)
        ButtonTone.Quiet -> Color.Transparent
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.975f else 1f, label = "buttonPress")

    Row(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(NG.buttonShape)
            .background(fill, NG.buttonShape)
            .border(if (tone == ButtonTone.Danger) 1.4.dp else 1.dp, stroke, NG.buttonShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = content),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            GuardIcon(icon, null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(label, fontSize = 15.5f.sp, fontWeight = FontWeight.Medium, color = content)
        if (trailingIcon != null) {
            Spacer(Modifier.width(9.dp))
            GuardIcon(trailingIcon, null, modifier = Modifier.size(19.dp))
        }
    }
}

/** 小尺寸的次要按鈕，放在區塊標題右邊或卡片內。 */
@Composable
fun ChipButton(
    label: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = NG.blueLight,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "chipPress")
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(NG.chipShape)
            .background(NG.cardMuted)
            .border(1.dp, NG.lineSoft, NG.chipShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = tint),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GuardIcon(icon, null, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, color = NG.inkMuted)
    }
}

/** 區塊標題，左邊一小段藍色標線。 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(NG.blueLight),
        )
        Spacer(Modifier.width(9.dp))
        Text(title, style = NG.sectionTitle, color = NG.ink)
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

/** 首頁三格統計中的一格。圖示與數字同色，一眼分得出三格在講什麼。 */
@Composable
fun StatTile(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color = NG.ink,
    @DrawableRes icon: Int? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NG.cardBrush)
            .border(1.dp, NG.line, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(valueColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                GuardIcon(icon, null, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.height(7.dp))
        }
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
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .padding(start = 8.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(fg))
        Spacer(Modifier.width(5.dp))
        Text(text = label, style = NG.pill, color = fg)
    }
}

/** 全部 / 已攔截 / 已允許 三段切換。 */
@Composable
fun <T> SegmentedTabs(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    iconFor: (T) -> Int? = { null },
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
            val background by animateColorAsState(
                targetValue = if (active) NG.actionBlue else Color.Transparent,
                label = "segment",
            )
            val content by animateColorAsState(
                targetValue = if (active) Color.White else NG.inkMuted,
                label = "segmentInk",
            )
            val icon = iconFor(value)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(background)
                    .selectable(selected = active, role = Role.Tab, onClick = { onSelect(value) })
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    GuardIcon(icon, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = label,
                    fontSize = 13.5f.sp,
                    fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                    color = content,
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

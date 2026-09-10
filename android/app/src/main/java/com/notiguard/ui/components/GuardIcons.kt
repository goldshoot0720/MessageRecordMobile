package com.notiguard.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * 全套自繪向量圖示（等同 SVG path），24×24 畫布、1.7 線寬、圓端圓角。
 *
 * 不用 material-icons-extended：那包會多帶幾 MB 進 APK，而且它的線寬與圓角
 * 跟這支 App 的設計語言對不上。這裡的每個圖示都照同一組參數畫，混用時不會有粗細差。
 */
object GuardIcons {

    // ---- 通知／狀態 ----

    val Bell = outline("Bell") {
        moveTo(18f, 8f); curveTo(18f, 4.8f, 16f, 3f, 13.5f, 2.7f)
        verticalLineTo(2f); curveTo(13.5f, 0.7f, 10.5f, 0.7f, 10.5f, 2f)
        verticalLineTo(2.7f); curveTo(8f, 3f, 6f, 4.8f, 6f, 8f)
        curveTo(6f, 14f, 4f, 14.5f, 4f, 17f)
        curveTo(4f, 18f, 5f, 18f, 6f, 18f); horizontalLineTo(18f)
        curveTo(19f, 18f, 20f, 18f, 20f, 17f)
        curveTo(20f, 14.5f, 18f, 14f, 18f, 8f); close()
        moveTo(9f, 21f); curveTo(10f, 23f, 14f, 23f, 15f, 21f)
    }

    /** 攔截中的鈴鐺：鈴鐺加一道斜槓。 */
    val BellOff = outline("BellOff") {
        moveTo(18f, 8f); curveTo(18f, 4.8f, 16f, 3f, 13.5f, 2.7f)
        verticalLineTo(2f); curveTo(13.5f, 0.7f, 10.5f, 0.7f, 10.5f, 2f)
        verticalLineTo(2.7f); curveTo(8f, 3f, 6f, 4.8f, 6f, 8f)
        curveTo(6f, 14f, 4f, 14.5f, 4f, 17f)
        curveTo(4f, 18f, 5f, 18f, 6f, 18f); horizontalLineTo(18f)
        curveTo(19f, 18f, 20f, 18f, 20f, 17f)
        curveTo(20f, 14.5f, 18f, 14f, 18f, 8f); close()
        moveTo(3.5f, 2.5f); lineTo(20.5f, 21.5f)
    }

    /** 盾牌：權限頁與品牌列的主視覺。 */
    val Shield = outline("Shield") {
        moveTo(12f, 2.4f); lineTo(20f, 5.8f)
        verticalLineTo(11.6f)
        curveTo(20f, 16.9f, 16.4f, 20.4f, 12f, 21.6f)
        curveTo(7.6f, 20.4f, 4f, 16.9f, 4f, 11.6f)
        verticalLineTo(5.8f); close()
        moveTo(8.6f, 11.9f); lineTo(11.2f, 14.5f); lineTo(15.6f, 9.6f)
    }

    val Clock = outline("Clock") {
        circle(12f, 12f, 10f)
        moveTo(12f, 6f); verticalLineTo(12f); lineTo(16f, 14f)
    }

    val Message = outline("Message") {
        moveTo(5f, 3f); horizontalLineTo(19f); quadTo(22f, 3f, 22f, 6f)
        verticalLineTo(15f); quadTo(22f, 18f, 19f, 18f)
        horizontalLineTo(10f); lineTo(5f, 22f); verticalLineTo(18f)
        quadTo(2f, 18f, 2f, 15f); verticalLineTo(6f); quadTo(2f, 3f, 5f, 3f); close()
        moveTo(6f, 8f); horizontalLineTo(18f)
        moveTo(6f, 12f); horizontalLineTo(16f)
    }

    val Block = outline("Block") {
        circle(12f, 12f, 10f)
        moveTo(5f, 5f); lineTo(19f, 19f)
    }

    val Check = outline("Check") {
        circle(12f, 12f, 10f)
        moveTo(7f, 12f); lineTo(10.5f, 15.5f); lineTo(17f, 8f)
    }

    /** 沒有外框的勾，放在主要按鈕上比圓框版清爽。 */
    val CheckMark = outline("CheckMark") {
        moveTo(4.5f, 12.6f); lineTo(9.5f, 17.6f); lineTo(19.5f, 6.4f)
    }

    // ---- 導覽 ----

    val Back = outline("Back", mirrored = true) {
        moveTo(15f, 3f); lineTo(6f, 12f); lineTo(15f, 21f)
    }

    val ChevronRight = outline("ChevronRight", mirrored = true) {
        moveTo(9f, 4.5f); lineTo(16.5f, 12f); lineTo(9f, 19.5f)
    }

    val Home = outline("Home") {
        moveTo(2.8f, 11.4f); lineTo(12f, 3.2f); lineTo(21.2f, 11.4f)
        moveTo(5.4f, 9.6f); verticalLineTo(19f); quadTo(5.4f, 21f, 7.4f, 21f)
        horizontalLineTo(16.6f); quadTo(18.6f, 21f, 18.6f, 19f); verticalLineTo(9.6f)
        moveTo(9.6f, 21f); verticalLineTo(14.6f); horizontalLineTo(14.4f); verticalLineTo(21f)
    }

    /** 長條圖：統計頁籤。 */
    val Chart = outline("Chart") {
        moveTo(3f, 20.6f); horizontalLineTo(21f)
        rect(4.2f, 12.2f, 4.4f, 6.2f, 1.2f)
        rect(9.8f, 8.4f, 4.4f, 10f, 1.2f)
        rect(15.4f, 4.6f, 4.4f, 13.8f, 1.2f)
    }

    /** 齒輪：外圈牙數用三角函數算，才不會出現手繪的歪斜。 */
    val Settings = outline("Settings") {
        gear(12f, 12f, outer = 9.6f, inner = 7.4f, teeth = 8)
        circle(12f, 12f, 3.4f)
    }

    val Search = outline("Search") {
        circle(10.6f, 10.6f, 7.4f)
        moveTo(16.1f, 16.1f); lineTo(21f, 21f)
    }

    val Close = outline("Close") {
        moveTo(5.6f, 5.6f); lineTo(18.4f, 18.4f)
        moveTo(18.4f, 5.6f); lineTo(5.6f, 18.4f)
    }

    /** 直式三點的「更多」。線寬夠粗，小圓看起來就是實心點。 */
    val More = outline("More") {
        circle(12f, 5.2f, 0.75f)
        circle(12f, 12f, 0.75f)
        circle(12f, 18.8f, 0.75f)
    }

    // ---- 動作 ----

    val Filter = outline("Filter") {
        moveTo(3.2f, 5f); horizontalLineTo(20.8f); lineTo(14f, 13.2f)
        verticalLineTo(20.4f); lineTo(10f, 18.2f); verticalLineTo(13.2f); close()
    }

    /** 匯出：托盤加一支往上的箭頭。 */
    val Export = outline("Export") {
        moveTo(12f, 15.4f); verticalLineTo(3.2f)
        moveTo(7.4f, 7.8f); lineTo(12f, 3.2f); lineTo(16.6f, 7.8f)
        moveTo(4f, 14.6f); verticalLineTo(18.8f); quadTo(4f, 20.8f, 6f, 20.8f)
        horizontalLineTo(18f); quadTo(20f, 20.8f, 20f, 18.8f); verticalLineTo(14.6f)
    }

    val Trash = outline("Trash") {
        moveTo(3.6f, 6.4f); horizontalLineTo(20.4f)
        moveTo(9.4f, 6.4f); verticalLineTo(4.2f); horizontalLineTo(14.6f); verticalLineTo(6.4f)
        moveTo(5.8f, 6.4f); verticalLineTo(19f); quadTo(5.8f, 21f, 7.8f, 21f)
        horizontalLineTo(16.2f); quadTo(18.2f, 21f, 18.2f, 19f); verticalLineTo(6.4f)
        moveTo(10.2f, 10.2f); verticalLineTo(17.2f)
        moveTo(13.8f, 10.2f); verticalLineTo(17.2f)
    }

    /** 重試：缺一角的圓加箭頭。 */
    val Refresh = outline("Refresh") {
        arc(12f, 12f, 8f, start = -50f, sweep = 290f)
        moveTo(13.6f, 1.6f); lineTo(17.1f, 5.9f); lineTo(12.2f, 7.2f)
    }

    val Tag = outline("Tag") {
        moveTo(11.2f, 2.4f); horizontalLineTo(4.4f); quadTo(2.4f, 2.4f, 2.4f, 4.4f)
        verticalLineTo(11.2f); lineTo(12.8f, 21.6f); lineTo(21.6f, 12.8f); close()
        circle(7f, 7f, 1.6f)
    }

    val Info = outline("Info") {
        circle(12f, 12f, 9.6f)
        moveTo(12f, 11f); verticalLineTo(16.8f)
        circle(12f, 7.4f, 0.6f)
    }

    /** 應用程式清單：四宮格。 */
    val Apps = outline("Apps") {
        rect(3.2f, 3.2f, 7.4f, 7.4f, 2f)
        rect(13.4f, 3.2f, 7.4f, 7.4f, 2f)
        rect(3.2f, 13.4f, 7.4f, 7.4f, 2f)
        rect(13.4f, 13.4f, 7.4f, 7.4f, 2f)
    }

    // ---- 繪圖基本件 ----

    /** 圓：用四段三次貝茲逼近，誤差小於 0.03%。 */
    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        arc(cx, cy, r, start = 0f, sweep = 360f)
        close()
    }

    /** 圓角矩形。 */
    private fun PathBuilder.rect(x: Float, y: Float, w: Float, h: Float, radius: Float) {
        val r = minOf(radius, w / 2f, h / 2f)
        moveTo(x + r, y)
        lineTo(x + w - r, y); quadTo(x + w, y, x + w, y + r)
        lineTo(x + w, y + h - r); quadTo(x + w, y + h, x + w - r, y + h)
        lineTo(x + r, y + h); quadTo(x, y + h, x, y + h - r)
        lineTo(x, y + r); quadTo(x, y, x + r, y)
        close()
    }

    /**
     * 任意角度的圓弧。每 90° 切一段三次貝茲，控制點距離用
     * k = 4/3·tan(θ/4) 求得（SVG 轉貝茲的標準做法）。
     */
    private fun PathBuilder.arc(cx: Float, cy: Float, r: Float, start: Float, sweep: Float) {
        val segments = ceil(abs(sweep) / 90f).toInt().coerceAtLeast(1)
        val step = sweep / segments
        val k = 4f / 3f * tan(rad(step) / 4f)
        var a0 = start
        moveTo(cx + r * cos(rad(a0)), cy + r * sin(rad(a0)))
        repeat(segments) {
            val a1 = a0 + step
            val x0 = cx + r * cos(rad(a0)); val y0 = cy + r * sin(rad(a0))
            val x1 = cx + r * cos(rad(a1)); val y1 = cy + r * sin(rad(a1))
            curveTo(
                x0 - k * r * sin(rad(a0)), y0 + k * r * cos(rad(a0)),
                x1 + k * r * sin(rad(a1)), y1 - k * r * cos(rad(a1)),
                x1, y1,
            )
            a0 = a1
        }
    }

    /** 齒輪外框：每顆牙兩個外半徑點、兩個內半徑點。 */
    private fun PathBuilder.gear(cx: Float, cy: Float, outer: Float, inner: Float, teeth: Int) {
        val step = 360f / teeth
        repeat(teeth) { i ->
            val a = i * step
            val pts = listOf(
                (a - step * 0.20f) to outer,
                (a + step * 0.20f) to outer,
                (a + step * 0.30f) to inner,
                (a + step * 0.70f) to inner,
            )
            pts.forEachIndexed { index, (angle, radius) ->
                val x = cx + radius * cos(rad(angle))
                val y = cy + radius * sin(rad(angle))
                if (i == 0 && index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        close()
    }

    private fun rad(degrees: Float): Float = (degrees * PI / 180f).toFloat()

    private fun outline(
        name: String,
        mirrored: Boolean = false,
        draw: PathBuilder.() -> Unit,
    ): ImageVector = ImageVector.Builder(
        name, 24.dp, 24.dp, 24f, 24f, autoMirror = mirrored,
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            pathBuilder = draw,
        )
    }.build()
}

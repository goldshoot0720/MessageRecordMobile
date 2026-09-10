package com.notiguard.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Shared outline icons matching the notification controls in the reference. */
object GuardIcons {
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
    val Back = outline("Back", mirrored = true) {
        moveTo(15f, 3f); lineTo(6f, 12f); lineTo(15f, 21f)
    }
    val Clock = outline("Clock") {
        moveTo(22f, 12f); curveTo(22f, 25.3f, 2f, 25.3f, 2f, 12f)
        curveTo(2f, -1.3f, 22f, -1.3f, 22f, 12f); close()
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
        moveTo(22f, 12f); curveTo(22f, 25.3f, 2f, 25.3f, 2f, 12f)
        curveTo(2f, -1.3f, 22f, -1.3f, 22f, 12f); close()
        moveTo(5f, 5f); lineTo(19f, 19f)
    }
    val Check = outline("Check") {
        moveTo(22f, 12f); curveTo(22f, 25.3f, 2f, 25.3f, 2f, 12f)
        curveTo(2f, -1.3f, 22f, -1.3f, 22f, 12f); close()
        moveTo(7f, 12f); lineTo(10.5f, 15.5f); lineTo(17f, 8f)
    }

    private fun outline(
        name: String,
        mirrored: Boolean = false,
        draw: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit,
    ): ImageVector = ImageVector.Builder(
        name, 24.dp, 24.dp, 24f, 24f, autoMirror = mirrored,
    ).apply {
        path(stroke = SolidColor(Color.White), strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            pathBuilder = draw)
    }.build()
}

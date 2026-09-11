package com.notiguard.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notiguard.ui.components.ButtonTone
import com.notiguard.ui.components.GuardButton
import com.notiguard.ui.components.GuardIcon
import com.notiguard.ui.components.GuardIcons
import com.notiguard.ui.components.screenBackground
import com.notiguard.ui.theme.NG

/**
 * 未授權時的引導頁。通知存取沒有 runtime dialog，只能導向系統設定。
 */
@Composable
fun PermissionScreen(
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeroShield()

        Spacer(Modifier.height(26.dp))
        Text("開啟通知存取", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = NG.ink)
        Spacer(Modifier.height(10.dp))
        Text(
            "MessageRecord 需要「通知存取」權限才能攔截與記錄通知。\n" +
                "所有紀錄只存在這台裝置上，不會上傳。",
            style = NG.body,
            color = NG.inkMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NG.cardShape)
                .background(NG.cardBrush)
                .border(1.dp, NG.line, NG.cardShape)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Step(1, GuardIcons.Settings, "點下方按鈕開啟系統設定")
            Step(2, GuardIcons.Search, "在清單中找到 MessageRecord")
            Step(3, GuardIcons.CheckMark, "把開關打開並允許")
        }

        Spacer(Modifier.height(24.dp))
        GuardButton("前往設定開啟", onOpenSettings, icon = GuardIcons.Settings)
        Spacer(Modifier.height(8.dp))
        GuardButton(
            "稍後設定，先查看紀錄",
            onSkip,
            icon = GuardIcons.ChevronRight,
            tone = ButtonTone.Quiet,
        )
    }
}

/** 主視覺：盾牌加一圈慢慢呼吸的光暈，讓空白的權限頁不那麼死。 */
@Composable
private fun HeroShield() {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "pulseScale",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0x33187BFF), Color(0x00187BFF))),
                ),
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(NG.blueSoft)
                .border(1.dp, NG.blueLight.copy(alpha = 0.42f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            GuardIcon(GuardIcons.Shield, null, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun Step(index: Int, icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NG.blueSoft)
                .border(1.dp, NG.blueLight.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            GuardIcon(icon, null, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = NG.inkMuted, modifier = Modifier.weight(1f))
        Text("$index", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NG.inkFaint)
    }
}

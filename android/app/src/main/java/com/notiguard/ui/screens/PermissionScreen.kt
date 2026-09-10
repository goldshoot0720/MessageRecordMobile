package com.notiguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .background(NG.base)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(NG.blueSoft)
                .border(1.dp, NG.blueLight.copy(alpha = 0.42f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Notifications, null, tint = NG.blueLight, modifier = Modifier.size(46.dp))
        }

        Spacer(Modifier.height(28.dp))
        Text("開啟通知存取", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = NG.ink)
        Spacer(Modifier.height(10.dp))
        Text(
            "MessageRecord 需要「通知存取」權限才能攔截與記錄通知。\n" +
                "所有紀錄只存在這台裝置上，不會上傳。",
            style = NG.body,
            color = NG.inkMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(26.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NG.cardShape)
                .background(NG.card)
                .border(1.dp, NG.line, NG.cardShape)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Step(1, "點下方按鈕開啟系統設定")
            Step(2, "在清單中找到 MessageRecord")
            Step(3, "把開關打開並允許")
        }

        Spacer(Modifier.height(26.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NG.buttonShape)
                .background(NG.actionBlue)
                .clickable(onClick = onOpenSettings)
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("前往設定開啟", fontSize = 15.5f.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "稍後設定，先查看紀錄",
            fontSize = 14.5f.sp,
            color = NG.inkMuted,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onSkip)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun Step(index: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(NG.blueSoft)
                .border(1.dp, NG.blueLight.copy(alpha = 0.4f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("$index", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NG.blueLight)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = NG.inkMuted)
    }
}


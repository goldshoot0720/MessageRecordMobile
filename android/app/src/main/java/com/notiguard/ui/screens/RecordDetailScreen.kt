package com.notiguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordSource
import com.notiguard.ui.Fmt
import com.notiguard.ui.components.AppGlyph
import com.notiguard.ui.components.StatusPill
import com.notiguard.ui.theme.NG
import com.notiguard.ui.components.GuardIcons

@Composable
fun RecordDetailScreen(
    record: NotificationRecord?,
    blocking: Boolean,
    icon: ImageBitmap?,
    onBack: () -> Unit,
    onAllowApp: () -> Unit,
    onBlockApp: () -> Unit,
    onForgetApp: () -> Unit,
) {
    var confirmRemoval by remember { mutableStateOf(false) }
    if (confirmRemoval && record != null) {
        AlertDialog(onDismissRequest = { confirmRemoval = false },
            title = { Text("移除 ${record.appLabel} 的紀錄？") },
            text = { Text("將刪除此應用程式的全部通知紀錄及攔截規則。此動作無法復原。") },
            confirmButton = { TextButton(onClick = { confirmRemoval = false; onForgetApp() }) { Text("移除", color = NG.redLight) } },
            dismissButton = { TextButton(onClick = { confirmRemoval = false }) { Text("取消") } })
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NG.base)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                GuardIcons.Back,
                contentDescription = "返回",
                tint = NG.ink,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .clickable(onClick = onBack)
                    .padding(14.dp)
                    .size(20.dp),
            )
            Text(
                if (record?.blocked == true) "攔截詳情" else "通知詳情",
                style = NG.navTitle,
                color = NG.ink,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.width(48.dp))
        }

        if (record == null) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("這筆紀錄已不存在。", style = NG.body, color = NG.inkFaint)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // ---- 詳情卡 ----
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(NG.cardShape)
                    .background(NG.cardBrush)
                    .border(1.dp, NG.line, NG.cardShape),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppGlyph(record.packageName, record.appLabel, icon, size = 48)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            record.appLabel,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = NG.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(Fmt.longDateTime(record.postedAt), style = NG.caption, color = NG.inkFaint)
                    }
                    Spacer(Modifier.width(8.dp))
                    StatusPill(blocked = record.blocked, expanded = true)
                }

                // 通知內容
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NG.cardMuted)
                        .border(1.dp, NG.lineSoft, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                ) {
                    if (record.title.isNotBlank() && !record.text.startsWith(record.title)) {
                        Text(record.title, fontSize = 14.5f.sp, fontWeight = FontWeight.Medium, color = NG.ink)
                    }
                    if (record.text.isNotBlank()) {
                        Text(record.text, style = NG.body, color = if (record.title.isBlank()) NG.ink else NG.inkMuted)
                    }
                }

                MetaRow(GuardIcons.Bell, "來源應用程式", record.appLabel)
                MetaRow(GuardIcons.Clock, "通知時間", Fmt.timestamp(record.postedAt))
                MetaRow(GuardIcons.Message, "通知類型", record.category ?: "未分類")
                MetaRow(GuardIcons.Block, "處理動作", if (record.blocked) "攔截" else "允許")
                if (record.removedAt != null) {
                    MetaRow(GuardIcons.Check, "來源撤回時間", Fmt.timestamp(record.removedAt))
                }
                MetaRow(
                    GuardIcons.Bell,
                    "紀錄來源",
                    when (record.source) {
                        RecordSource.ANDROID_LISTENER -> "本機通知監聽"
                        RecordSource.COMPANION_SYNC -> "配對裝置同步"
                        RecordSource.SEED -> "示範資料"
                        else -> record.source
                    },
                )
            }

            // ---- 處理動作 ----
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (blocking) {
                    PrimaryButton("允許此應用程式通知", onClick = onAllowApp)
                    DangerButton("繼續攔截", onClick = onBlockApp)
                } else {
                    PrimaryButton("已允許此應用程式通知", onClick = onAllowApp)
                    DangerButton("改為攔截此應用程式", onClick = onBlockApp)
                }
                Text(
                    "從此應用程式移除",
                    fontSize = 14.5f.sp,
                    color = NG.redLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { confirmRemoval = true }
                        .padding(vertical = 16.dp),
                )
            }

            Text(
                "攔截的通知不會出現在系統通知欄，但紀錄永久保留",
                style = NG.pill,
                color = NG.inkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
            )
        }
    }
}

@Composable
private fun MetaRow(icon: ImageVector, label: String, value: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(NG.lineSoft),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Icon(icon, null, tint = NG.inkFaint, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(13.dp))
        Column {
            Text(label, style = NG.caption, color = NG.inkFaint)
            Text(value, fontSize = 15.sp, color = NG.ink)
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NG.buttonShape)
            .background(NG.actionBrush)
            .border(1.dp, NG.blueLight.copy(alpha = 0.5f), NG.buttonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(GuardIcons.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, fontSize = 15.5f.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
private fun DangerButton(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NG.buttonShape)
            .border(1.5.dp, NG.redLight, NG.buttonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(GuardIcons.Block, null, tint = NG.redLight, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, fontSize = 15.5f.sp, fontWeight = FontWeight.Medium, color = NG.redLight)
    }
}

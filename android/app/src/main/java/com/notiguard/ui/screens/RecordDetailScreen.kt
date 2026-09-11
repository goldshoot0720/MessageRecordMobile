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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordSource
import com.notiguard.ui.Fmt
import com.notiguard.ui.components.AppGlyph
import com.notiguard.ui.components.ButtonTone
import com.notiguard.ui.components.GuardButton
import com.notiguard.ui.components.IconAction
import com.notiguard.ui.components.StatusPill
import com.notiguard.ui.components.screenBackground
import com.notiguard.ui.theme.NG
import com.notiguard.ui.components.GuardIcon
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
            containerColor = NG.card,
            shape = NG.cardShape,
            icon = {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(NG.iconShapeSmall)
                        .background(NG.redSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    GuardIcon(GuardIcons.Trash, null, modifier = Modifier.size(20.dp))
                }
            },
            title = { Text("移除 ${record.appLabel} 的紀錄？", style = NG.sectionTitle, color = NG.ink) },
            text = { Text("將刪除此應用程式的全部通知紀錄及攔截規則。此動作無法復原。", style = NG.body, color = NG.inkMuted) },
            confirmButton = { TextButton(onClick = { confirmRemoval = false; onForgetApp() }) { Text("移除", color = NG.redLight) } },
            dismissButton = { TextButton(onClick = { confirmRemoval = false }) { Text("取消", color = NG.inkMuted) } })
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAction(GuardIcons.Back, "返回", onBack)
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
                MetaRow(GuardIcons.Tag, "通知類型", record.category ?: "未分類")
                MetaRow(
                    if (record.blocked) GuardIcons.Block else GuardIcons.CheckMark,
                    "處理動作",
                    if (record.blocked) "攔截" else "允許",
                    tint = if (record.blocked) NG.redLight else NG.blueLight,
                )
                if (record.removedAt != null) {
                    MetaRow(GuardIcons.Refresh, "來源撤回時間", Fmt.timestamp(record.removedAt))
                }
                MetaRow(
                    GuardIcons.Info,
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
                    GuardButton("允許此應用程式通知", onAllowApp, icon = GuardIcons.CheckMark)
                    GuardButton("繼續攔截", onBlockApp, icon = GuardIcons.Block, tone = ButtonTone.Danger)
                } else {
                    GuardButton("已允許此應用程式通知", onAllowApp, icon = GuardIcons.CheckMark)
                    GuardButton("改為攔截此應用程式", onBlockApp, icon = GuardIcons.Block, tone = ButtonTone.Danger)
                }
                GuardButton(
                    "從此應用程式移除",
                    { confirmRemoval = true },
                    icon = GuardIcons.Trash,
                    tone = ButtonTone.Quiet,
                    contentColor = NG.redLight,
                    modifier = Modifier.padding(top = 2.dp),
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
private fun MetaRow(icon: Int, label: String, value: String, tint: Color = NG.inkFaint) {
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
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            GuardIcon(icon, null, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = NG.caption, color = NG.inkFaint)
            Text(value, fontSize = 15.sp, color = NG.ink)
        }
    }
}

package com.notiguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.imePadding
import com.notiguard.ui.components.RecordSearchField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordFilter
import com.notiguard.ui.AppDetailUiState
import com.notiguard.ui.Fmt
import com.notiguard.ui.components.AppGlyph
import com.notiguard.ui.components.EndNote
import com.notiguard.ui.components.GuardSwitch
import com.notiguard.ui.components.SegmentedTabs
import com.notiguard.ui.components.StatusPill
import com.notiguard.ui.theme.NG
import com.notiguard.ui.components.GuardIcons

@Composable
fun AppDetailScreen(
    state: AppDetailUiState,
    icon: ImageBitmap?,
    onBack: () -> Unit,
    onSetFilter: (RecordFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onSetBlocking: (Boolean) -> Unit,
    onOpenRecord: (NotificationRecord) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var query by rememberSaveable(state.packageName) { mutableStateOf(state.query) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NG.base)
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // ---- 導覽列 ----
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
                state.appLabel,
                style = NG.navTitle,
                color = NG.ink,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, "更多選項", tint = NG.inkMuted)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text(if (state.blocking) "允許此應用程式通知" else "攔截此應用程式通知") },
                        onClick = { onSetBlocking(!state.blocking); menuOpen = false })
                    DropdownMenuItem(text = { Text("顯示全部紀錄") },
                        onClick = { query = ""; onQueryChange(""); onSetFilter(RecordFilter.ALL); menuOpen = false })
                }
            }
        }

        // ---- App 卡 ----
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(NG.cardShape)
                .background(NG.cardBrush)
                .border(1.dp, NG.line, NG.cardShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppGlyph(state.packageName, state.appLabel, icon, size = 60)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(state.appLabel, style = NG.heroName, color = NG.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "共 ${state.total} 則通知",
                    fontSize = 13.sp,
                    color = NG.inkFaint,
                )
            }
            Spacer(Modifier.width(8.dp))
            GuardSwitch(
                checked = state.blocking,
                onCheckedChange = onSetBlocking,
                contentDescription = "攔截這支應用程式的通知",
            )
        }

        RecordSearchField(query, { value ->
            query = value.trim()
            onQueryChange(value)
        }, label = "搜尋通知標題或內文",
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp))

        // ---- 三段切換 ----
        SegmentedTabs(
            options = listOf(
                RecordFilter.ALL to "全部",
                RecordFilter.BLOCKED to "已攔截",
                RecordFilter.ALLOWED to "已允許",
            ),
            selected = state.filter,
            onSelect = onSetFilter,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )

        // ---- 紀錄清單，依日期分組 ----
        if (state.records.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    if (query.isNotBlank()) "目前篩選下沒有符合的通知。\n試試其他關鍵字、清除搜尋或切換分頁。" else when (state.filter) {
                        RecordFilter.BLOCKED -> "這支應用程式目前沒有被攔截的通知。\n切回「全部」看看完整紀錄。"
                        RecordFilter.ALLOWED -> "這支應用程式目前沒有被放行的通知。\n切回「全部」看看完整紀錄。"
                        RecordFilter.ALL -> "還沒有這支應用程式的紀錄。"
                    },
                    style = NG.body,
                    color = NG.inkFaint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(40.dp),
                )
            }
        } else {
            val groups = state.records
                .groupBy { Fmt.dayKey(it.postedAt) }
                .entries
                .sortedByDescending { it.key }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                groups.forEach { (dayKey, items) ->
                    item(key = "h$dayKey") {
                        Row(
                            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                Fmt.dayLabel(dayKey),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NG.ink,
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(Fmt.daySub(dayKey), style = NG.caption, color = NG.inkFaint)
                        }
                    }
                    item(key = "line$dayKey") {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(NG.lineSoft)) {
                            Box(Modifier.width(28.dp).height(1.dp).background(NG.blueLight))
                        }
                    }
                    items(items.size, key = { "r${items[it].id}" }) { index ->
                        val record = items[index]
                        RecordRow(
                            record = record,
                            icon = icon,
                            onClick = { onOpenRecord(record) },
                        )
                        HorizontalDivider(color = NG.lineSoft)
                    }
                }
                item { EndNote(if (query.isBlank()) "已顯示 ${state.records.size} 則通知" else "找到 ${state.records.size} 則符合的通知") }
            }
        }
    }
}

@Composable
private fun RecordRow(
    record: NotificationRecord,
    icon: ImageBitmap?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppGlyph(record.packageName, record.appLabel, icon, size = 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    record.appLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NG.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(Fmt.time(record.postedAt), fontSize = 12.sp, color = NG.inkFaint)
            }
            Text(
                record.text.ifBlank { record.title },
                style = NG.summary,
                color = NG.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        StatusPill(blocked = record.blocked)
    }
}

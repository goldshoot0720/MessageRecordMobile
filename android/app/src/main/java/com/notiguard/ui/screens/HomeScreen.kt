package com.notiguard.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.notiguard.ui.components.RecordSearchField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notiguard.data.AppSummary
import com.notiguard.ui.HomeUiState
import com.notiguard.ui.components.AppGlyph
import com.notiguard.ui.components.EndNote
import com.notiguard.ui.components.GuardSwitch
import com.notiguard.ui.components.StatTile
import com.notiguard.ui.theme.NG
import com.notiguard.ui.components.GuardIcons

@Composable
fun HomeScreen(
    state: HomeUiState,
    iconFor: (String) -> ImageBitmap?,
    onToggleMaster: (Boolean) -> Unit,
    onOpenApp: (AppSummary, String) -> Unit,
    onQueryChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var selectedPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf(state.query) }
    var showStats by rememberSaveable { mutableStateOf(false) }
    val visibleApps = state.apps
    if (showStats) {
        AlertDialog(onDismissRequest = { showStats = false }, title = { Text("通知統計") },
            text = { Column {
                Text("累計通知：${state.stats.total}")
                Text("已攔截：${state.stats.blockedCount}")
                Text("已允許：${state.stats.total - state.stats.blockedCount}")
                Text("今日已攔截：${state.blockedToday}")
                Text("應用程式：${state.stats.appCount}")
            } }, confirmButton = { TextButton(onClick = { showStats = false }) { Text("完成") } })
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NG.base)
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // ---- 品牌列 ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(NG.iconShapeSmall)
                    .background(NG.blueSoft)
                    .border(1.dp, NG.blueLight.copy(alpha = 0.4f), NG.iconShapeSmall),
                contentAlignment = Alignment.Center,
            ) {
                Icon(GuardIcons.Bell, null, tint = NG.blueLight, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(11.dp))
            Text("MessageRecord", style = NG.brand, color = NG.ink)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "設定",
                tint = NG.inkMuted,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .clickable(onClick = onOpenSettings)
                    .padding(14.dp)
                    .size(21.dp),
            )
        }

        // ---- 總開關 ----
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(NG.cardShape)
                .background(NG.cardBrush)
                .border(1.dp, NG.line, NG.cardShape)
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(NG.iconShapeSmall)
                    .background(NG.blueSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(GuardIcons.Bell, null, tint = NG.blueLight, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (state.masterEnabled) "通知已啟用攔截" else "攔截已暫停",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = NG.ink,
                )
                Text(
                    if (state.masterEnabled) "今日已攔截 ${state.blockedToday} 則通知"
                    else "新通知將全部放行，紀錄照常保留",
                    style = NG.caption,
                    color = NG.inkFaint,
                )
            }
            Spacer(Modifier.width(8.dp))
            GuardSwitch(
                checked = state.masterEnabled,
                onCheckedChange = onToggleMaster,
                contentDescription = "啟用通知攔截",
            )
        }

        // ---- 三格統計 ----
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            StatTile(Modifier.weight(1f), "${state.stats.total}", "總通知", NG.blueLight)
            StatTile(
                Modifier.weight(1f),
                "${state.stats.blockedCount}",
                "已攔截",
                NG.green,
            )
            StatTile(Modifier.weight(1f), "${state.stats.appCount}", "應用程式", NG.blueLight)
        }

        // ---- 區塊標題 ----
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("應用程式", style = NG.sectionTitle, color = NG.ink)
        }
        RecordSearchField(query, { value ->
            query = value.trim()
            onQueryChange(value)
        },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

        // ---- App 清單 ----
        Box(Modifier.weight(1f)) {
            if (visibleApps.isEmpty()) {
                Text(
                    if (query.isNotBlank()) "找不到符合的應用程式或通知內容。\n試試其他關鍵字，或清除搜尋。" else "還沒有任何紀錄。\n授權通知存取後，新進的通知會即時出現在這裡。",
                    style = NG.body,
                    color = NG.inkFaint,
                    modifier = Modifier.align(Alignment.Center).padding(40.dp),
                )
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(visibleApps, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            icon = iconFor(app.packageName),
                            guarding = state.masterEnabled && app.isBlocking,
                            selected = app.packageName == (selectedPackage ?: state.apps.firstOrNull()?.packageName),
                            onClick = { selectedPackage = app.packageName; onOpenApp(app, query) },
                        )
                        Box(Modifier.fillMaxWidth().height(1.dp).background(NG.lineSoft))
                    }
                    item {
                        EndNote(if (query.isBlank()) "已顯示全部 ${state.stats.appCount} 個應用程式" else "找到 ${visibleApps.size} 個應用程式，點入查看符合的通知")
                    }
                }
            }
        }

        BottomTabs(onStats = { showStats = true }, onSettings = onOpenSettings)
    }
}

@Composable
private fun AppRow(
    app: AppSummary,
    icon: ImageBitmap?,
    guarding: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NG.rowShape)
            .clickable(onClick = onClick)
            .background(if (selected) NG.blueSoft else Color.Transparent)
            .border(1.dp, if (selected) NG.blue.copy(alpha = 0.7f) else Color.Transparent, NG.rowShape)
            .padding(horizontal = 5.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppGlyph(app.packageName, app.appLabel, icon, size = 40)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    app.appLabel,
                    style = NG.appName,
                    color = NG.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (guarding) {
                    Spacer(Modifier.width(7.dp))
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(NG.blue),
                    )
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "${app.total}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = NG.inkMuted,
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = NG.inkFaint,
            modifier = Modifier.padding(start = 16.dp).size(18.dp),
        )
    }
}

/**
 * 首頁導覽：統計開啟即時摘要，設定開啟系統通知存取設定。
 */
@Composable
private fun BottomTabs(onStats: () -> Unit, onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NG.surface)
            .border(width = 1.dp, color = NG.lineSoft, shape = RoundedCornerShape(0.dp))
            .padding(top = 10.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Tab("首頁", active = true, onClick = {}) { tint ->
            Icon(Icons.Filled.Home, null, tint = tint, modifier = Modifier.size(21.dp))
        }
        Tab("統計", active = false, onClick = onStats) { tint -> BarsIcon(tint) }
        Tab("設定", active = false, onClick = onSettings) { tint ->
            Icon(Icons.Outlined.Settings, null, tint = tint, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun Tab(label: String, active: Boolean, onClick: () -> Unit, icon: @Composable (Color) -> Unit) {
    val tint: Color = if (active) NG.blueLight else NG.inkFaint
    Column(modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        icon(tint)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = tint)
    }
}

/** 長條圖圖示。material-icons-core 沒有 BarChart，為了一顆圖示拉進 extended 不划算。 */
@Composable
private fun BarsIcon(tint: Color) {
    Canvas(Modifier.size(21.dp)) {
        val w = size.width / 7f
        val gap = w / 2f
        val heights = listOf(0.42f, 0.65f, 0.95f)
        heights.forEachIndexed { i, h ->
            val barHeight = size.height * h
            drawRoundRect(
                color = tint,
                topLeft = Offset(i * (w + gap) + gap, size.height - barHeight),
                size = Size(w, barHeight),
                cornerRadius = CornerRadius(w / 2.5f),
            )
        }
    }
}


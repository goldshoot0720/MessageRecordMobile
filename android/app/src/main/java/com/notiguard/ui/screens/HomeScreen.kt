package com.notiguard.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notiguard.data.AppSummary
import com.notiguard.ui.HomeUiState
import com.notiguard.ui.components.AppGlyph
import com.notiguard.ui.components.ChipButton
import com.notiguard.ui.components.EndNote
import com.notiguard.ui.components.GlyphBadge
import com.notiguard.ui.components.GuardSwitch
import com.notiguard.ui.components.IconAction
import com.notiguard.ui.components.SectionHeader
import com.notiguard.ui.components.StatTile
import com.notiguard.ui.components.screenBackground
import com.notiguard.ui.theme.NG
import com.notiguard.ui.components.GuardIcons

@Composable
fun HomeScreen(
    state: HomeUiState,
    iconFor: (String) -> ImageBitmap?,
    onToggleMaster: (Boolean) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenApp: (AppSummary) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var selectedPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var showStats by rememberSaveable { mutableStateOf(false) }
    val visibleApps = state.apps
    if (showStats) {
        StatsDialog(state = state, onDismiss = { showStats = false })
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        // ---- 品牌列 ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 20.dp, end = 10.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlyphBadge(GuardIcons.Shield, size = 36)
            Spacer(Modifier.width(11.dp))
            Column {
                Text("MessageRecord", style = NG.brand, color = NG.ink)
                Text(
                    if (state.masterEnabled) "攔截中" else "已暫停",
                    style = NG.pill,
                    color = if (state.masterEnabled) NG.blueLight else NG.inkFaint,
                )
            }
            Spacer(Modifier.weight(1f))
            IconAction(GuardIcons.Search, "搜尋通知", onOpenSearch)
            IconAction(GuardIcons.Settings, "設定", onOpenSettings)
        }

        // ---- 總開關 ----
        MasterCard(
            enabled = state.masterEnabled,
            blockedToday = state.blockedToday,
            onToggle = onToggleMaster,
        )

        // ---- 三格統計 ----
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            StatTile(Modifier.weight(1f), "${state.stats.total}", "總通知", NG.blueLight, GuardIcons.Message)
            StatTile(Modifier.weight(1f), "${state.stats.blockedCount}", "已攔截", NG.green, GuardIcons.Shield)
            StatTile(Modifier.weight(1f), "${state.stats.appCount}", "應用程式", NG.blueLight, GuardIcons.Apps)
        }

        // ---- 區塊標題 ----
        SectionHeader(
            title = "應用程式",
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp),
        ) {
            ChipButton("搜尋通知", GuardIcons.Search, onOpenSearch)
        }

        // ---- App 清單 ----
        Box(Modifier.weight(1f)) {
            if (visibleApps.isEmpty()) {
                EmptyApps(Modifier.align(Alignment.Center))
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
                            onClick = { selectedPackage = app.packageName; onOpenApp(app) },
                        )
                        Box(Modifier.fillMaxWidth().height(1.dp).background(NG.lineSoft))
                    }
                    item {
                        EndNote("已顯示全部 ${state.stats.appCount} 個應用程式")
                    }
                }
            }
        }

        BottomTabs(onStats = { showStats = true }, onSettings = onOpenSettings)
    }
}

/** 總開關卡。啟用時是藍的，暫停時整張卡退成灰的，遠看就知道現在守不守。 */
@Composable
private fun MasterCard(enabled: Boolean, blockedToday: Int, onToggle: (Boolean) -> Unit) {
    val accent by animateColorAsState(if (enabled) NG.blueLight else NG.inkFaint, label = "masterAccent")
    val edge by animateColorAsState(if (enabled) NG.blue.copy(alpha = 0.45f) else NG.line, label = "masterEdge")
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(NG.cardShape)
            .background(NG.cardBrush)
            .border(1.dp, edge, NG.cardShape)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphBadge(
            icon = if (enabled) GuardIcons.Shield else GuardIcons.BellOff,
            size = 36,
            tint = accent,
            background = if (enabled) NG.blueSoft else NG.cardMuted,
            border = accent.copy(alpha = 0.35f),
        )
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (enabled) "通知已啟用攔截" else "攔截已暫停",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = NG.ink,
            )
            Text(
                if (enabled) "今日已攔截 $blockedToday 則通知"
                else "新通知將全部放行，紀錄照常保留",
                style = NG.caption,
                color = NG.inkFaint,
            )
        }
        Spacer(Modifier.width(8.dp))
        GuardSwitch(
            checked = enabled,
            onCheckedChange = onToggle,
            contentDescription = "啟用通知攔截",
        )
    }
}

@Composable
private fun EmptyApps(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(NG.cardMuted)
                .border(1.dp, NG.lineSoft, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(GuardIcons.Bell, null, tint = NG.inkFaint, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("還沒有任何紀錄", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = NG.inkMuted)
        Spacer(Modifier.height(6.dp))
        Text(
            "授權通知存取後，新進的通知會即時出現在這裡。",
            style = NG.body,
            color = NG.inkFaint,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
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
            .padding(horizontal = 8.dp, vertical = 9.dp),
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
                    Box(Modifier.size(6.dp).clip(CircleShape).background(NG.blue))
                }
            }
            Text(
                if (app.blockedCount > 0) "已攔截 ${app.blockedCount} 則" else "全部放行",
                style = NG.caption,
                color = NG.inkFaint,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "${app.total}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = NG.inkMuted,
            modifier = Modifier
                .clip(CircleShape)
                .background(NG.cardMuted)
                .padding(horizontal = 10.dp, vertical = 3.dp),
        )
        Icon(
            GuardIcons.ChevronRight,
            contentDescription = null,
            tint = NG.inkFaint,
            modifier = Modifier.padding(start = 10.dp).size(16.dp),
        )
    }
}

/** 統計摘要。用自繪的列取代 AlertDialog 的預設排版，跟其他頁維持同一套色。 */
@Composable
private fun StatsDialog(state: HomeUiState, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NG.card,
        shape = NG.cardShape,
        icon = { GlyphBadge(GuardIcons.Chart, size = 38) },
        title = { Text("通知統計", style = NG.sectionTitle, color = NG.ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatLine(GuardIcons.Message, "累計通知", "${state.stats.total}", NG.blueLight)
                StatLine(GuardIcons.Shield, "已攔截", "${state.stats.blockedCount}", NG.green)
                StatLine(GuardIcons.Check, "已允許", "${state.stats.total - state.stats.blockedCount}", NG.blueLight)
                StatLine(GuardIcons.Clock, "今日已攔截", "${state.blockedToday}", NG.green)
                StatLine(GuardIcons.Apps, "應用程式", "${state.stats.appCount}", NG.blueLight)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成", color = NG.blueLight) }
        },
    )
}

@Composable
private fun StatLine(icon: ImageVector, label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NG.chipShape)
            .background(NG.cardMuted)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 14.sp, color = NG.inkMuted, modifier = Modifier.weight(1f))
        Text(value, style = NG.statValue, color = accent, fontSize = 17.sp)
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
            .padding(top = 8.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Tab("首頁", GuardIcons.Home, active = true, onClick = {})
        Tab("統計", GuardIcons.Chart, active = false, onClick = onStats)
        Tab("設定", GuardIcons.Settings, active = false, onClick = onSettings)
    }
}

@Composable
private fun Tab(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    val tint by animateColorAsState(if (active) NG.blueLight else NG.inkFaint, label = "tabTint")
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (active) NG.blueSoft else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 11.sp, color = tint)
    }
}

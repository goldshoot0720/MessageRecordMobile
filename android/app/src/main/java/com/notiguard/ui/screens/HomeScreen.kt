package com.notiguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.notiguard.data.AppSummary
import com.notiguard.data.NotificationRecord
import com.notiguard.data.TimeRange
import com.notiguard.ui.Fmt
import com.notiguard.ui.HomeUiState
import com.notiguard.ui.PopularApp
import com.notiguard.ui.PopularApps
import com.notiguard.ui.components.AppGlyph
import com.notiguard.ui.components.EndNote
import com.notiguard.ui.components.GlyphBadge
import com.notiguard.ui.components.GuardIcon
import com.notiguard.ui.components.GuardIcons
import com.notiguard.ui.components.GuardSwitch
import com.notiguard.ui.components.IconAction
import com.notiguard.ui.components.SegmentedTabs
import com.notiguard.ui.components.StatusPill
import com.notiguard.ui.components.TimeRangeBar
import com.notiguard.ui.components.screenBackground
import com.notiguard.ui.theme.NG

/** 主區要看單則通知，或依應用程式彙整。 */
private enum class HomeFeed { RECORDS, APPS }

/**
 * 首頁。上方控制最多佔四分之一；下面至少四分之三是可捲動的訊息或應用清單。
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    iconFor: (String) -> ImageBitmap?,
    installedFor: (String) -> Boolean,
    onToggleMaster: (Boolean) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenApp: (AppSummary) -> Unit,
    onOpenRecord: (NotificationRecord) -> Unit,
    onOpenPopular: (PopularApp) -> Unit,
    onOpenSettings: () -> Unit,
    onTimeRange: (TimeRange) -> Unit,
) {
    var showStats by rememberSaveable { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var feedName by rememberSaveable { mutableStateOf(HomeFeed.RECORDS.name) }
    val feed = HomeFeed.valueOf(feedName)
    val lifecycleOwner = LocalLifecycleOwner.current
    var resumeTick by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) resumeTick++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val visibleApps = state.apps
    val summaries = visibleApps.associateBy { it.packageName }
    val popular = remember(resumeTick) { PopularApps.ordered(installedFor) }
    if (showStats) {
        StatsDialog(state = state, onDismiss = { showStats = false })
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground()
            .navigationBarsPadding(),
    ) {
        val chromeCap = maxHeight * 0.25f
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = chromeCap)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(start = 16.dp, end = 6.dp, top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlyphBadge(
                        icon = if (state.masterEnabled) GuardIcons.Shield else GuardIcons.BellOff,
                        size = 32,
                        background = if (state.masterEnabled) NG.blueSoft else NG.cardMuted,
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "MessageRecord",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NG.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (state.masterEnabled) "攔截中 · 今日 ${state.blockedToday}"
                            else "已暫停 · 紀錄仍保留",
                            style = NG.pill,
                            color = if (state.masterEnabled) NG.blueLight else NG.inkFaint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    GuardSwitch(
                        checked = state.masterEnabled,
                        onCheckedChange = onToggleMaster,
                        contentDescription = "啟用通知攔截",
                    )
                    IconAction(GuardIcons.Search, "搜尋通知", onOpenSearch)
                    Box {
                        IconAction(GuardIcons.More, "更多", { menuOpen = true })
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            containerColor = NG.card,
                        ) {
                            DropdownMenuItem(
                                text = { Text("通知統計", color = NG.ink) },
                                leadingIcon = { GuardIcon(GuardIcons.Chart, null, modifier = Modifier.size(18.dp)) },
                                onClick = { menuOpen = false; showStats = true },
                            )
                            DropdownMenuItem(
                                text = { Text("通知存取設定", color = NG.ink) },
                                leadingIcon = { GuardIcon(GuardIcons.Settings, null, modifier = Modifier.size(18.dp)) },
                                onClick = { menuOpen = false; onOpenSettings() },
                            )
                        }
                    }
                }

                SegmentedTabs(
                    options = listOf(
                        HomeFeed.RECORDS to "通知紀錄",
                        HomeFeed.APPS to "應用程式",
                    ),
                    selected = feed,
                    onSelect = { feedName = it.name },
                    compact = true,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    iconFor = {
                        when (it) {
                            HomeFeed.RECORDS -> GuardIcons.Message
                            HomeFeed.APPS -> GuardIcons.Apps
                        }
                    },
                )

                TimeRangeBar(
                    selected = state.timeRange,
                    onSelect = onTimeRange,
                    compact = true,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (feed) {
                    HomeFeed.RECORDS -> RecordFeed(
                        records = state.records,
                        range = state.timeRange,
                        iconFor = iconFor,
                        onOpen = onOpenRecord,
                    )
                    HomeFeed.APPS -> AppFeed(
                        apps = visibleApps,
                        popular = popular,
                        summaries = summaries,
                        range = state.timeRange,
                        masterEnabled = state.masterEnabled,
                        iconFor = iconFor,
                        installedFor = installedFor,
                        onOpen = onOpenApp,
                        onOpenPopular = onOpenPopular,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordFeed(
    records: List<NotificationRecord>,
    range: TimeRange,
    iconFor: (String) -> ImageBitmap?,
    onOpen: (NotificationRecord) -> Unit,
) {
    if (records.isEmpty()) {
        EmptyFeed(
            title = "這個範圍還沒有通知",
            body = if (range == TimeRange.ALL) {
                "授權後，新的訊息與通知會出現在這裡。"
            } else {
                "目前是${range.label}。可以改選一個月、一年或全部。"
            },
            modifier = Modifier.fillMaxSize(),
        )
        return
    }
    val groups = records.groupBy { Fmt.dayKey(it.postedAt) }.entries.sortedByDescending { it.key }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        groups.forEach { (dayKey, dayRecords) ->
            item(key = "h$dayKey") {
                Text(
                    Fmt.dayLabel(dayKey),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NG.inkFaint,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            }
            items(dayRecords, key = { it.id }) { record ->
                MessageRow(
                    record = record,
                    icon = iconFor(record.packageName),
                    onClick = { onOpen(record) },
                )
            }
        }
        item { EndNote("已顯示 ${records.size} 則通知") }
    }
}

@Composable
private fun AppFeed(
    apps: List<AppSummary>,
    popular: List<PopularApp>,
    summaries: Map<String, AppSummary>,
    range: TimeRange,
    masterEnabled: Boolean,
    iconFor: (String) -> ImageBitmap?,
    installedFor: (String) -> Boolean,
    onOpen: (AppSummary) -> Unit,
    onOpenPopular: (PopularApp) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "popular") {
            PopularRail(
                apps = popular,
                summaries = summaries,
                iconFor = iconFor,
                installedFor = installedFor,
                onOpen = onOpenPopular,
            )
        }
        if (apps.isEmpty()) {
            item(key = "empty-apps") {
                EmptyFeed(
                    title = "這個範圍還沒有紀錄",
                    body = if (range == TimeRange.ALL) {
                        "點上方的 LINE 或其他常用應用先查看，授權後新通知也會出現在這裡。"
                    } else {
                        "目前是${range.label}。可以改選一個月、一年或全部。"
                    },
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                )
            }
        } else {
            items(apps, key = { it.packageName }) { app ->
                AppRow(
                    app = app,
                    icon = iconFor(app.packageName),
                    guarding = masterEnabled && app.isBlocking,
                    onClick = { onOpen(app) },
                )
            }
            item { EndNote("已顯示全部 ${apps.size} 個應用程式") }
        }
    }
}

@Composable
private fun MessageRow(
    record: NotificationRecord,
    icon: ImageBitmap?,
    onClick: () -> Unit,
) {
    val headline = record.title.ifBlank { record.text.ifBlank { record.appLabel } }
    val body = when {
        record.text.isBlank() -> ""
        record.text == headline -> ""
        else -> record.text
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NG.rowShape)
            .background(NG.card)
            .border(1.dp, NG.lineSoft, NG.rowShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AppGlyph(record.packageName, record.appLabel, icon, size = 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    record.appLabel,
                    style = NG.appName,
                    color = NG.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                Text(Fmt.time(record.postedAt), fontSize = 12.sp, color = NG.inkFaint)
            }
            Spacer(Modifier.height(3.dp))
            Text(
                headline,
                fontSize = 14.5f.sp,
                fontWeight = FontWeight.Medium,
                color = NG.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
            )
            if (body.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    body,
                    style = NG.summary,
                    color = NG.inkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        StatusPill(blocked = record.blocked)
    }
}

@Composable
private fun PopularRail(
    apps: List<PopularApp>,
    summaries: Map<String, AppSummary>,
    iconFor: (String) -> ImageBitmap?,
    installedFor: (String) -> Boolean,
    onOpen: (PopularApp) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(apps, key = { it.packageName }) { app ->
            PopularTile(
                app = app,
                summary = summaries[app.packageName],
                icon = iconFor(app.packageName),
                installed = installedFor(app.packageName),
                onClick = { onOpen(app) },
            )
        }
    }
}

@Composable
private fun PopularTile(
    app: PopularApp,
    summary: AppSummary?,
    icon: ImageBitmap?,
    installed: Boolean,
    onClick: () -> Unit,
) {
    val count = summary?.total ?: 0
    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppGlyph(
            packageName = app.packageName,
            appLabel = app.label,
            icon = icon,
            size = 32,
            modifier = Modifier.graphicsLayer { alpha = if (installed || count > 0) 1f else 0.45f },
        )
        Spacer(Modifier.height(3.dp))
        Text(
            app.label,
            fontSize = 10.sp,
            color = if (count > 0) NG.blueLight else NG.inkFaint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmptyFeed(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NG.cardMuted)
                .border(1.dp, NG.lineSoft, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            GuardIcon(GuardIcons.Bell, null, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = NG.inkMuted)
        Spacer(Modifier.height(6.dp))
        Text(body, style = NG.body, color = NG.inkFaint, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AppRow(
    app: AppSummary,
    icon: ImageBitmap?,
    guarding: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NG.rowShape)
            .background(NG.card)
            .border(1.dp, NG.lineSoft, NG.rowShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
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
        GuardIcon(
            GuardIcons.ChevronRight,
            null,
            modifier = Modifier.padding(start = 10.dp).size(16.dp),
        )
    }
}

@Composable
private fun StatsDialog(state: HomeUiState, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NG.card,
        shape = NG.cardShape,
        icon = { GlyphBadge(GuardIcons.Chart, size = 38) },
        title = {
            Column {
                Text("通知統計", style = NG.sectionTitle, color = NG.ink)
                Text(state.timeRange.label, style = NG.caption, color = NG.inkFaint)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatLine(GuardIcons.Message, "通知", "${state.stats.total}", NG.blueLight)
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
private fun StatLine(icon: Int, label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NG.chipShape)
            .background(NG.cardMuted)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GuardIcon(icon, null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 14.sp, color = NG.inkMuted, modifier = Modifier.weight(1f))
        Text(value, style = NG.statValue, color = accent, fontSize = 17.sp)
    }
}

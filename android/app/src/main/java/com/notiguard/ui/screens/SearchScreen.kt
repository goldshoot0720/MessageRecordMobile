package com.notiguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordFilter
import com.notiguard.ui.Fmt
import com.notiguard.ui.SearchResults
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.notiguard.ui.components.AppGlyph
import com.notiguard.ui.components.GuardButton
import com.notiguard.ui.components.ButtonTone
import com.notiguard.ui.components.GuardIcons
import com.notiguard.ui.components.IconAction
import com.notiguard.ui.components.screenBackground
import com.notiguard.ui.components.SegmentedTabs
import com.notiguard.ui.components.StatusPill
import com.notiguard.ui.theme.NG

@Composable
fun SearchScreen(
    appLabel: String,
    query: String,
    filter: RecordFilter,
    results: SearchResults,
    onQuery: (String) -> Unit,
    onFilter: (RecordFilter) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenRecord: (NotificationRecord) -> Unit,
    iconFor: (String) -> ImageBitmap?,
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val searchFocus = remember { FocusRequester() }
    // 進到搜尋頁就把游標放進輸入框並叫出鍵盤，少一次點擊。
    LaunchedEffect(Unit) {
        searchFocus.requestFocus()
        keyboard?.show()
    }
    Column(Modifier.fillMaxSize().screenBackground().safeDrawingPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconAction(GuardIcons.Back, "返回", onBack, tint = NG.ink)
            Spacer(Modifier.width(4.dp))
            Text(if (appLabel.isBlank()) "搜尋通知" else "搜尋 $appLabel 通知",
                style = NG.navTitle, color = NG.ink, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f))
        }
        OutlinedTextField(
            value = query, onValueChange = onQuery, singleLine = true,
            placeholder = { Text(if (appLabel.isBlank()) "App 名稱、標題或內容" else "輸入標題或內容") },
            leadingIcon = { Icon(GuardIcons.Search, null, tint = NG.blueLight, modifier = Modifier.size(19.dp)) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) {
                Icon(GuardIcons.Close, "清除搜尋", tint = NG.inkMuted, modifier = Modifier.size(18.dp))
            } },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NG.blue,
                unfocusedBorderColor = NG.line,
                focusedContainerColor = NG.cardMuted,
                unfocusedContainerColor = NG.cardMuted,
                focusedTextColor = NG.ink,
                unfocusedTextColor = NG.ink,
                cursorColor = NG.blueLight,
                focusedLabelColor = NG.blueLight,
                unfocusedLabelColor = NG.inkFaint,
                focusedPlaceholderColor = NG.inkFaint,
                unfocusedPlaceholderColor = NG.inkFaint,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            shape = NG.buttonShape,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                .focusRequester(searchFocus),
        )
        SegmentedTabs(
            options = listOf(RecordFilter.ALL to "全部", RecordFilter.BLOCKED to "已攔截", RecordFilter.ALLOWED to "已允許"),
            selected = filter, onSelect = onFilter,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            iconFor = {
                when (it) {
                    RecordFilter.ALL -> GuardIcons.Apps
                    RecordFilter.BLOCKED -> GuardIcons.Block
                    RecordFilter.ALLOWED -> GuardIcons.CheckMark
                }
            },
        )
        when {
            query.isBlank() -> SearchState(GuardIcons.Search, "輸入關鍵字，搜尋已儲存的通知。")
            results.loading -> SearchState(GuardIcons.Refresh, "搜尋中…")
            results.failed -> {
                SearchState(GuardIcons.Info, "暫時無法讀取通知，請重試。", NG.redLight)
                GuardButton("重試", onRetry, icon = GuardIcons.Refresh, tone = ButtonTone.Tonal,
                    fillWidth = false, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            results.records.isEmpty() -> SearchState(GuardIcons.Filter, "找不到符合的通知。\n試試其他關鍵字，或切換篩選條件。")
            else -> {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(GuardIcons.Check, null, tint = NG.blueLight, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("找到 ${results.records.size} 則通知", style = NG.caption, color = NG.inkFaint)
                }
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(results.records, key = { it.id }) { record ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(NG.cardShape)
                                .background(NG.cardBrush)
                                .border(1.dp, NG.lineSoft, NG.cardShape)
                                .clickable { focus.clearFocus(); onOpenRecord(record) }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppGlyph(record.packageName, record.appLabel, iconFor(record.packageName), size = 32)
                                Text(record.appLabel, style = NG.appName, color = NG.ink,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
                                StatusPill(blocked = record.blocked)
                            }
                            if (record.title.isNotBlank()) Text(record.title, style = NG.body, color = NG.ink,
                                maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                            if (record.text.isNotBlank()) Text(record.text, style = NG.summary, color = NG.inkMuted,
                                maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                            Text(Fmt.timestamp(record.postedAt), style = NG.caption, color = NG.inkFaint,
                                modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

/** 空／載入／失敗共用的狀態區塊：一顆圖示加一段說明，比純文字好認。 */
@Composable
private fun ColumnScope.SearchState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color = NG.inkFaint,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NG.cardMuted)
                .border(1.dp, NG.lineSoft, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(text, style = NG.body, color = NG.inkMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

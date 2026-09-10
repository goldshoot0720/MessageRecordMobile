package com.notiguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordFilter
import com.notiguard.ui.Fmt
import com.notiguard.ui.SearchResults
import com.notiguard.ui.components.AppGlyph
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
    Column(Modifier.fillMaxSize().background(NG.base).safeDrawingPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = NG.ink) }
            Text(if (appLabel.isBlank()) "搜尋通知" else "搜尋 $appLabel 通知",
                style = NG.navTitle, color = NG.ink, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f))
        }
        OutlinedTextField(
            value = query, onValueChange = onQuery, singleLine = true,
            label = { Text("搜尋通知") },
            placeholder = { Text(if (appLabel.isBlank()) "App 名稱、標題或內容" else "輸入標題或內容") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) {
                Icon(Icons.Filled.Close, "清除搜尋")
            } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            shape = NG.buttonShape,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        )
        SegmentedTabs(
            options = listOf(RecordFilter.ALL to "全部", RecordFilter.BLOCKED to "已攔截", RecordFilter.ALLOWED to "已允許"),
            selected = filter, onSelect = onFilter,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        when {
            query.isBlank() -> SearchMessage("輸入關鍵字，搜尋已儲存的通知。")
            results.loading -> SearchMessage("搜尋中…")
            results.failed -> {
                SearchMessage("暫時無法讀取通知，請重試。")
                TextButton(onClick = onRetry, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("重試") }
            }
            results.records.isEmpty() -> SearchMessage("找不到符合的通知。\n試試其他關鍵字，或切換篩選條件。")
            else -> {
                Text("找到 ${results.records.size} 則通知", style = NG.caption, color = NG.inkFaint,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)) {
                    items(results.records, key = { it.id }) { record ->
                        Column(Modifier.fillMaxWidth().clickable {
                            focus.clearFocus(); onOpenRecord(record)
                        }.padding(vertical = 14.dp)) {
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
                        HorizontalDivider(color = NG.lineSoft)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchMessage(text: String) {
    Text(text, style = NG.body, color = NG.inkMuted, modifier = Modifier.padding(24.dp))
}

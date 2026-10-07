package com.notiguard.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notiguard.data.NotiGuardRepository
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordFilter
import com.notiguard.data.TimeRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchResults(
    val records: List<NotificationRecord> = emptyList(),
    val loading: Boolean = false,
    val failed: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repo: NotiGuardRepository,
    packageName: String,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    val query = savedState.getStateFlow("searchQuery", "")
    val filter = savedState.getStateFlow("searchFilter", RecordFilter.ALL)
    val timeRange = repo.timeRange
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeRange.WEEK)
    private val retry = MutableStateFlow(0)
    val results = combine(query, filter, retry, repo.timeRange) { text, selection, _, range ->
        Triple(text, selection, range.since())
    }.flatMapLatest { (text, selection, since) ->
            repo.searchRecords(text, packageName.ifBlank { null }, selection, since)
                .map { SearchResults(records = it) }
                .onStart { emit(SearchResults(loading = text.isNotBlank())) }
                .catch { emit(SearchResults(failed = true)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResults())

    val recentSearches = repo.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) { savedState["searchQuery"] = value }
    fun setFilter(value: RecordFilter) { savedState["searchFilter"] = value }
    fun setTimeRange(value: TimeRange) { viewModelScope.launch { repo.setTimeRange(value) } }
    fun retry() { retry.value++ }

    /** 把目前關鍵字記進最近搜尋。空白不會進來。 */
    fun commitQuery() {
        repo.enqueueRememberSearch(query.value)
    }

    fun pickRecent(value: String) {
        setQuery(value)
        repo.enqueueRememberSearch(value)
    }

    fun removeRecent(value: String) {
        viewModelScope.launch { repo.forgetSearch(value) }
    }

    fun clearRecent() {
        viewModelScope.launch { repo.clearRecentSearches() }
    }
}

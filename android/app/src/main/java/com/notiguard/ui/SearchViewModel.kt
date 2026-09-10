package com.notiguard.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notiguard.data.NotiGuardRepository
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class SearchResults(
    val records: List<NotificationRecord> = emptyList(),
    val loading: Boolean = false,
    val failed: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    repo: NotiGuardRepository,
    packageName: String,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    val query = savedState.getStateFlow("searchQuery", "")
    val filter = savedState.getStateFlow("searchFilter", RecordFilter.ALL)
    private val retry = MutableStateFlow(0)
    val results = combine(query, filter, retry) { text, selection, _ -> text to selection }
        .flatMapLatest { (text, selection) ->
            repo.searchRecords(text, packageName.ifBlank { null }, selection)
                .map { SearchResults(records = it) }
                .onStart { emit(SearchResults(loading = text.isNotBlank())) }
                .catch { emit(SearchResults(failed = true)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResults())

    fun setQuery(value: String) { savedState["searchQuery"] = value }
    fun setFilter(value: RecordFilter) { savedState["searchFilter"] = value }
    fun retry() { retry.value++ }
}

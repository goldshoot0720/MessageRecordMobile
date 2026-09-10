package com.notiguard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notiguard.data.AppRule
import com.notiguard.data.AppSummary
import com.notiguard.data.GuardStats
import com.notiguard.data.NotiGuardRepository
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordExporter
import com.notiguard.data.RecordFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ---------------------------------------------------------------- 首頁

data class HomeUiState(
    val masterEnabled: Boolean = true,
    val stats: GuardStats = GuardStats(0, 0, 0),
    val blockedToday: Int = 0,
    val apps: List<AppSummary> = emptyList(),
)

class HomeViewModel(private val repo: NotiGuardRepository) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        repo.masterEnabled,
        repo.stats,
        repo.blockedToday,
        repo.appSummaries,
    ) { master, stats, today, apps ->
        HomeUiState(master, stats, today, apps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setMaster(enabled: Boolean) {
        viewModelScope.launch { repo.setMasterEnabled(enabled) }
    }
}

// ---------------------------------------------------------------- 應用程式頁

data class AppDetailUiState(
    val packageName: String = "",
    val appLabel: String = "",
    val total: Int = 0,
    val blocking: Boolean = true,
    val filter: RecordFilter = RecordFilter.ALL,
    val records: List<NotificationRecord> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppDetailViewModel(
    private val repo: NotiGuardRepository,
    private val packageName: String,
    private val appLabel: String,
) : ViewModel() {

    private val filter = MutableStateFlow(RecordFilter.ALL)

    private val records = filter.flatMapLatest { repo.records(packageName, it) }

    val state: StateFlow<AppDetailUiState> = combine(
        repo.recordCount(packageName),
        repo.rule(packageName),
        filter,
        records,
    ) { total, rule: AppRule?, currentFilter, list ->
        AppDetailUiState(
            packageName = packageName,
            appLabel = appLabel,
            total = total,
            blocking = rule?.blocking ?: true,
            filter = currentFilter,
            records = list,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppDetailUiState(packageName = packageName, appLabel = appLabel),
    )

    fun setFilter(value: RecordFilter) {
        filter.value = value
    }

    fun setBlocking(blocking: Boolean) {
        viewModelScope.launch { repo.setBlocking(packageName, appLabel, blocking) }
    }

    /** 產生這支 App 的匯出 JSON，交給呼叫端選擇儲存位置。 */
    fun buildExport(onReady: (fileName: String, json: String) -> Unit) {
        viewModelScope.launch {
            val json = repo.exportJson(packageName)
            onReady(RecordExporter.fileName(appLabel), json)
        }
    }
}

// ---------------------------------------------------------------- 通知詳情

@OptIn(ExperimentalCoroutinesApi::class)
class RecordDetailViewModel(
    private val repo: NotiGuardRepository,
    recordId: Long,
) : ViewModel() {

    val record: StateFlow<NotificationRecord?> = repo.record(recordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** 這支 App 目前的攔截規則，決定詳情頁底部兩顆按鈕的文案。 */
    val blocking: StateFlow<Boolean> = record
        .flatMapLatest { rec ->
            if (rec == null) flowOf(true)
            else repo.rule(rec.packageName).map { it?.blocking ?: true }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** 「允許此應用程式通知」：之後這支 App 的通知不再攔截。 */
    fun allowApp() = setBlocking(false)

    /** 「繼續攔截」／「改為攔截此應用程式」。 */
    fun blockApp() = setBlocking(true)

    private fun setBlocking(value: Boolean) {
        val current = record.value ?: return
        viewModelScope.launch {
            repo.setBlocking(current.packageName, current.appLabel, value)
        }
    }

    /** 「從此應用程式移除」：刪掉規則與該 App 的所有紀錄。 */
    fun forgetApp(onDone: () -> Unit) {
        val current = record.value ?: return
        viewModelScope.launch {
            repo.forget(current.packageName)
            onDone()
        }
    }
}

// ---------------------------------------------------------------- factory

/**
 * ViewModel 工廠。參數少到不需要 assisted injection。
 */
class NotiGuardViewModelFactory(
    private val repo: NotiGuardRepository,
    private val packageName: String = "",
    private val appLabel: String = "",
    private val recordId: Long = 0,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T =
        if (modelClass == SearchViewModel::class.java)
            SearchViewModel(repo, packageName, extras.createSavedStateHandle()) as T
        else create(modelClass)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(repo) as T

        modelClass.isAssignableFrom(AppDetailViewModel::class.java) ->
            AppDetailViewModel(repo, packageName, appLabel) as T

        modelClass.isAssignableFrom(RecordDetailViewModel::class.java) ->
            RecordDetailViewModel(repo, recordId) as T

        else -> error("Unknown ViewModel: ${modelClass.name}")
    }
}

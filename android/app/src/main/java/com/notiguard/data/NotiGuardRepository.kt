package com.notiguard.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar

private val Context.dataStore by preferencesDataStore(name = "notiguard_prefs")

/**
 * 資料層唯一入口。UI 與監聽服務都只透過這裡讀寫。
 */
class NotiGuardRepository(
    private val context: Context,
    private val dao: NotiGuardDao,
) {

    private val masterKey = booleanPreferencesKey("master_enabled")
    private val recentSearchKey = stringPreferencesKey("recent_searches")
    private val timeRangeKey = stringPreferencesKey("notification_time_range")
    private val recentSearchGate = Mutex()
    private val recentSearchWrites = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    // ---------- 總開關 ----------

    /** 全域攔截開關。關閉時所有通知照常顯示，但紀錄仍然寫入。 */
    val masterEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[masterKey] ?: true }

    suspend fun setMasterEnabled(enabled: Boolean) {
        context.dataStore.edit { it[masterKey] = enabled }
    }

    suspend fun isMasterEnabled(): Boolean = masterEnabled.first()

    // ---------- 首頁 ----------

    /** 通知要看多遠。沒選過就是一週之內。 */
    val timeRange: Flow<TimeRange> =
        context.dataStore.data.map { TimeRange.fromStored(it[timeRangeKey]) }

    suspend fun setTimeRange(range: TimeRange) {
        context.dataStore.edit { it[timeRangeKey] = range.name }
    }

    fun appSummaries(since: Long): Flow<List<AppSummary>> = dao.observeAppSummaries(since)

    fun stats(since: Long): Flow<GuardStats> = dao.observeStats(since)

    /** 今日已攔截則數。每次收集時以當下的當地零點計算。 */
    val blockedToday: Flow<Int> = dao.observeBlockedSince(startOfToday())

    // ---------- 應用程式頁 ----------

    fun records(packageName: String, filter: RecordFilter, since: Long = 0L): Flow<List<NotificationRecord>> =
        when (filter) {
            RecordFilter.ALL -> dao.observeRecords(packageName, since)
            RecordFilter.BLOCKED -> dao.observeRecords(packageName, blocked = true, since = since)
            RecordFilter.ALLOWED -> dao.observeRecords(packageName, blocked = false, since = since)
        }

    fun recordCount(packageName: String, since: Long = 0L): Flow<Int> = dao.observeCount(packageName, since)

    fun searchRecords(
        query: String,
        packageName: String?,
        filter: RecordFilter,
        since: Long = 0L,
    ): Flow<List<NotificationRecord>> =
        if (query.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList())
        else dao.searchRecords(query.trim(), packageName, when (filter) {
            RecordFilter.ALL -> null
            RecordFilter.BLOCKED -> true
            RecordFilter.ALLOWED -> false
        }, since)

    /** 搜尋通知頁的最近關鍵字，最新在前。 */
    val recentSearches: Flow<List<String>> =
        context.dataStore.data.map { RecentSearches.decode(it[recentSearchKey]) }

    /**
     * 離開頁面時 ViewModel 可能立刻被取消，所以這筆寫入不跟畫面的協程走。
     * 空白關鍵字直接略過。
     */
    fun enqueueRememberSearch(query: String) {
        if (query.isBlank()) return
        recentSearchWrites.launch { rememberSearch(query) }
    }

    suspend fun rememberSearch(query: String) {
        recentSearchGate.withLock {
            context.dataStore.edit { prefs ->
                val current = RecentSearches.decode(prefs[recentSearchKey])
                val next = RecentSearches.remember(current, query)
                if (next != current) prefs[recentSearchKey] = RecentSearches.encode(next)
            }
        }
    }

    suspend fun forgetSearch(query: String) {
        recentSearchGate.withLock {
            context.dataStore.edit { prefs ->
                val next = RecentSearches.forget(RecentSearches.decode(prefs[recentSearchKey]), query)
                prefs[recentSearchKey] = RecentSearches.encode(next)
            }
        }
    }

    suspend fun clearRecentSearches() {
        recentSearchGate.withLock {
            context.dataStore.edit { it.remove(recentSearchKey) }
        }
    }

    fun rule(packageName: String): Flow<AppRule?> = dao.observeRule(packageName)

    fun record(id: Long): Flow<NotificationRecord?> = dao.observeRecord(id)

    // ---------- 規則 ----------

    /** 這支 App 是否要攔截。沒設過規則的預設攔截。 */
    suspend fun isBlocking(packageName: String): Boolean =
        dao.rule(packageName)?.blocking ?: true

    suspend fun setBlocking(packageName: String, appLabel: String, blocking: Boolean) {
        dao.upsertRule(AppRule(packageName = packageName, appLabel = appLabel, blocking = blocking))
    }

    /** 「從此應用程式移除」：清掉規則與所有紀錄。 */
    suspend fun forget(packageName: String) {
        dao.deleteRule(packageName)
        dao.deleteRecordsFor(packageName)
    }

    // ---------- 寫入 ----------

    suspend fun insert(record: NotificationRecord) {
        dao.insertRecord(record)
    }

    suspend fun markRemoved(uid: String, at: Long = System.currentTimeMillis()) {
        dao.markRemoved(uid, at)
    }

    // ---------- 匯出 ----------

    /** 匯出成跨平台 JSON，供 iOS 版經「從檔案匯入」讀進去。 */
    suspend fun exportJson(packageName: String? = null): String =
        RecordExporter.toJson(dao.recordsForExport(packageName), deviceId())

    /** 穩定的裝置識別，只用來在多機同步時分辨紀錄來源。 */
    private fun deviceId(): String = "android-" +
        android.os.Build.MANUFACTURER.lowercase().replace(" ", "-") + "-" +
        android.os.Build.MODEL.lowercase().replace(" ", "-")

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}


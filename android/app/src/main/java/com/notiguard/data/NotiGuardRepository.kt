package com.notiguard.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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


    // ---------- 總開關 ----------

    /** 全域攔截開關。關閉時所有通知照常顯示，但紀錄仍然寫入。 */
    val masterEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[masterKey] ?: true }

    suspend fun setMasterEnabled(enabled: Boolean) {
        context.dataStore.edit { it[masterKey] = enabled }
    }

    suspend fun isMasterEnabled(): Boolean = masterEnabled.first()

    // ---------- 首頁 ----------

    val appSummaries: Flow<List<AppSummary>> = dao.observeAppSummaries()

    fun searchApps(query: String): Flow<List<AppSummary>> = dao.observeAppSummaries(query.trim())

    val stats: Flow<GuardStats> = dao.observeStats()

    /** 今日已攔截則數。每次收集時以當下的當地零點計算。 */
    val blockedToday: Flow<Int> = dao.observeBlockedSince(startOfToday())

    // ---------- 應用程式頁 ----------

    fun records(packageName: String, filter: RecordFilter): Flow<List<NotificationRecord>> =
        when (filter) {
            RecordFilter.ALL -> dao.observeRecords(packageName)
            RecordFilter.BLOCKED -> dao.observeRecords(packageName, blocked = true)
            RecordFilter.ALLOWED -> dao.observeRecords(packageName, blocked = false)
        }

    fun searchRecords(packageName: String, filter: RecordFilter, query: String): Flow<List<NotificationRecord>> =
        dao.searchRecords(packageName, when (filter) {
            RecordFilter.ALL -> null
            RecordFilter.BLOCKED -> true
            RecordFilter.ALLOWED -> false
        }, query.trim())

    fun recordCount(packageName: String): Flow<Int> = dao.observeCount(packageName)

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

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}


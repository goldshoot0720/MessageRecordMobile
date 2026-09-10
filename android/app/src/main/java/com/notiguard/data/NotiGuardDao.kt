package com.notiguard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotiGuardDao {

    // ---------- 寫入 ----------

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: NotificationRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecords(records: List<NotificationRecord>)

    @Query("UPDATE records SET removedAt = :at WHERE uid = :uid AND removedAt IS NULL")
    suspend fun markRemoved(uid: String, at: Long)

    @Upsert
    suspend fun upsertRule(rule: AppRule)

    // ---------- 規則 ----------

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName")
    suspend fun rule(packageName: String): AppRule?

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName")
    fun observeRule(packageName: String): Flow<AppRule?>

    @Query("DELETE FROM app_rules WHERE packageName = :packageName")
    suspend fun deleteRule(packageName: String)

    // ---------- 首頁 ----------

    /**
     * 依 App 聚合的清單，通知數多的排前面。
     * 左外連接 app_rules，沒有規則的 App 在 [AppSummary.isBlocking] 視為攔截中。
     */
    @Query(
        """
        SELECT r.packageName            AS packageName,
               MAX(r.appLabel)          AS appLabel,
               COUNT(*)                 AS total,
               SUM(CASE WHEN r.blocked THEN 1 ELSE 0 END) AS blockedCount,
               MAX(r.postedAt)          AS lastPostedAt,
               ru.blocking              AS blocking
        FROM records r
        LEFT JOIN app_rules ru ON ru.packageName = r.packageName
        GROUP BY r.packageName
        ORDER BY total DESC, lastPostedAt DESC
        """
    )
    fun observeAppSummaries(): Flow<List<AppSummary>>

    @Query(
        """
        SELECT COUNT(*)                                   AS total,
               COALESCE(SUM(CASE WHEN blocked THEN 1 ELSE 0 END), 0)   AS blockedCount,
               COUNT(DISTINCT packageName)                AS appCount
        FROM records
        """
    )
    fun observeStats(): Flow<GuardStats>

    /** 今日（傳入當地零點的 epoch 毫秒）已攔截則數。 */
    @Query("SELECT COUNT(*) FROM records WHERE blocked AND postedAt >= :since")
    fun observeBlockedSince(since: Long): Flow<Int>

    // ---------- 應用程式頁 ----------

    @Query(
        """
        SELECT * FROM records
        WHERE packageName = :packageName
        ORDER BY postedAt DESC
        """
    )
    fun observeRecords(packageName: String): Flow<List<NotificationRecord>>

    @Query(
        """
        SELECT * FROM records
        WHERE packageName = :packageName AND blocked = :blocked
        ORDER BY postedAt DESC
        """
    )
    fun observeRecords(packageName: String, blocked: Boolean): Flow<List<NotificationRecord>>

    @Query("SELECT COUNT(*) FROM records WHERE packageName = :packageName")
    fun observeCount(packageName: String): Flow<Int>

    // ---------- 詳情 ----------

    @Query("SELECT * FROM records WHERE id = :id")
    fun observeRecord(id: Long): Flow<NotificationRecord?>

    // ---------- 維護 ----------

    @Query("DELETE FROM records WHERE packageName = :packageName")
    suspend fun deleteRecordsFor(packageName: String)

    @Query("SELECT COUNT(*) FROM records")
    suspend fun recordCount(): Int
}


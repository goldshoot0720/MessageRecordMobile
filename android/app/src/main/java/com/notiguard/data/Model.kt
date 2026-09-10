package com.notiguard.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 紀錄的取得方式。與 shared/notiguard-record.schema.json 的 `source` 對應。 */
object RecordSource {
    const val ANDROID_LISTENER = "android_listener"
    const val COMPANION_SYNC = "companion_sync"
    const val SEED = "seed"
}

/**
 * 單則通知的永久紀錄。
 *
 * 攔截不等於刪除：即使 [blocked] 為 true、通知已從系統通知欄移除，這筆紀錄仍然保留。
 */
@Entity(
    tableName = "records",
    indices = [
        Index("packageName"),
        Index("postedAt"),
        Index(value = ["uid"], unique = true),
    ],
)
data class NotificationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 去重用的穩定鍵：通知識別碼、來源訊息時間與內容的 SHA-256。舊資料保留原鍵。 */
    val uid: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val channelId: String? = null,
    /** 通知類型，見 [Categorizer]。 */
    val category: String? = null,
    val postedAt: Long,
    /** 被來源 App 撤回的時間，未撤回為 null。 */
    val removedAt: Long? = null,
    val blocked: Boolean,
    val ongoing: Boolean = false,
    val source: String = RecordSource.ANDROID_LISTENER,
)

/**
 * 每個 App 的攔截規則。[blocking] 為 true 時，該 App 的新通知會被移出通知欄。
 */
@Entity(tableName = "app_rules")
data class AppRule(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val blocking: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
)

/** 首頁清單的一列。由 SQL 聚合而來，不是資料表。 */
data class AppSummary(
    val packageName: String,
    val appLabel: String,
    @ColumnInfo(name = "total") val total: Int,
    @ColumnInfo(name = "blockedCount") val blockedCount: Int,
    @ColumnInfo(name = "lastPostedAt") val lastPostedAt: Long,
    /** 來自 app_rules 的左外連接，沒有規則時視為攔截中。 */
    @ColumnInfo(name = "blocking") val blocking: Boolean?,
) {
    val isBlocking: Boolean get() = blocking ?: true
}

/** 首頁三格統計。 */
data class GuardStats(
    @ColumnInfo(name = "total") val total: Int,
    @ColumnInfo(name = "blockedCount") val blockedCount: Int,
    @ColumnInfo(name = "appCount") val appCount: Int,
)

/** 應用程式頁的三段切換。 */
enum class RecordFilter { ALL, BLOCKED, ALLOWED }

/**
 * 通知類型。優先採用系統給的 category，取不到才從內容推導。
 *
 * 推導是啟發式的，只用來分組與統計，不影響是否攔截。
 */
object Categorizer {
    private val PROMO = Regex("優惠|折扣|特賣|限時|活動|推薦|為你|снова|sale|deal|off", RegexOption.IGNORE_CASE)
    private val TRANSACTION = Regex("支出|存入|轉帳|交易|帳單|應繳|刷卡|扣款")
    private val SECURITY = Regex("登入|驗證碼|安全|密碼|授權|login|verify", RegexOption.IGNORE_CASE)
    private val GROUP = Regex("群組|社團|頻道|group|channel", RegexOption.IGNORE_CASE)

    fun of(systemCategory: String?, title: String, text: String): String {
        when (systemCategory) {
            "msg" -> return "一般訊息"
            "email" -> return "一般郵件"
            "call", "missed_call" -> return "通話通知"
            "promo" -> return "推廣訊息"
            "transport", "progress" -> return "系統通知"
            "event", "reminder" -> return "行事曆通知"
            "err", "sys", "service" -> return "系統通知"
        }
        val blob = "$title $text"
        return when {
            TRANSACTION.containsMatchIn(blob) -> "交易通知"
            SECURITY.containsMatchIn(blob) -> "安全性通知"
            PROMO.containsMatchIn(blob) -> "推廣訊息"
            GROUP.containsMatchIn(blob) -> "群組訊息"
            else -> "一般訊息"
        }
    }
}

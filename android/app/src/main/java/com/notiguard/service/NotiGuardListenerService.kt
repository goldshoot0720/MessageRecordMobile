package com.notiguard.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.notiguard.NotiGuardApp
import com.notiguard.data.Categorizer
import com.notiguard.data.NotiGuardRepository
import com.notiguard.data.NotificationRecord
import com.notiguard.data.RecordSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.security.MessageDigest

/**
 * 通知攔截的實作。
 *
 * 系統在使用者於「設定 → 通知 → 裝置與應用程式通知」授權後綁定這個服務，
 * 之後每一則通知的送達與撤回都會回呼到這裡。
 *
 * 流程：
 * 1. [onNotificationPosted] 收到通知 → 抽出內容 → 去重後寫入紀錄（略過群組摘要）
 * 2. 若總開關開啟且該 App 的規則是攔截 → [cancelNotification] 把它移出通知欄
 *
 * 攔截不等於刪除：通知從通知欄消失，但紀錄留在資料庫，可在 App 內完整回查。
 */
class NotiGuardListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val repo: NotiGuardRepository
        get() = (application as NotiGuardApp).repository

    private lateinit var identity: AppIdentity

    override fun onCreate() {
        super.onCreate()
        identity = AppIdentity(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (shouldIgnore(sbn)) return

        val extras = sbn.notification.extras
        val title = extras.charSequenceText(Notification.EXTRA_TITLE)
        val text = extras.charSequenceText(Notification.EXTRA_TEXT)
            .ifBlank { extras.charSequenceText(Notification.EXTRA_BIG_TEXT) }
            .ifBlank { extras.charSequenceText(Notification.EXTRA_SUB_TEXT) }

        val isSummary = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        // 摘要只負責攔截，不另存成訊息；一般空白佔位通知仍略過。
        if (!isSummary && title.isBlank() && text.isBlank()) return

        val packageName = sbn.packageName
        val label = identity.label(packageName)
        val ongoing = sbn.isOngoing || sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0

        scope.launch {
            // 常駐通知（音樂播放、導航、下載進度）攔掉會破壞來源 App，一律放行。
            val blocked = !ongoing &&
                repo.isMasterEnabled() &&
                repo.isBlocking(packageName)

            if (!isSummary) repo.insert(
                NotificationRecord(
                    uid = uidOf(sbn),
                    packageName = packageName,
                    appLabel = label,
                    title = title,
                    text = text,
                    channelId = sbn.notification.channelId,
                    category = Categorizer.of(sbn.notification.category, title, text),
                    postedAt = sbn.postTime,
                    blocked = blocked,
                    ongoing = ongoing,
                    source = RecordSource.ANDROID_LISTENER,
                )
            )

            if (blocked) {
                // 這一行就是「攔截」：把通知從系統通知欄移除。
                // 必須回到 binder thread 呼叫，用 post 丟回主執行緒最穩。
                runCatching { cancelNotification(sbn.key) }
                    .onFailure { Log.w(TAG, "cancel failed for ${sbn.key}", it) }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (shouldIgnore(sbn)) return
        scope.launch {
            repo.markRemoved(uidOf(sbn))
            // 升級前已記錄、升級後才撤回的通知仍能標記。
            repo.markRemoved("${sbn.key}:${sbn.postTime}")
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    /** 自己的通知不記也不攔，否則會遞迴。 */
    private fun shouldIgnore(sbn: StatusBarNotification): Boolean =
        sbn.packageName == packageName ||
            sbn.packageName == "$packageName.debug"

    private fun uidOf(sbn: StatusBarNotification): String {
        val notification = sbn.notification
        val extras = notification.extras
        val messages = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
            ?.messages.orEmpty()
        // postTime 是每次發布／更新的時間，不能當成訊息本身的時間。
        // 缺少來源時間時保守退回 postTime，避免合併真正的新訊息。
        val messageTime = messages.lastOrNull()?.timestamp?.takeIf { it > 0 }
            ?: notification.`when`.takeIf { it > 0 }
            ?: sbn.postTime
        val parts = listOf(
            sbn.key, messageTime.toString(),
            extras.charSequenceText(Notification.EXTRA_TITLE),
            extras.charSequenceText(Notification.EXTRA_TEXT),
            extras.charSequenceText(Notification.EXTRA_BIG_TEXT),
            extras.charSequenceText(Notification.EXTRA_SUB_TEXT),
        ) + messages.flatMap {
            listOf(it.timestamp.toString(), it.text?.toString().orEmpty(),
                it.sender?.toString().orEmpty(), it.dataMimeType.orEmpty(), it.dataUri?.toString().orEmpty())
        }
        val fingerprint = parts.joinToString("") { "${it.length}:$it" }
        val hash = MessageDigest.getInstance("SHA-256").digest(fingerprint.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        return "notification-v2:$hash"
    }

    private fun android.os.Bundle.charSequenceText(key: String): String =
        getCharSequence(key)?.toString()?.trim().orEmpty()

    companion object {
        private const val TAG = "NotiGuardListener"
    }
}

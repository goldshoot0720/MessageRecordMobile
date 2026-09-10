package com.notiguard.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * 通知存取權限的檢查與導向。
 *
 * 這個權限沒有 runtime permission dialog，只能把使用者送到系統設定頁自己開。
 */
object ListenerAccess {

    fun isGranted(context: Context): Boolean {
        val component = ComponentName(context, NotiGuardListenerService::class.java)
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        // 這個設定值是用冒號串起來的 flattened ComponentName 清單。
        return enabled.split(':').any {
            ComponentName.unflattenFromString(it) == component
        }
    }

    /**
     * 開啟系統的通知存取設定頁。
     *
     * Android 11 (API 30) 起可以帶 component 直接跳到本 App 那一列；
     * 舊版只能開總清單讓使用者自己找。
     */
    fun settingsIntent(context: Context): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val component = ComponentName(context, NotiGuardListenerService::class.java)
            return Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                putExtra(
                    Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                    component.flattenToString(),
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * 讓系統重新綁定服務。授權後偶爾會沒有立刻連上，
     * 或 App 更新後服務停住時可以呼叫這個。
     */
    fun requestRebind(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val component = ComponentName(context, NotiGuardListenerService::class.java)
            runCatching {
                android.service.notification.NotificationListenerService
                    .requestRebind(component)
            }
        }
    }
}
